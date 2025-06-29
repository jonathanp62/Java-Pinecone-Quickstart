package net.jmp.pinecone.quickstart;

/*
 * (#)Operation.java    0.8.0   06/16/2025
 * (#)Operation.java    0.5.0   06/16/2025
 * (#)Operation.java    0.4.0   06/04/2025
 * (#)Operation.java    0.2.0   05/21/2025
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

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import io.pinecone.proto.ListResponse;

import java.util.List;
import java.util.Map;

import net.jmp.pinecone.quickstart.text.UnstructuredText;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///
/// The index operation class.
///
/// @version    0.8.0
/// @since      0.2.0
public abstract class Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    protected final Pinecone pinecone;

    /// The chat model.
    protected final String chatModel;

    /// The dense embedding model.
    protected final String denseEmbeddingModel;

    /// The searchable embedding model.
    protected final String searchableEmbeddingModel;

    /// The sparse embedding model.
    protected final String sparseEmbeddingModel;

    /// The dense index name.
    protected final String denseIndexName;

    /// The searchable index name.
    protected final String searchableIndexName;

    /// The sparse index name.
    protected final String sparseIndexName;

    /// The namespace.
    protected final String namespace;

    /// The reranking model.
    protected final String rerankingModel;

    /// The query text.
    protected final String queryText;

    /// The OpenAI API key.
    protected final String openAiApiKey;

    /// The MongoDB client.
    protected final MongoClient mongoClient;

    /// The MongoDB collection name.
    protected final String collectionName;

    /// The MongoDB database name.
    protected final String dbName;

    /// The number of top results to return when querying.
    protected final int topK;

    /// The text map.
    protected final Map<String, UnstructuredText.Text> textMap = new UnstructuredText().getTextMap();

    /// The constructor.
    ///
    /// @param operationBuilder net.jmp.pinecone.quickstart.Operation.OperationBuilder
    protected Operation(final OperationBuilder operationBuilder) {
        super();

        this.pinecone = operationBuilder.pinecone;
        this.chatModel = operationBuilder.chatModel;
        this.denseEmbeddingModel = operationBuilder.denseEmbeddingModel;
        this.searchableEmbeddingModel = operationBuilder.searchableEmbeddingModel;
        this.sparseEmbeddingModel = operationBuilder.sparseEmbeddingModel;
        this.denseIndexName = operationBuilder.denseIndexName;
        this.searchableIndexName = operationBuilder.searchableIndexName;
        this.sparseIndexName = operationBuilder.sparseIndexName;
        this.namespace = operationBuilder.namespace;
        this.rerankingModel = operationBuilder.rerankingModel;
        this.queryText = operationBuilder.queryText;
        this.openAiApiKey = operationBuilder.openAiApiKey;
        this.mongoClient = operationBuilder.mongoClient;
        this.collectionName = operationBuilder.collectionName;
        this.dbName = operationBuilder.dbName;
        this.topK = operationBuilder.topK;
    }

    /// Return the operation builder.
    ///
    /// @return net.jmp.pinecone.quickstart.Operation.OperationBuilder
    protected static OperationBuilder operationBuilder() {
        return new OperationBuilder();
    }

    /// The operate method.
    public abstract void operate();

    /// Return true if the dense index exists.
    ///
    /// @return boolean
    protected boolean doesDenseIndexExist() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.doesIndexExist(this.denseIndexName);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Return true if the searchable index exists.
    ///
    /// @return boolean
    protected boolean doesSearchableIndexExist() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.doesIndexExist(this.searchableIndexName);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Return true if the sparse index exists.
    ///
    /// @return boolean
    protected boolean doesSparseIndexExist() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.doesIndexExist(this.sparseIndexName);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the named index exists.
    ///
    /// @param  indexName   java.lang.String
    /// @return             boolean
    private boolean doesIndexExist(final String indexName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(indexName));
        }

        boolean result = false;

        final IndexList indexList = this.pinecone.listIndexes();
        final List<IndexModel> indexes = indexList.getIndexes();

        if (indexes != null) {
            for (final IndexModel indexModel : indexes) {
                if (indexModel.getName().equals(indexName)) {
                    result = true;

                    break;
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the dense index is loaded.
    ///
    /// @return boolean
    protected boolean isDenseIndexLoaded() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.isNamedIndexLoaded(this.denseIndexName, this.namespace);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the searchable index is loaded.
    ///
    /// @return boolean
    protected boolean isSearchableIndexLoaded() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.isNamedIndexLoaded(this.searchableIndexName, this.namespace);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the sparse index is loaded.
    ///
    /// @return boolean
    protected boolean isSparseIndexLoaded() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.isNamedIndexLoaded(this.sparseIndexName, this.namespace);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the named index is loaded.
    ///
    /// @param  indexName   java.lang.String
    /// @param  namespace   java.lang.String
    /// @return             boolean
    private boolean isNamedIndexLoaded(final String indexName, final String namespace) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(indexName, namespace));
        }

        int vectorsCount = 0;

        try (final Index index = this.pinecone.getIndexConnection(indexName)) {
            final ListResponse response = index.list(namespace);

            vectorsCount = response.getVectorsCount();
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Index name   : {}", indexName);
            this.logger.debug("Namespace    : {}", namespace);
            this.logger.debug("Vectors count: {}", vectorsCount);
        }

        final boolean result = vectorsCount > 0;

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// The operation builder class.
    protected static class OperationBuilder {
        /// The Pinecone client.
        protected Pinecone pinecone;

        /// The chat model.
        protected String chatModel;

        /// The dense embedding model.
        protected String denseEmbeddingModel;

        /// The searchable embedding model.
        protected String searchableEmbeddingModel;

        /// The sparse embedding model.
        protected String sparseEmbeddingModel;

        /// The dense index name.
        protected String denseIndexName;

        /// The searchable index name.
        protected String searchableIndexName;

        /// The sparse index name.
        protected String sparseIndexName;

        /// The namespace.
        protected String namespace;

        /// The reranking model.
        protected String rerankingModel;

        /// The query text.
        protected String queryText;

        /// The OpenAI API key.
        protected String openAiApiKey;

        /// The MongoDB client.
        protected MongoClient mongoClient;

        /// The MongoDB collection name.
        protected String collectionName;

        /// The MongoDB database name.
        protected String dbName;

        /// The number of top results to return when querying.
        protected int topK;

        /// The default constructor.
        protected OperationBuilder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone net.jmp.pinecone.Pinecone
        /// @return          net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the chat model.
        ///
        /// @param  chatModel   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder chatModel(final String chatModel) {
            this.chatModel = chatModel;

            return this;
        }

        /// Set the dense embedding model.
        ///
        /// @param  denseEmbeddingModel java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder denseEmbeddingModel(final String denseEmbeddingModel) {
            this.denseEmbeddingModel = denseEmbeddingModel;

            return this;
        }

        /// Set the searchable embedding model.
        ///
        /// @param  searchableEmbeddingModel    java.lang.String
        /// @return                             net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder searchableEmbeddingModel(final String searchableEmbeddingModel) {
            this.searchableEmbeddingModel = searchableEmbeddingModel;

            return this;
        }

        /// Set the sparse embedding model.
        ///
        /// @param  sparseEmbeddingModel    java.lang.String
        /// @return                         net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder sparseEmbeddingModel(final String sparseEmbeddingModel) {
            this.sparseEmbeddingModel = sparseEmbeddingModel;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  denseIndexName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder denseIndexName(final String denseIndexName) {
            this.denseIndexName = denseIndexName;

            return this;
        }

        /// Set the searchable index name.
        ///
        /// @param  searchableIndexName java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder searchableIndexName(final String searchableIndexName) {
            this.searchableIndexName = searchableIndexName;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  sparseIndexName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder sparseIndexName(final String sparseIndexName) {
            this.sparseIndexName = sparseIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the reranking model.
        ///
        /// @param  rerankingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText java.lang.String
        /// @return           net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the OpenAI API key.
        ///
        /// @param  openAiApiKey java.lang.String
        /// @return              net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder openAiApiKey(final String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Set the topK value.
        ///
        /// @param  topK    int
        /// @return         net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder topK(final int topK) {
            this.topK = topK;

            return this;
        }
    }
}
