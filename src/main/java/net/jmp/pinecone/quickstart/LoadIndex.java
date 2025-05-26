package net.jmp.pinecone.quickstart;

/*
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

import java.util.*;

import java.util.stream.Collectors;

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
/// @version    0.2.0
/// @since      0.2.0
final  class LoadIndex extends IndexOperation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    private final String embeddingModel;

    /// The mongo client.
    private final MongoClient mongoClient;

    /// The collection name.
    private final String collectionName;

    /// The database name.
    private final String dbName;

    /// The constructor.
    ///
    /// @param  pinecone        io.pinecone.clients.Pinecone
    /// @param  embeddingModel  java.lang.String
    /// @param  indexName       java.lang.String
    /// @param  namespace       java.lang.String
    /// @param  mongoClient     com.mongodb.client.MongoClient
    LoadIndex(final Pinecone pinecone,
              final String embeddingModel,
              final String indexName,
              final String namespace,
              final MongoClient mongoClient) {
        super(pinecone, indexName, namespace);

        this.embeddingModel = embeddingModel;
        this.mongoClient = mongoClient;
        this.collectionName = System.getProperty("app.mongoDbCollection");
        this.dbName = System.getProperty("app.mongoDbName");
    }

    /// The operate method.
    @Override
    protected void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.indexExists() && !this.isIndexLoaded()) {
            final List<UnstructuredTextDocument> documents = this.createContent();
            final List<Embedding> embeddings = this.createEmbeddings(documents);
            final List<Struct> metadata = this.createMetadata(documents);

            assert embeddings.size() == metadata.size();

            final List<VectorWithUnsignedIndices> vectors = new ArrayList<>(embeddings.size());

            for (int i = 0; i < embeddings.size(); i++) {
                final Embedding embedding = embeddings.get(i);
                final Struct metadataStruct = metadata.get(i);
                final String vectorId = metadataStruct.getFieldsMap().get("documentid").getStringValue();

                vectors.add(buildUpsertVectorWithUnsignedIndices(vectorId, embedding.getDenseEmbedding().getValues(), null, null, metadataStruct));
            }

            this.logger.info("Loading index: {}", this.indexName);

            try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
                final UpsertResponse result = index.upsert(vectors, this.namespace);
                final int upsertedCount = result.getUpsertedCount();
                final int serializedSize = result.getSerializedSize();

                this.logger.info("Upserted {} vectors", upsertedCount);
                this.logger.info("Serialized size: {}", serializedSize);
            }
        } else {
            this.logger.info("Index either does not exist or is already loaded: {}", this.indexName);
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
    /// @param  documents   java.util.List<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    /// @return             java.util.List<io.pinecone.clients.model.Embedding>
    private List<Embedding> createEmbeddings(final List<UnstructuredTextDocument> documents) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(documents));
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
            this.logger.debug("Embedding model: {}", this.embeddingModel);
            this.logger.debug("Parameters: {}", parameters);
            this.logger.debug("Embedding text: {}", inputs);
        }

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(this.embeddingModel, parameters, inputs);
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
}
