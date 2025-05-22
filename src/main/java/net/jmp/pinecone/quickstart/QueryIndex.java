package net.jmp.pinecone.quickstart;

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

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;
import org.openapitools.inference.client.model.EmbeddingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query index class.
///
/// @version    0.2.0
/// @since      0.2.0
final class QueryIndex extends IndexOperation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    private final String embeddingModel;

    /// The reranking model.
    private final String rerankingModel;

    /// The query text.
    private final String queryText;

    /// The constructor.
    ///
    /// @param  pinecone        io.pinecone.clients.Pinecone
    /// @param  embeddingModel  java.lang.String
    /// @param  indexName       java.lang.String
    /// @param  namespace       java.lang.String
    /// @param  rerankingModel  java.lang.String
    /// @param  queryText       java.lang.String
    QueryIndex(final Pinecone pinecone,
               final String embeddingModel,
               final String indexName,
               final String namespace,
               final String rerankingModel,
               final String queryText) {
        super(pinecone, indexName, namespace);

        this.embeddingModel = embeddingModel;
        this.rerankingModel = rerankingModel;
        this.queryText = queryText;
    }

    /// The operate method.
    @Override
    protected void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.indexExists() && this.isIndexLoaded()) {
            final List<Float> queryVector = this.queryToVector(this.queryText);
            final List<ScoredVectorWithUnsignedIndices> matches = this.query(queryVector);

            this.rerank(matches);
        } else {
            this.logger.info("Index does not exist or is not loaded: {}", this.indexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Query the index using the query vector.
    ///
    /// @param  queryVector java.util.List<java.lang.Float>
    /// @return             java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    private List<ScoredVectorWithUnsignedIndices> query(final List<Float> queryVector) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryVector.toString()));
        }

        List<ScoredVectorWithUnsignedIndices> matches = new ArrayList<>();

        this.logger.info("Querying index: {}", indexName);

        try (final Index index = this.pinecone.getIndexConnection(indexName)) {
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
                    this.logger.debug("Vector ID : {}", match.getId());
                    this.logger.debug("Score     : {}", match.getScore());
                    this.logger.debug("Category  : {}", fields.get("category").getStringValue());
                }

                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Content ID: {}", fields.get("id").getStringValue());
                    this.logger.info("Content   : {}", this.textMap.get(fields.get("id").getStringValue()).getContent());
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(matches));
        }

        return matches;
    }

    /// Rerank the results.
    ///
    /// @param  matches     java.util.List<io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices>
    private void rerank(final List<ScoredVectorWithUnsignedIndices> matches) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(matches));
        }

        this.logger.info("Reranking model: {}", this.rerankingModel);
        this.logger.info("Reranking results for {} matches", matches.size());

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Convert the query text to a vector.
    ///
    /// @param  query       java.lang.String
    /// @return             java.util.List<java.lang.Float>
    private List<Float> queryToVector(final String query) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(query));
        }

        final Map<String, Object> parameters = new HashMap<>();
        final Inference client = this.pinecone.getInferenceClient();

        List<Float> values = new ArrayList<>();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(this.embeddingModel, parameters, List.of(query));
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (embeddings != null) {
            final List<Embedding> embeddingsList = embeddings.getData();

            assert embeddingsList.size() == 1;

            values = embeddingsList.getFirst().getDenseEmbedding().getValues();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Query: {}: {}", query, embeddings.toJson());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(values));
        }

        return values;
    }
}
