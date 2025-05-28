package net.jmp.pinecone.quickstart.query;

/*
 * (#)Query.java    0.3.0   05/27/2025
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

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import com.mongodb.client.MongoClient;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.*;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query class.
///
/// @version    0.3.0
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

    /// Query the index by vector ID.
    ///
    /// @param  vectorId java.lang.String
    /// @return          java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    List<ScoredVectorWithUnsignedIndices> queryById(final String vectorId) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(vectorId));
        }

        List<ScoredVectorWithUnsignedIndices> matches;

        this.logger.info("Querying index: {}", this.indexName);
        this.logger.info("Querying ID   : {}", vectorId);

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final QueryResponseWithUnsignedIndices queryResponse =
                    index.queryByVectorId(10,
                            vectorId,
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

                    final DocumentFetcher fetcher = new DocumentFetcher(this.mongoClient, this.collectionName, this.dbName);
                    final Optional<UnstructuredTextDocument> content = fetcher.getDocument(fields.get("mongoid").getStringValue());

                    content.ifPresent(doc -> this.logger.debug("Content  : {}", doc.getContent()));
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
    }

    /// Query the index by query vector.
    ///
    /// @param  queryVector java.util.List<java.lang.Float>
    /// @param  categories  java.util.Set<java.lang.String>
    /// @return             java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    List<ScoredVectorWithUnsignedIndices> query(final List<Float> queryVector,
                                                final Set<String> categories) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryVector.toString(), categories));
        }

        List<ScoredVectorWithUnsignedIndices> matches;

        this.logger.info("Querying index: {}", this.indexName);

        final Struct filter = this.createFilter(categories);

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final QueryResponseWithUnsignedIndices queryResponse =
                    index.query(10,
                            queryVector,
                            null,
                            null,
                            null,
                            this.namespace,
                            filter,
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

                    final DocumentFetcher fetcher = new DocumentFetcher(this.mongoClient, this.collectionName, this.dbName);
                    final Optional<UnstructuredTextDocument> content = fetcher.getDocument(fields.get("mongoid").getStringValue());

                    content.ifPresent(doc -> this.logger.debug("Content  : {}", doc.getContent()));
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
    }

    /// Create the filter. For multiple categories, the filter will be equivalent to:
    ///
    /// "category": { "$in": ["category1", "category2"] }
    ///
    /// @param  categories java.util.Set<java.lang.String>
    /// @return            io.pinecone.unsigned_indices_model.Struct
    private Struct createFilter(final Set<String> categories) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(categories));
        }

        Struct filter = null;

        if (!categories.isEmpty()) {
            if (categories.size() == 1) {
                filter = Struct.newBuilder()
                        .putFields("category", Value.newBuilder().setStringValue(categories.iterator().next()).build())
                        .build();
            } else {
                // Start with the list: ["category1", "category2"]

                final ListValue.Builder listValueBuilder = ListValue.newBuilder();

                for (final String category : categories) {
                    listValueBuilder.addValues(Value.newBuilder().setStringValue(category).build());
                }

                final ListValue categoriesList = listValueBuilder.build();

                // Create the $in struct with the "$in" key

                final Struct inStruct = Struct.newBuilder()
                        .putFields("$in", Value.newBuilder().setListValue(categoriesList).build())
                        .build();

                // Creating the final category struct with the "category" key

                filter = Struct.newBuilder()
                        .putFields("category", Value.newBuilder().setStructValue(inStruct).build())
                        .build();
            }

            this.logger.debug("Filter: {}", filter);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(filter));
        }

        return filter;
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
