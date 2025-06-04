package net.jmp.pinecone.quickstart;

/*
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
/// @version    0.4.0
/// @since      0.2.0
public abstract class Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    protected final Pinecone pinecone;

    /// The embedding model.
    protected final String embeddingModel;

    /// The dense index name.
    protected final String indexName;

    /// The sparse index name.
    protected final String indexSparseName;

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

    /// The text map.
    protected final Map<String, UnstructuredText.Text> textMap = new UnstructuredText().getTextMap();

    /// The constructor.
    ///
    /// @param operationBuilder net.jmp.pinecone.quickstart.Operation.OperationBuilder
    protected Operation(final OperationBuilder operationBuilder) {
        super();

        this.pinecone = operationBuilder.pinecone;
        this.embeddingModel = operationBuilder.embeddingModel;
        this.indexName = operationBuilder.indexName;
        this.indexSparseName = operationBuilder.indexSparseName;
        this.namespace = operationBuilder.namespace;
        this.rerankingModel = operationBuilder.rerankingModel;
        this.queryText = operationBuilder.queryText;
        this.openAiApiKey = operationBuilder.openAiApiKey;
        this.mongoClient = operationBuilder.mongoClient;
        this.collectionName = operationBuilder.collectionName;
        this.dbName = operationBuilder.dbName;
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
    protected boolean indexExists() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.denseIndexExists();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Return true if the dense index exists.
    ///
    /// @return boolean
    protected boolean denseIndexExists() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.doesIndexExist(this.indexName);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Return true if the sparse index exists.
    ///
    /// @return boolean
    protected boolean sparseIndexExists() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final boolean result = this.doesIndexExist(this.indexSparseName);

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

    /// Check if the index is loaded.
    ///
    /// @return boolean
    protected boolean isIndexLoaded() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        int vectorsCount = 0;

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final ListResponse response = index.list(this.namespace);

            vectorsCount = response.getVectorsCount();
        }

        if (this.logger.isDebugEnabled()) {
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

        /// The embedding model.
        protected String embeddingModel;

        /// The dense index name.
        protected String indexName;

        /// The sparse index name.
        protected String indexSparseName;

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

        /// Set the embedding model.
        ///
        /// @param  embeddingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder embeddingModel(final String embeddingModel) {
            this.embeddingModel = embeddingModel;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  indexName java.lang.String
        /// @return           net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  indexSparseName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.Operation.OperationBuilder
        public OperationBuilder indexSparseName(final String indexSparseName) {
            this.indexSparseName = indexSparseName;

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
    }
}
