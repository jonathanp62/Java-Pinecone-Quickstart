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

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

import com.openai.models.ChatModel;

import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

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
import org.openapitools.inference.client.model.RankedDocument;
import org.openapitools.inference.client.model.RerankResult;

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

    /// The OpenAI API key.
    private final String openAiApiKey;

    /// The constructor.
    ///
    /// @param  pinecone        io.pinecone.clients.Pinecone
    /// @param  embeddingModel  java.lang.String
    /// @param  indexName       java.lang.String
    /// @param  namespace       java.lang.String
    /// @param  rerankingModel  java.lang.String
    /// @param  queryText       java.lang.String
    /// @param  openAiApiKey    java.lang.String
    QueryIndex(final Pinecone pinecone,
               final String embeddingModel,
               final String indexName,
               final String namespace,
               final String rerankingModel,
               final String queryText,
               final String openAiApiKey) {
        super(pinecone, indexName, namespace);

        this.embeddingModel = embeddingModel;
        this.rerankingModel = rerankingModel;
        this.queryText = queryText;
        this.openAiApiKey = openAiApiKey;
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
            final List<String> reranked = this.rerank(matches);
            final String summary = this.rag(reranked);

            this.logger.info(summary);
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
                    this.logger.debug("Content ID: {}", fields.get("id").getStringValue());
                    this.logger.debug("Content   : {}", this.textMap.get(fields.get("id").getStringValue()).getContent());
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
    /// @return             java.util.List<java.lang.String>
    private List<String> rerank(final List<ScoredVectorWithUnsignedIndices> matches) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(matches));
        }

        this.logger.info("Reranking model: {}", this.rerankingModel);
        this.logger.info("Reranking results for {} matches", matches.size());

        /* Create a list of documents to rerank. */

        final List<Map<String, Object>> documents = new ArrayList<>();

        for (final ScoredVectorWithUnsignedIndices match : matches) {
            final Struct metadata = match.getMetadata();
            final Map<String, Value> fields = metadata.getFieldsMap();

            final Map<String, Object> document = new HashMap<>();

            document.put("id", fields.get("id").getStringValue());
            document.put("category", fields.get("category").getStringValue());
            document.put("content", this.textMap.get(fields.get("id").getStringValue()).getContent());

            documents.add(document);
        }

        /* Rerank the documents based on the content field */

        final List<String> rankFields = List.of("content");

        /* Create the parameters for the reranking model */

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("truncate", "END");

        /* Perform the reranking */

        final Inference inference = this.pinecone.getInferenceClient();

        RerankResult result = null;

        try {
            result = inference.rerank(
                    this.rerankingModel,
                    this.queryText,
                    documents,
                    rankFields, 10,
                    true,
                    parameters
            );
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        List<String> rankedContent = new ArrayList<>();

        if (result != null) {
            final List<RankedDocument> rankedDocuments = result.getData();

            for (final RankedDocument rankedDocument : rankedDocuments) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Document: {}", rankedDocument.toJson());
                }

                final Map<String, Object> document = rankedDocument.getDocument();

                assert document != null;

                final String id = (String) document.get("id");
                final String category = (String) document.get("category");
                final String content = (String) document.get("content");

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("ID      : {}", id);
                    this.logger.debug("Category: {}", category);
                }

                this.logger.info(content);

                rankedContent.add(content);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(rankedContent));
        }

        return rankedContent;
    }

    /// Generate a response.
    ///
    /// @param  rankedContent   java.util.List<java.lang.String>
    /// @return                 java.lang.String
    private String rag(final List<String> rankedContent) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(rankedContent));
        }

        String response = "";
        OpenAIClient openai = null;

        try {
            openai = OpenAIOkHttpClient.builder()
                    .apiKey(this.openAiApiKey)
                    .build();

            /* Construct the prompt */

            final StringBuilder sb = new StringBuilder("Use the following content to answer the question:\n\n");

            for (final String content : rankedContent) {
                sb.append(content);
                sb.append("\n");
            }

            sb.append("\nQuestion: ").append(this.queryText).append("\n");
            final String prompt = sb.toString();

            this.logger.info("Prompt: {}", prompt);
            this.logger.info("Size  : {}", prompt.length());

            final ChatCompletionCreateParams chatCompletionCreateParams = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1)
                    .addUserMessage(prompt)
                    .build();

            /* Send the prompt to OpenAI */
            
            final ChatCompletion chatCompletion = openai.chat().completions().create(chatCompletionCreateParams);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug(chatCompletion.toString());   // See etc/open-ai-chat-completion.txt
            }

            response = chatCompletion.choices().getFirst().message().content().orElse("No response returned");
        } finally {
            if (openai != null) {
                openai.close();
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(response));
        }

        return response;
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
