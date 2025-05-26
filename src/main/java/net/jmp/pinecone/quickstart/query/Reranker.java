package net.jmp.pinecone.quickstart.query;

/*
 * (#)Reranker.java 0.2.0   05/26/2025
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
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Projections;

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.*;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.bson.conversions.Bson;

import org.bson.types.ObjectId;

import org.openapitools.inference.client.ApiException;

import org.openapitools.inference.client.model.RankedDocument;
import org.openapitools.inference.client.model.RerankResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The re-ranker class.
///
/// @version    0.2.0
/// @since      0.2.0
final class Reranker {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    private final Pinecone pinecone;

    /// The reranking model.
    private final String rerankingModel;

    /// The query text.
    private final String queryText;

    /// The MongoDB client.
    private final MongoClient mongoClient;

    /// The collection name.
    private final String collectionName;

    /// The database name.
    private final String dbName;

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.query.Reranker.Builder
    Reranker(final Builder builder) {
        super();

        this.pinecone = builder.pinecone;
        this.rerankingModel = builder.rerankingModel;
        this.queryText = builder.queryText;
        this.mongoClient = builder.mongoClient;
        this.collectionName = builder.collectionName;
        this.dbName = builder.dbName;
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.query.Reranker.Builder
    static Builder builder() {
        return new Builder();
    }

    /// Rerank the results.
    ///
    /// @param  matches     java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    /// @return             java.util.List<java.lang.String>
    List<String> rerank(final List<ScoredVectorWithUnsignedIndices> matches) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(matches));
        }

        this.logger.info("Reranking model: {}", this.rerankingModel);
        this.logger.info("Reranking results for {} matches", matches.size());

        /* Create a list of documents to rerank. */

        final List<Map<String, Object>> documents = this.getDocuments(matches);

        /* Rerank the documents based on the content field */

        final RerankResult result = this.rerankDocuments(documents);

        /* Get the ranked content. */

        final List<String> rankedContent = this.getRankedContent(result, matches.size());

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(rankedContent));
        }

        return rankedContent;
    }

    /// Get the documents.
    ///
    /// @param  matches java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    /// @return         java.util.List<java.util.Map<java.lang.String, java.lang.Object>>
    private List<Map<String, Object>> getDocuments(final List<ScoredVectorWithUnsignedIndices> matches) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(matches));
        }

        final List<Map<String, Object>> documents = new ArrayList<>(matches.size());

        for (final ScoredVectorWithUnsignedIndices match : matches) {
            final Struct metadata = match.getMetadata();
            final Map<String, Value> fields = metadata.getFieldsMap();
            final Map<String, Object> document = new HashMap<>();

            document.put("mongoid", fields.get("mongoid").getStringValue());
            document.put("documentid", fields.get("documentid").getStringValue());
            document.put("category", fields.get("category").getStringValue());

            final Optional<UnstructuredTextDocument> content = this.getDocument(fields.get("mongoid").getStringValue());
            final UnstructuredTextDocument doc = content.orElseThrow(() -> new RuntimeException("Could not find MongoDB document: " + fields.get("mongoid").getStringValue()));

            document.put("content", doc.getContent());

            documents.add(document);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(documents));
        }

        return documents;
    }

    /// Rerank the documents.
    ///
    /// @param  documents   java.util.List<java.util.Map<java.lang.String, java.lang.Object>>
    /// @return             io.pinecone.rerank_model.RerankResult
    private RerankResult rerankDocuments(final List<Map<String, Object>> documents) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(documents));
        }

        final List<String> rankFields = List.of("content");

        /* Create the parameters for the reranking model */

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("truncate", "END");

        /* Perform the reranking */

        final Inference inference = this.pinecone.getInferenceClient();

        RerankResult result = null;

        try {
            result = inference.rerank(
                    this.rerankingModel,
                    this.queryText,
                    documents,
                    rankFields, 10,
                    true,
                    parameters
            );
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Get the ranked content.
    ///
    /// @param  result  io.pinecone.rerank_model.RerankResult
    /// @param  size    int
    /// @return         java.util.List<java.lang.String>
    private List<String> getRankedContent(final RerankResult result, final int size) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(result));
        }

        List<String> rankedContent = new ArrayList<>(size);

        if (result != null) {
            final List<RankedDocument> rankedDocuments = result.getData();

            for (final RankedDocument rankedDocument : rankedDocuments) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Document: {}", rankedDocument.toJson());
                }

                final Map<String, Object> document = rankedDocument.getDocument();

                assert document != null;

                final String mongoId = (String) document.get("mongoid");
                final String documentId = (String) document.get("documentid");
                final String category = (String) document.get("category");
                final String content = (String) document.get("content");

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Mongo ID: {}", mongoId);
                    this.logger.debug("Doc ID  : {}", documentId);
                    this.logger.debug("Category: {}", category);
                }

                this.logger.info(content);

                rankedContent.add(content);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(rankedContent));
        }

        return rankedContent;
    }

    /// Get a document from MongoDB.
    ///
    /// @param  mongoId java.lang.String
    /// @return         java.util.Optional<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    private Optional<UnstructuredTextDocument> getDocument(final String mongoId) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(mongoId));
        }

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);

        final Bson projectionFields = Projections.fields(
                Projections.include("id", "content", "category")
        );

        final Document mongoDocument = collection
                .find(eq(new ObjectId(mongoId)))
                .projection(projectionFields)
                .first();

        UnstructuredTextDocument document = null;

        if (mongoDocument != null) {
            document = new UnstructuredTextDocument(
                    mongoId,
                    mongoDocument.get("id").toString(),
                    mongoDocument.get("content").toString(),
                    mongoDocument.get("category").toString()
            );
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(document));
        }

        return Optional.ofNullable(document);
    }

    /// The builder class.
    static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The reranking model.
        private String rerankingModel;

        /// The query text.
        private String queryText;

        /// The MongoDB client.
        private MongoClient mongoClient;

        /// The MongoDB collection name.
        private String collectionName;

        /// The MongoDB database name.
        private String dbName;

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    net.jmp.pinecone.Pinecone
        /// @return             net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the reranking model.
        ///
        /// @param  rerankingModel  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText   java.lang.String
        /// @return            net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName  java.lang.String
        /// @return         net.jmp.pinecone.quickstart.query.Reranker.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the reranker.
        ///
        /// @return  net.jmp.pinecone.quickstart.query.Reranker
        public Reranker build() {
            return new Reranker(this);
        }
    }
}
