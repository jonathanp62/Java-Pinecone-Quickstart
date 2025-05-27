package net.jmp.pinecone.quickstart.query;

/*
 * (#)QueryIndex.java   0.2.0   05/21/2025
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

import com.mongodb.client.*;

import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.*;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query index class.
///
/// @version    0.2.0
/// @since      0.2.0
public final class QueryIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.query.QueryIndex.Builder
    private QueryIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .embeddingModel(builder.embeddingModel)
                .indexName(builder.indexName)
                .namespace(builder.namespace)
                .rerankingModel(builder.rerankingModel)
                .queryText(builder.queryText)
                .openAiApiKey(builder.openAiApiKey)
                .mongoClient(builder.mongoClient)
                .collectionName(builder.collectionName)
                .dbName(builder.dbName)
        );
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.query.QueryIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.indexExists() && this.isIndexLoaded()) {
            final QueryVector queryVector = new QueryVector(this.pinecone, this.embeddingModel);
            final List<Float> queryVectorList = queryVector.queryTextToVector(this.queryText);

            final Query query = Query.builder()
                .pinecone(this.pinecone)
                .indexName(this.indexName)
                .namespace(this.namespace)
                .mongoClient(this.mongoClient)
                .collectionName(this.collectionName)
                .dbName(this.dbName)
                .build();

            final Set<String> categories = this.getCategories();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Categories: {}", categories);
            }

            final List<ScoredVectorWithUnsignedIndices> matches = query.query(queryVectorList, categories);

            final Reranker reranker = Reranker.builder()
                .pinecone(this.pinecone)
                .rerankingModel(this.rerankingModel)
                .queryText(this.queryText)
                .mongoClient(this.mongoClient)
                .collectionName(this.collectionName)
                .dbName(this.dbName)
                .build();

            final List<String> reranked = reranker.rerank(matches);
            final Summarizer summarizer = new Summarizer(this.openAiApiKey, this.queryText);
            final String summary = summarizer.summarize(reranked);

            this.logger.info(summary);
        } else {
            this.logger.info("Index does not exist or is not loaded: {}", this.indexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Get any categories found in the query text.
    ///
    /// @return java.util.Set<java.lang.String>
    /// @since  0.3.0
    private Set<String> getCategories() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Set<String> categories = new HashSet<>();

        String textToSplit;

        if (this.queryText.endsWith(".")) {
            textToSplit = this.queryText.substring(0, this.queryText.length() - 1);
        } else {
            textToSplit = this.queryText;
        }

        final String[] splits = textToSplit.split(" ");

        for (final String split : splits) {
            if (this.isWordACategory(split)) {
                categories.add(split);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(categories));
        }

        return categories;
    }

    /// Check if the word is a category.
    ///
    /// @param  word    java.lang.String
    /// @return         boolean
    /// @since          0.3.0
    private boolean isWordACategory(final String word) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(word));
        }

        boolean result = false;

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> categoriesCollection = database.getCollection("categories");

        result = categoriesCollection.find(new Document("category", word)).first() != null;

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone cliebt.
        private Pinecone pinecone;

        /// The embedding model.
        private String embeddingModel;

        /// The index name.
        private String indexName;

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

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone net.jmp.pinecone.Pinecone
        /// @return          net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the embedding model.
        ///
        /// @param  embeddingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder embeddingModel(final String embeddingModel) {
            this.embeddingModel = embeddingModel;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the re-ranking model.
        ///
        /// @param  rerankingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the OpenAI API key.
        ///
        /// @param  openAiApiKey java.lang.String
        /// @return              net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder openAiApiKey(final String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.query.QueryIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the query index.
        ///
        /// @return  net.jmp.pinecone.quickstart.query.QueryIndex
        public QueryIndex build() {
            return new QueryIndex(this);
        }
    }
}
