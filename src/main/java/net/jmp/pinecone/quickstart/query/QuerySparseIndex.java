package net.jmp.pinecone.quickstart.query;

/*
 * (#)QuerySparseIndex.java 0.4.0   06/10/2025
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

import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.*;

import net.jmp.pinecone.quickstart.Operation;

import net.jmp.pinecone.quickstart.corenlp.NLPUtil;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query sparse index class.
///
/// @version    0.4.0
/// @since      0.4.0
public final class QuerySparseIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
    private QuerySparseIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .sparseEmbeddingModel(builder.sparseEmbeddingModel)
                .sparseIndexName(builder.sparseIndexName)
                .namespace(builder.namespace)
                .rerankingModel(builder.rerankingModel)
                .queryText(builder.queryText)
                .openAiApiKey(builder.openAiApiKey)
                .mongoClient(builder.mongoClient)
                .collectionName(builder.collectionName)
                .dbName(builder.dbName)
                .topK(builder.topK)
        );
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.doesSparseIndexExist() && this.isSparseIndexLoaded()) {
            final String significantWords = NLPUtil.getSignificantWordsAsString(this.queryText);
            final QueryVector queryVector = new QueryVector(this.pinecone, this.sparseEmbeddingModel);
            final SparseVector sparseVector = queryVector.queryTextToSparseVector(significantWords);

            if (!sparseVector.getSparseValues().isEmpty() && !sparseVector.getSparseIndices().isEmpty()) {
                final Query query = Query.builder()
                        .pinecone(this.pinecone)
                        .indexName(this.sparseIndexName)
                        .topK(this.topK)
                        .namespace(this.namespace)
                        .mongoClient(this.mongoClient)
                        .collectionName(this.collectionName)
                        .dbName(this.dbName)
                        .build();

                final CategoryUtil categoryUtil = new CategoryUtil(this.mongoClient, this.dbName);
                final Set<String> categories = categoryUtil.getCategories(this.queryText);

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Categories: {}", categories);
                }

                final List<ScoredVectorWithUnsignedIndices> matches = query.query(
                        sparseVector.getSparseIndices(),
                        sparseVector.getSparseValues(),
                        categories);

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Matches: {}", matches);
                }
            } else {
                this.logger.error("The sparse embeddings are empty");
            }
        } else {
            this.logger.info("Sparse index does not exist or is not loaded: {}", this.sparseIndexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone cliebt.
        private Pinecone pinecone;

        /// The sparse embedding model.
        private String sparseEmbeddingModel;

        /// The sparse index name.
        private String sparseIndexName;

        /// The namespace.
        private String namespace;

        /// The re-ranking model.
        private String rerankingModel;

        /// The query text.
        private String queryText;

        /// The OpenAI API key.
        private String openAiApiKey;

        /// The MongoDB client.
        private MongoClient mongoClient;

        /// The MongoDB collection name.
        private String collectionName;

        /// The MongoDB database name.
        private String dbName;

        /// The number of top results to return when querying.
        private int topK;

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone net.jmp.pinecone.Pinecone
        /// @return          net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the sparse embedding model.
        ///
        /// @param  sparseEmbeddingModel    java.lang.String
        /// @return                         net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder sparseEmbeddingModel(final String sparseEmbeddingModel) {
            this.sparseEmbeddingModel = sparseEmbeddingModel;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  sparseIndexName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder sparseIndexName(final String sparseIndexName) {
            this.sparseIndexName = sparseIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the re-ranking model.
        ///
        /// @param  rerankingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the OpenAI API key.
        ///
        /// @param  openAiApiKey java.lang.String
        /// @return              net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder openAiApiKey(final String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Set the topK value.
        ///
        /// @param  topK    int
        /// @return         net.jmp.pinecone.quickstart.query.QuerySparseIndex.Builder
        public Builder topK(final int topK) {
            this.topK = topK;

            return this;
        }

        /// Build the query sparse index.
        ///
        /// @return  net.jmp.pinecone.quickstart.query.QuerySparseIndex
        public QuerySparseIndex build() {
            return new QuerySparseIndex(this);
        }
    }
}
