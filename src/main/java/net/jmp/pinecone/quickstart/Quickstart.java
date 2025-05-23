package net.jmp.pinecone.quickstart;

/*
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

import io.pinecone.clients.Pinecone;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The quickstart class.
///
/// @version    0.2.0
/// @since      0.1.0
final class Quickstart {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    private final String embeddingModel;

    /// The index name.
    private final String indexName;

    /// The namespace.
    private final String namespace;

    /// The reranking model.
    private final String rerankingModel;

    /// The query text.
    private final String queryText;

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.Quickstart.Builder
    private Quickstart(final Builder builder) {
        super();

        this.embeddingModel = builder.embeddingModel;
        this.indexName = builder.indexName;
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

        final String apiKey = this.getApiKey().orElseThrow(() -> new RuntimeException("Pinecone API key not found"));
        final Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        switch (operation) {
            case "create" -> this.createIndex(pinecone);
            case "delete" -> this.deleteIndex(pinecone);
            case "describe" -> this.describeIndex(pinecone);
            case "describeNamespace" -> this.describeNamespace(pinecone);
            case "fetch" -> this.fetchIndex(pinecone);
            case "list" -> this.listIndex(pinecone);
            case "listIndexes" -> this.listIndexes(pinecone);
            case "listNamespaces" -> this.listNamespaces(pinecone);
            case "load" -> this.loadIndex(pinecone);
            case "query" -> this.queryIndex(pinecone);
            default -> this.logger.error("Unknown operation: {}", operation);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Get the API key.
    ///
    /// @return     java.util.Optional<java.lang.String>
    private Optional<String> getApiKey() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String apiKeyFileName = System.getProperty("app.apiKey");

        String apiKey = null;

        try {
            apiKey = Files.readString(Paths.get(apiKeyFileName)).trim();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Pinecone API key file: {}", apiKeyFileName);
                this.logger.debug("Pinecone API key: {}", apiKey);
            }
        } catch (final IOException ioe) {
            this.logger.error("Unable to read API key file: {}", apiKeyFileName, ioe);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(apiKey));
        }

        return Optional.ofNullable(apiKey);
    }

    /// Fetch from the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void fetchIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        new FetchIndex(pinecone, this.indexName, this.namespace).operate();

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

        new ListIndex(pinecone, this.indexName, this.namespace).operate();

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

        new ListIndexes(pinecone, null, null).operate();

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

        new ListNamespaces(pinecone, this.indexName, null).operate();

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

        new CreateIndex(pinecone, this.indexName, this.namespace).operate();

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

        new DescribeIndex(pinecone, this.indexName, this.namespace).operate();

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

        new DescribeNamespace(pinecone, this.indexName, this.namespace).operate();

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

        new DeleteIndex(pinecone, this.indexName, null).operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Load the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void loadIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        new LoadIndex(
                pinecone,
                this.embeddingModel,
                this.indexName,
                this.namespace
        ).operate();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Query the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void queryIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        new QueryIndex(
                pinecone,
                this.embeddingModel,
                this.indexName,
                this.namespace,
                this.rerankingModel,
                this.queryText
        ).operate();

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
