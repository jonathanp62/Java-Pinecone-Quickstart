package net.jmp.pinecone.quickstart.query;

/*
 * (#)QueryDenseIndex.java  0.5.0   06/14/2025
 * (#)QueryDenseIndex.java  0.4.0   06/09/2025
 * (#)QueryDenseIndex.java  0.2.0   05/21/2025
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

/// The query dense index class.
///
/// @version    0.5.0
/// @since      0.2.0
public final class QueryDenseIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
    private QueryDenseIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .chatModel(builder.chatModel)
                .denseEmbeddingModel(builder.denseEmbeddingModel)
                .denseIndexName(builder.denseIndexName)
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
    /// @return net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.doesDenseIndexExist() && this.isDenseIndexLoaded()) {
            List<ScoredVectorWithUnsignedIndices> matches;

            if (this.queryText.startsWith("rec")) {
                matches = this.queryById();
            } else {
                matches = this.queryByVector();
            }

            final Reranker reranker = Reranker.builder()
                .pinecone(this.pinecone)
                .rerankingModel(this.rerankingModel)
                .queryText(this.queryText)
                .mongoClient(this.mongoClient)
                .collectionName(this.collectionName)
                .dbName(this.dbName)
                .topN(this.topK)
                .build();

            final List<String> reranked = reranker.rerank(matches);
            final String question = this.getQuestion(reranked.getFirst());

            final Summarizer summarizer = new Summarizer(this.openAiApiKey, question, this.chatModel);
            final String summary = summarizer.summarize(reranked);

            this.logger.info(summary);
        } else {
            this.logger.info("Dense index does not exist or is not loaded: {}", this.denseIndexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Query the dense index by vector ID.
    ///
    /// @return java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    private List<ScoredVectorWithUnsignedIndices> queryById() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Query query = Query.builder()
                .pinecone(this.pinecone)
                .indexName(this.denseIndexName)
                .namespace(this.namespace)
                .mongoClient(this.mongoClient)
                .collectionName(this.collectionName)
                .dbName(this.dbName)
                .topK(this.topK)
                .build();

        final List<ScoredVectorWithUnsignedIndices> matches = query.queryById(this.queryText);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
    }

    /// Query the dense index by vector.
    ///
    /// @return java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    List<ScoredVectorWithUnsignedIndices> queryByVector() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final QueryVector queryVector = new QueryVector(this.pinecone, this.denseEmbeddingModel);
        final DenseVector denseVector = queryVector.queryTextToDenseVector(this.queryText);

        final Query query = Query.builder()
                .pinecone(this.pinecone)
                .indexName(this.denseIndexName)
                .namespace(this.namespace)
                .mongoClient(this.mongoClient)
                .collectionName(this.collectionName)
                .dbName(this.dbName)
                .topK(this.topK)
                .build();

        final CategoryUtil categoryUtil = new CategoryUtil(this.mongoClient, this.dbName);
        final Set<String> categories = categoryUtil.getCategories(this.queryText);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Categories: {}", categories);
        }

        final List<ScoredVectorWithUnsignedIndices> matches = query.query(denseVector.getDenseValues(), categories);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
    }

    /// Get the content by vector ID.
    ///
    /// @param  vectorId java.lang.String
    /// @return          java.util.Optional<java.lang.String>
    private Optional<String> getContent(final String vectorId) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(vectorId));
        }

        String content = null;

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);
        final Document document = collection.find(new Document("id", vectorId)).first();

        if (document != null && document.containsKey("content")) {
            content = document.get("content").toString();
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(content));
        }

        return Optional.ofNullable(content);
    }

    /// Get the question.
    ///
    /// @param  topRanked   java.lang.String
    /// @return             java.lang.String
    private String getQuestion(final String topRanked) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(topRanked));
        }

        String question;

        if (this.queryText.startsWith("rec")) {
            question = this.getContent(this.queryText).orElse(topRanked);
        } else {
            question = this.queryText;
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(question));
        }

        return question;
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The chat model.
        private String chatModel;

        /// The dense embedding model.
        private String denseEmbeddingModel;

        /// The dense index name.
        private String denseIndexName;

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
        /// @return          net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the chat model.
        ///
        /// @param  chatModel   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder chatModel(final String chatModel) {
            this.chatModel = chatModel;

            return this;
        }

        /// Set the dense embedding model.
        ///
        /// @param  denseEmbeddingModel java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder denseEmbeddingModel(final String denseEmbeddingModel) {
            this.denseEmbeddingModel = denseEmbeddingModel;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  denseIndexName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder denseIndexName(final String denseIndexName) {
            this.denseIndexName = denseIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the re-ranking model.
        ///
        /// @param  rerankingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query text.
        ///
        /// @param  queryText java.lang.String
        /// @return           net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the OpenAI API key.
        ///
        /// @param  openAiApiKey java.lang.String
        /// @return              net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder openAiApiKey(final String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Set the topK value.
        ///
        /// @param  topK    int
        /// @return         net.jmp.pinecone.quickstart.query.QueryDenseIndex.Builder
        public Builder topK(final int topK) {
            this.topK = topK;

            return this;
        }

        /// Build the dense query index.
        ///
        /// @return  net.jmp.pinecone.quickstart.query.QueryDenseIndex
        public QueryDenseIndex build() {
            return new QueryDenseIndex(this);
        }
    }
}
