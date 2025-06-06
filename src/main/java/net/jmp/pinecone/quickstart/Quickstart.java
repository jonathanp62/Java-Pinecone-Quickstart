package net.jmp.pinecone.quickstart;

/*
 * (#)Quickstart.java   0.3.0   05/27/2025
 * (#)Quickstart.java   0.2.0   05/21/2025
 * (#)Quickstart.java   0.1.0   05/17/2025
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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import io.pinecone.clients.Pinecone;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

import net.jmp.pinecone.quickstart.create.CreateIndex;
import net.jmp.pinecone.quickstart.delete.DeleteIndex;
import net.jmp.pinecone.quickstart.describe.DescribeIndex;
import net.jmp.pinecone.quickstart.describe.DescribeModels;
import net.jmp.pinecone.quickstart.describe.DescribeNamespace;
import net.jmp.pinecone.quickstart.fetch.FetchIndex;
import net.jmp.pinecone.quickstart.list.ListIndex;
import net.jmp.pinecone.quickstart.list.ListIndexes;
import net.jmp.pinecone.quickstart.list.ListNamespaces;
import net.jmp.pinecone.quickstart.load.LoadIndex;
import net.jmp.pinecone.quickstart.query.QueryIndex;
import net.jmp.pinecone.quickstart.store.StoreUnstructuredText;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The quickstart class.
///
/// @version    0.3.0
/// @since      0.1.0
final class Quickstart {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    private final String embeddingModel;

    /// The index name.
    private final String indexName;

    /// The MongoDB collection.
    private final String mongoDbCollection;

    /// The MongoDB name.
    private final String mongoDbName;

    /// The MongoDB URI file name.
    private final String mongoDbUriFile;

    /// The namespace.
    private final String namespace;

    /// The reranking model.
    private final String rerankingModel;

    /// The query text.
    private final String queryText;

    /// The Open AI API key.
    private String openAiApiKey;

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.Quickstart.Builder
    private Quickstart(final Builder builder) {
        super();

        this.embeddingModel = builder.embeddingModel;
        this.indexName = builder.indexName;
        this.mongoDbCollection = builder.mongoDbCollection;
        this.mongoDbName = builder.mongoDbName;
        this.mongoDbUriFile = builder.mongoDbUriFile;
        this.namespace = builder.namespace;
        this.rerankingModel = builder.rerankingModel;
        this.queryText = builder.queryText;
    }

    /// The builder method.
    ///
    /// @return net.jmp.pinecone.quickstart.Quickstart.Builder
    static Builder builder() {
        return new Builder();
    }

    /// The start method.
    ///
    /// @param  operation   java.lang.String
    void start(final String operation) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(operation));
        }

        this.openAiApiKey = this.getOpenAIApiKey().orElseThrow(() -> new RuntimeException("OpenAI API key not found"));

        final String mongoDbUri = this.getMongoDbUri().orElseThrow(() -> new RuntimeException("MongoDB URI not found"));
        final String pineconeApiKey = this.getPineconeApiKey().orElseThrow(() -> new RuntimeException("Pinecone API key not found"));
        final Pinecone pinecone = new Pinecone.Builder(pineconeApiKey).build();

        try (final MongoClient mongoClient = MongoClients.create(mongoDbUri)) {
            switch (operation) {
                case "create" -> this.createIndex(pinecone);
                case "delete" -> this.deleteIndex(pinecone);
                case "describe" -> this.describeIndex(pinecone);
                case "describeModels" -> this.describeModels(pinecone);
                case "describeNamespace" -> this.describeNamespace(pinecone);
                case "fetch" -> this.fetchIndex(pinecone);
                case "list" -> this.listIndex(pinecone);
                case "listIndexes" -> this.listIndexes(pinecone);
                case "listNamespaces" -> this.listNamespaces(pinecone);
                case "load" -> this.loadIndex(pinecone, mongoClient);
                case "query" -> this.queryIndex(pinecone, mongoClient);
                case "store" -> this.storeUnstructuredText(mongoClient);
                default -> this.logger.error("Unknown operation: {}", operation);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Get the OpenAI API key.
    ///
    /// @return java.util.Optional<java.lang.String>
    private Optional<String> getOpenAIApiKey() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Optional<String> apiKey = this.getApiKey("app.openaiApiKey");

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(apiKey));
        }

        return apiKey;
    }

    /// Get the Pinecone API key.
    ///
    /// @return java.util.Optional<java.lang.String>
    private Optional<String> getPineconeApiKey() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Optional<String> apiKey = this.getApiKey("app.pineconeApiKey");

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(apiKey));
        }

        return apiKey;
    }

    /// Get the API key.
    ///
    /// @param  propertyName    java.lang.String
    /// @return                 java.util.Optional<java.lang.String>
    private Optional<String> getApiKey(final String propertyName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(propertyName));
        }

        final String apiKeyFileName = System.getProperty(propertyName);

        String apiKey = null;

        try {
            apiKey = Files.readString(Paths.get(apiKeyFileName)).trim();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("API key file: {}", apiKeyFileName);
                this.logger.debug("API key: {}", apiKey);
            }
        } catch (final IOException ioe) {
            this.logger.error("Unable to read API key file: {}", apiKeyFileName, ioe);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(apiKey));
        }

        return Optional.ofNullable(apiKey);
    }

    /// Get the MongoDB URI.
    ///
    /// @return java.util.Optional<java.lang.String>
    private Optional<String> getMongoDbUri() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String mongoDbUriFileName = this.mongoDbUriFile;

        String mongoDbUri = null;

        try {
            mongoDbUri = Files.readString(Paths.get(mongoDbUriFileName)).trim();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("MongoDb URI file: {}", mongoDbUriFileName);
                this.logger.debug("MongoDb URI: {}", mongoDbUri);
            }
        } catch (final IOException ioe) {
            this.logger.error("Unable to read MongoDb URI file: {}", mongoDbUriFileName, ioe);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(mongoDbUri));
        }

        return Optional.ofNullable(mongoDbUri);
    }

    /// Fetch from the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void fetchIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final FetchIndex fetchIndex = FetchIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        fetchIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// List the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void listIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final ListIndex listIndex = ListIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        listIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// List the indexes.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void listIndexes(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final ListIndexes listIndexes = ListIndexes.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        listIndexes.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// List the namespaces.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void listNamespaces(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final ListNamespaces listNamespaces = ListNamespaces.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        listNamespaces.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Create the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void createIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final CreateIndex createIndex = CreateIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        createIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Describe the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void describeIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final DescribeIndex describeIndex = DescribeIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        describeIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Describe the models.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void describeModels(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final DescribeModels describeModels = DescribeModels.builder()
                .pinecone(pinecone)
                .build();

        describeModels.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Describe the namespace.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void describeNamespace(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final DescribeNamespace describeNamespace = DescribeNamespace.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        describeNamespace.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Delete the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void deleteIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final DeleteIndex deleteIndex = DeleteIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .build();

        deleteIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Load the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  mongoClient com.mongodb.client.MongoClient
    private void loadIndex(final Pinecone pinecone, final MongoClient mongoClient) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, mongoClient));
        }

        final LoadIndex loadIndex = LoadIndex.builder()
            .pinecone(pinecone)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .embeddingModel(this.embeddingModel)
            .mongoClient(mongoClient)
            .collectionName(this.mongoDbCollection)
            .dbName(this.mongoDbName)
            .build();

        loadIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Query the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  mongoClient com.mongodb.client.MongoClient
    private void queryIndex(final Pinecone pinecone, final MongoClient mongoClient) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, mongoClient));
        }

        final QueryIndex queryIndex = QueryIndex.builder()
            .pinecone(pinecone)
            .embeddingModel(this.embeddingModel)
            .indexName(this.indexName)
            .namespace(this.namespace)
            .rerankingModel(this.rerankingModel)
            .queryText(this.queryText)
            .openAiApiKey(this.openAiApiKey)
            .mongoClient(mongoClient)
            .collectionName(this.mongoDbCollection)
            .dbName(this.mongoDbName)
            .build();

        queryIndex.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Store the unstructured text.
    ///
    /// @param  mongoClient io.mongodb.client.MongoClient
    private void storeUnstructuredText(final MongoClient mongoClient) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(mongoClient));
        }

        final StoreUnstructuredText storeUnstructuredText = StoreUnstructuredText.builder()
            .mongoClient(mongoClient)
            .collectionName(this.mongoDbCollection)
            .dbName(this.mongoDbName)
            .build();

        storeUnstructuredText.operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    static class Builder {
        /// The embedding model.
        private String embeddingModel;

        /// The index name.
        private String indexName;

        /// The MongoDb collection.
        private String mongoDbCollection;

        /// The MongoDb name.
        private String mongoDbName;

        /// The MongoDb URI file name.
        private String mongoDbUriFile;

        /// The namespace.
        private String namespace;

        /// The reranking model.
        private String rerankingModel;

        /// The query text.
        private String queryText;

        /// The default constructor.
        Builder() {
            super();
        }

        /// Set the embedding model.
        ///
        /// @param  embeddingModel  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder embeddingModel(final String embeddingModel) {
            this.embeddingModel = embeddingModel;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the MongoDb collection.
        ///
        /// @param  mongoDbCollection   java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder mongoDbCollection(final String mongoDbCollection) {
            this.mongoDbCollection = mongoDbCollection;

            return this;
        }

        /// Set the MongoDb name.
        ///
        /// @param  mongoDbName java.lang.String
        /// @return             net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder mongoDbName(final String mongoDbName) {
            this.mongoDbName = mongoDbName;

            return this;
        }

        /// Set the MongoDb URI.
        ///
        /// @param  mongoDbUriFile  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder mongoDbUriFile(final String mongoDbUriFile) {
            this.mongoDbUriFile = mongoDbUriFile;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the reranking model.
        ///
        /// @param  rerankingModel  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.Quickstart.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Build the quickstart object.
        ///
        /// @return net.jmp.pinecone.quickstart.Quickstart
        public Quickstart build() {
            return new Quickstart(this);
        }
    }
}
