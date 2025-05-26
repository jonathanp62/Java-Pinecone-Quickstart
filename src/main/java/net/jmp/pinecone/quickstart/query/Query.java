package net.jmp.pinecone.quickstart.query;

/*
 * (#)Query.java    0.2.0   05/26/2025
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

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query class.
///
/// @version    0.2.0
/// @since      0.2.0
final class Query {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    private final Pinecone pinecone;

    /// The index name.
    private final String indexName;

    /// The namespace.
    private final String namespace;

    /// The MongoDB client.
    private final MongoClient mongoClient;

    /// The MongoDB collection name.
    private final String collectionName;

    /// The MongoDB database name.
    private final String dbName;

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.query.Query.Builder
    Query(final Builder builder) {
        super();

        this.pinecone = builder.pinecone;
        this.indexName = builder.indexName;
        this.namespace = builder.namespace;
        this.mongoClient = builder.mongoClient;
        this.collectionName = builder.collectionName;
        this.dbName = builder.dbName;
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.query.Query.Builder
    static Builder builder() {
        return new Builder();
    }

    /// Query the index.
    ///
    /// @param  queryVector java.util.List<java.lang.Float>
    /// @return             java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    List<ScoredVectorWithUnsignedIndices> query(final List<Float> queryVector) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryVector.toString()));
        }

        List<ScoredVectorWithUnsignedIndices> matches = new ArrayList<>();

        this.logger.info("Querying index: {}", this.indexName);

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final QueryResponseWithUnsignedIndices queryResponse =
                    index.query(10,
                            queryVector,
                            null,
                            null,
                            null,
                            this.namespace,
                            null,
                            true,
                            true);

            matches = queryResponse.getMatchesList();

            for (final ScoredVectorWithUnsignedIndices match : matches) {
                final Struct metadata = match.getMetadata();
                final Map<String, Value> fields = metadata.getFieldsMap();

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Vector ID: {}", match.getId());
                    this.logger.debug("Score    : {}", match.getScore());
                    this.logger.debug("Mongo ID : {}", fields.get("mongoid").getStringValue());
                    this.logger.debug("Doc ID   : {}", fields.get("documentid").getStringValue());
                    this.logger.debug("Category : {}", fields.get("category").getStringValue());

                    final Optional<UnstructuredTextDocument> content = this.getDocument(fields.get("mongoid").getStringValue());

                    content.ifPresent(doc -> this.logger.debug("Content  : {}", doc.getContent()));
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
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

        /// The index name.
        private String indexName;

        /// The namespace.
        private String namespace;

        /// The MongoDB client.
        private MongoClient mongoClient;

        /// The MongoDB collection name.
        private String collectionName;

        /// The MongoDB database name.
        private String dbName;

        /// The default constructor.
        Builder() {

        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone net.jmp.pinecone.Pinecone
        /// @return          net.jmp.pinecone.quickstart.query.Query.Builder
        Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.Query.Builder
        Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.Query.Builder
        Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.query.Query.Builder
        Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.Query.Builder
        Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.query.Query.Builder
        Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the object.
        ///
        /// @return net.jmp.pinecone.quickstart.query.Query
        Query build() {
            return new Query(this);
        }
    }
}
