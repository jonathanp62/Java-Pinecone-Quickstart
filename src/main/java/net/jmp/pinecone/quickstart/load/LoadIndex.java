package net.jmp.pinecone.quickstart.load;

/*
 * (#)LoadIndex.java    0.4.0   06/04/2025
 * (#)LoadIndex.java    0.2.0   05/21/2025
 *
 * @author   Jonathan Parker
 *
 * MIT License
 *
 * Copyright (c) 2025 Jonathan M. Parker
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;

import io.pinecone.proto.UpsertResponse;

import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import static java.lang.Integer.toUnsignedLong;

import java.util.*;

import java.util.stream.Collectors;

import net.jmp.pinecone.quickstart.Operation;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.bson.conversions.Bson;

import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;
import org.openapitools.inference.client.model.EmbeddingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The load index class.
///
/// @version    0.4.0
/// @since      0.2.0
public final class LoadIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.load.LoadIndex.Builder
    private LoadIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .denseEmbeddingModel(builder.denseEmbeddingModel)
                .sparseEmbeddingModel(builder.sparseEmbeddingModel)
                .denseIndexName(builder.denseIndexName)
                .sparseIndexName(builder.sparseIndexName)
                .namespace(builder.namespace)
                .mongoClient(builder.mongoClient)
                .collectionName(builder.collectionName)
                .dbName(builder.dbName)
        );
    }

    /// Return the builder.
    ///
    /// @return  net.jmp.pinecone.quickstart.load.LoadIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        this.loadDenseIndex();
        this.loadSparseIndex();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Load the dense index.
    private void loadDenseIndex() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.doesDenseIndexExist() && !this.isDenseIndexLoaded()) {
            final List<UnstructuredTextDocument> documents = this.createContent();
            final List<Embedding> embeddings = this.createEmbeddings(documents, this.denseEmbeddingModel);
            final List<Struct> metadata = this.createMetadata(documents);

            assert embeddings.size() == metadata.size();

            final List<VectorWithUnsignedIndices> vectors = new ArrayList<>(embeddings.size());

            for (int i = 0; i < embeddings.size(); i++) {
                final Embedding embedding = embeddings.get(i);
                final Struct metadataStruct = metadata.get(i);
                final String vectorId = metadataStruct.getFieldsMap().get("documentid").getStringValue();

                vectors.add(
                        buildUpsertVectorWithUnsignedIndices(
                                vectorId,
                                embedding.getDenseEmbedding().getValues(),
                                null,
                                null,
                                metadataStruct
                        )
                );
            }

            this.logger.info("Loading dense index: {}", this.denseIndexName);

            try (final Index index = this.pinecone.getIndexConnection(this.denseIndexName)) {
                final UpsertResponse result = index.upsert(vectors, this.namespace);
                final int upsertedCount = result.getUpsertedCount();
                final int serializedSize = result.getSerializedSize();

                this.logger.info("Upserted {} vectors", upsertedCount);
                this.logger.info("Serialized size: {}", serializedSize);
            }
        } else {
            this.logger.info("Dense index either does not exist or is already loaded: {}", this.denseIndexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Load the sparse index.
    private void loadSparseIndex() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.doesSparseIndexExist() && !this.isSparseIndexLoaded()) {
            final List<UnstructuredTextDocument> documents = this.createContent();
            final List<Embedding> sparseEmbeddings = this.createEmbeddings(documents, this.sparseEmbeddingModel);
            final List<Struct> metadata = this.createMetadata(documents);

            assert sparseEmbeddings.size() == metadata.size();

            this.logger.info("Loading sparse index: {}", this.sparseIndexName);

            try (final Index index = this.pinecone.getIndexConnection(this.sparseIndexName)) {
                int totalUpsertedCount = 0;

                for (int i = 0; i < sparseEmbeddings.size(); i++) {
                    final Embedding sparseEmbedding = sparseEmbeddings.get(i);
                    final Struct metadataStruct = metadata.get(i);
                    final String vectorId = metadataStruct.getFieldsMap().get("documentid").getStringValue();

                    final List<Long> sparseIndices = new ArrayList<>(sparseEmbedding.getSparseEmbedding().getSparseIndices().size());

                    for (int j = 0; j < sparseEmbedding.getSparseEmbedding().getSparseIndices().size(); j++) {
                        sparseIndices.add(toUnsignedLong(sparseEmbedding.getSparseEmbedding().getSparseIndices().get(j)));
                    }

                    final List<Float> sparseValues = sparseEmbedding.getSparseEmbedding().getSparseValues();

                    final UpsertResponse result = index.upsert(
                            vectorId,
                            Collections.emptyList(),
                            sparseIndices,
                            sparseValues,
                            metadataStruct,
                            this.namespace
                    );

                    final int upsertedCount = result.getUpsertedCount();

                    totalUpsertedCount += upsertedCount;

                    if (this.logger.isDebugEnabled()) {
                        final int serializedSize = result.getSerializedSize();

                        this.logger.debug("Upserted {} vectors", upsertedCount);
                        this.logger.debug("Serialized size: {}", serializedSize);
                    }
                }

                this.logger.info("Upserted {} total vectors", totalUpsertedCount);
            }
        } else {
            this.logger.info("Sparse index either does not exist or is already loaded: {}", this.sparseIndexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Create content from the database.
    ///
    /// @return  java.util.List<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    private List<UnstructuredTextDocument> createContent() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final List<UnstructuredTextDocument> documents = new LinkedList<>();

        /* Get the text from the database */

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);

        final Bson projectionFields = Projections.fields(
                Projections.include("id", "content", "category")
        );

        try (final MongoCursor<Document> cursor = collection
                .find()
                .projection(projectionFields)
                .sort(Sorts.ascending("id"))
                .iterator()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("There are {} documents available", cursor.available());
            }

            while (cursor.hasNext()) {
                final Document document = cursor.next();
                final String mongoId = document.get("_id").toString();
                final String documentId = document.get("id").toString();
                final String content = document.get("content").toString();
                final String category = document.get("category").toString();

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("MongoId : {}", mongoId);
                    this.logger.debug("DocId   : {}", documentId);
                    this.logger.debug("Content : {}", content);
                    this.logger.debug("Category: {}", category);
                }

                documents.add(new UnstructuredTextDocument(mongoId, documentId, content, category));
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(documents));
        }

        return documents;
    }

    /// Create embeddings.
    ///
    /// @param  documents       java.util.List<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    /// @param  embeddingModel  java.lang.String
    /// @return                 java.util.List<io.pinecone.clients.model.Embedding>
    private List<Embedding> createEmbeddings(final List<UnstructuredTextDocument> documents,
                                             final String embeddingModel) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(documents, embeddingModel));
        }

        List<Embedding> embeddingList = new ArrayList<>();

        final Inference client = this.pinecone.getInferenceClient();

        final List<String> inputs = documents.stream()
                .map(UnstructuredTextDocument::getContent)
                .collect(Collectors.toList());

        /* The parameters relate to the embedding model */

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Embedding model: {}", embeddingModel);
            this.logger.debug("Parameters: {}", parameters);
            this.logger.debug("Embedding text: {}", inputs);
        }

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(embeddingModel, parameters, inputs);
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (embeddings != null) {
            embeddingList = embeddings.getData();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Embeddings: {}", embeddings.toJson());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(embeddingList));
        }

        return embeddingList;
    }

    /// Create metadata.
    ///
    /// @param  documents   java.util.List<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    /// @return             java.util.List<com.google.protobuf.Struct>
    private List<Struct> createMetadata(final List<UnstructuredTextDocument> documents) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(documents));
        }

        List<Struct> metadataList = new ArrayList<>();

        for (final UnstructuredTextDocument document : documents) {
            final Struct metadataStruct = Struct.newBuilder()
                    .putFields("mongoid", Value.newBuilder().setStringValue(document.getMongoId()).build())
                    .putFields("documentid", Value.newBuilder().setStringValue(document.getDocumentId()).build())
                    .putFields("category", Value.newBuilder().setStringValue(document.getCategory()).build())
                    .build();

            metadataList.add(metadataStruct);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Metadata: {}", metadataList);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(metadataList));
        }

        return metadataList;
    }

    /// The builder class.
    public static class Builder {
        /// The pinecone client.
        private Pinecone pinecone;

        /// The dense embedding model.
        private String denseEmbeddingModel;

        /// The sparse embedding model.
        private String sparseEmbeddingModel;

        /// The dense index name.
        private String denseIndexName;

        /// The sparse index name.
        private String sparseIndexName;

        /// The namespace.
        private String namespace;

        /// The mongo client.
        private MongoClient mongoClient;

        /// The collection name.
        private String collectionName;

        /// The database name.
        private String dbName;

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the pinecone client.
        ///
        /// @param  pinecone    net.jmp.pinecone.quickstart.Pinecone
        /// @return             net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the dense embedding model.
        ///
        /// @param  denseEmbeddingModel java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder denseEmbeddingModel(final String denseEmbeddingModel) {
            this.denseEmbeddingModel = denseEmbeddingModel;

            return this;
        }

        /// Set the sparse embedding model.
        ///
        /// @param  sparseEmbeddingModel    java.lang.String
        /// @return                         net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder sparseEmbeddingModel(final String sparseEmbeddingModel) {
            this.sparseEmbeddingModel = sparseEmbeddingModel;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  denseIndexName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder denseIndexName(final String denseIndexName) {
            this.denseIndexName = denseIndexName;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  sparseIndexName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder sparseIndexName(final String sparseIndexName) {
            this.sparseIndexName = sparseIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the mongo client.
        ///
        /// @param  mongoClient io.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.load.LoadIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the load index.
        ///
        /// @return net.jmp.pinecone.quickstart.load.LoadIndex
        public LoadIndex build() {
            return new LoadIndex(this);
        }
    }
}
