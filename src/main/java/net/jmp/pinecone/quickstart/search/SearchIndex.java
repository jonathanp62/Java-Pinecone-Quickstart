package net.jmp.pinecone.quickstart.search;

/*
 * (#)SearchIndex.java  0.8.0   06/25/2025
 * (#)SearchIndex.java  0.7.0   06/23/2025
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

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import net.jmp.pinecone.quickstart.query.DenseVector;
import net.jmp.pinecone.quickstart.query.QueryVector;

import net.jmp.pinecone.quickstart.text.UnstructuredText;

import org.openapitools.db_data.client.ApiException;

import org.openapitools.db_data.client.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The search index class.
///
/// @version    0.8.0
/// @since      0.7.0
public final class SearchIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The list of fields to be searched within the records.
    private final List<String> fields = List.of("text_segment", "category");

    /// The unstructured text object.
    private final UnstructuredText unstructuredText = new UnstructuredText();

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.search.SearchIndex.Builder
    private SearchIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .chatModel(builder.chatModel)
                .searchableEmbeddingModel(builder.searchableEmbeddingModel)
                .searchableIndexName(builder.searchableIndexName)
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
    /// @return net.jmp.pinecone.quickstart.search.SearchIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    ///
    /// Search by records and probably search
    /// by text most likely require the dense
    /// index to be created using a model name.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        this.searchRecords();
        this.searchRecordsByText();
        this.searchRecordsByVectorId();
        this.searchRecordsByVector();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Search records.
    private void searchRecords() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        try (final Index index = this.pinecone.getIndexConnection(this.searchableIndexName)) {
            final Map<String, String> inputs = Map.of("text", this.queryText);
            final SearchRecordsRequestQuery requestQuery = new SearchRecordsRequestQuery();

            requestQuery.setInputs(inputs);
            requestQuery.setTopK(this.topK);

            try {
                final SearchRecordsResponse response = index.searchRecords(
                        this.namespace,
                        requestQuery,
                        this.fields,
                        null
                );

                final SearchRecordsResponseResult result = response.getResult();
                final List<Hit> hits = result.getHits();

                this.logger.info("Search records found {} hits: ", hits.size());

                for (final Hit hit : hits) {
                    this.logHit(hit);
                    this.logContent(hit);
                }
            } catch (final ApiException e) {
                this.logger.error(catching(e));
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Search records by text.
    private void searchRecordsByText() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        try (final Index index = this.pinecone.getIndexConnection(this.searchableIndexName)) {
            try {
                final SearchRecordsResponse response = index.searchRecordsByText(
                        this.queryText,
                        this.namespace,
                        this.fields,
                        this.topK,
                        null,
                        null
                );

                final SearchRecordsResponseResult result = response.getResult();
                final List<Hit> hits = result.getHits();

                this.logger.info("Search records by text found {} hits: ", hits.size());

                for (final Hit hit : hits) {
                    this.logHit(hit);
                    this.logContent(hit);
                }
            } catch (final ApiException e) {
                this.logger.error(catching(e));
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Search records by vector ID.
    private void searchRecordsByVectorId() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        try (final Index index = this.pinecone.getIndexConnection(this.searchableIndexName)) {
            final SearchRecordsResponse response = index.searchRecordsById(
                    "rec11",
                    this.namespace,
                    this.fields,
                    this.topK,
                    null,
                    null
            );

            final SearchRecordsResponseResult result = response.getResult();
            final List<Hit> hits = result.getHits();

            this.logger.info("Search by vector ID 'rec11' found {} hits: ", hits.size());

            for (final Hit hit : hits) {
                this.logHit(hit);
                this.logContent(hit);
            }
        } catch (final ApiException e) {
            this.logger.error(catching(e));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Search records by vector.
    private void searchRecordsByVector() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final QueryVector queryVector = new QueryVector(this.pinecone, this.searchableEmbeddingModel);
        final DenseVector denseVector = queryVector.queryTextToDenseVector(this.queryText);
        final List<Float> denseVectorValues = denseVector.getDenseValues();
        final SearchRecordsVector searchRecordsVector = new SearchRecordsVector();

        searchRecordsVector.setValues(denseVectorValues);

        try (final Index index = this.pinecone.getIndexConnection(this.searchableIndexName)) {
            final Map<String, Object> filter = Map.of("category", "biology");
            final SearchRecordsResponse response = index.searchRecordsByVector(
                    searchRecordsVector,
                    this.namespace,
                    this.fields,
                    this.topK,
                    filter,
                    null
            );

            final SearchRecordsResponseResult result = response.getResult();
            final List<Hit> hits = result.getHits();

            this.logger.info("Search by vector found {} hits: ", hits.size());

            for (final Hit hit : hits) {
                this.logHit(hit);
                this.logContent(hit);
            }
        } catch (final ApiException e) {
            this.logger.error(catching(e));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Log a hit.
    ///
    /// @param  hit org.openapitools.db_data.client.model.Hit
    private void logHit(final Hit hit) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(hit));
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Score: {}", hit.getScore());
            this.logger.debug("ID   : {}", hit.getId());

            @SuppressWarnings("unchecked") final Map<String, Object> hitFields = (Map<String, Object>) hit.getFields();

            for (final Map.Entry<String, Object> entry : hitFields.entrySet()) {
                this.logger.debug("{}: {}", entry.getKey(), entry.getValue());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Log the content.
    ///
    /// @param  hit org.openapitools.db_data.client.model.Hit
    private void logContent(final Hit hit) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(hit));
        }

        @SuppressWarnings("unchecked")
        final Map<String, Object> hitFields = (Map<String, Object>) hit.getFields();
        final String category = (String) hitFields.getOrDefault("category", "");
        final String content = this.unstructuredText.lookup(hit.getId()).getContent();

        this.logger.info("{}: {}", category, content);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The chat model.
        private String chatModel;

        /// The searchable embedding model.
        private String searchableEmbeddingModel;

        /// The searchable index name.
        private String searchableIndexName;

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

        /// The number of top results to return when searching.
        private int topK;

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone net.jmp.pinecone.Pinecone
        /// @return          net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the chat model.
        ///
        /// @param  chatModel   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder chatModel(final String chatModel) {
            this.chatModel = chatModel;

            return this;
        }

        /// Set the searchable embedding model.
        ///
        /// @param  searchableEmbeddingModel    java.lang.String
        /// @return                             net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder searchableEmbeddingModel(final String searchableEmbeddingModel) {
            this.searchableEmbeddingModel = searchableEmbeddingModel;

            return this;
        }

        /// Set the searchable index name.
        ///
        /// @param  searchableIndexName java.lang.String
        /// @return                     net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder searchableIndexName(final String searchableIndexName) {
            this.searchableIndexName = searchableIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace java.lang.String
        /// @return           net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the re-ranking model.
        ///
        /// @param  rerankingModel java.lang.String
        /// @return                net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder rerankingModel(final String rerankingModel) {
            this.rerankingModel = rerankingModel;

            return this;
        }

        /// Set the query or search text.
        ///
        /// @param  queryText java.lang.String
        /// @return           net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder queryText(final String queryText) {
            this.queryText = queryText;

            return this;
        }

        /// Set the OpenAI API key.
        ///
        /// @param  openAiApiKey java.lang.String
        /// @return              net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder openAiApiKey(final String openAiApiKey) {
            this.openAiApiKey = openAiApiKey;

            return this;
        }

        /// Set the MongoDB client.
        ///
        /// @param  mongoClient com.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the MongoDB collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the MongoDB database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Set the topK value.
        ///
        /// @param  topK    int
        /// @return         net.jmp.pinecone.quickstart.search.SearchIndex.Builder
        public Builder topK(final int topK) {
            this.topK = topK;

            return this;
        }

        /// Build the search index.
        ///
        /// @return  net.jmp.pinecone.quickstart.search.SearchIndex
        public SearchIndex build() {
            return new SearchIndex(this);
        }
    }
}
