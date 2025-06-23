package net.jmp.pinecone.quickstart.query;

/*
 * (#)QueryVector.java  0.7.0   06/23/2025
 * (#)QueryVector.java  0.6.0   06/17/2025
 * (#)QueryVector.java  0.4.0   06/11/2025
 * (#)QueryVector.java  0.2.0   05/26/2025
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

import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.inference.client.ApiException;

import org.openapitools.inference.client.model.Embedding;
import org.openapitools.inference.client.model.EmbeddingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The query vector class.
///
/// @version    0.7.0
/// @since      0.2.0
public final class QueryVector {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    private final Pinecone pinecone;

    /// The embedding model.
    private final String embeddingModel;

    /// The constructor.
    ///
    /// @param  pinecone        io.pinecone.clients.Pinecone
    /// @param  embeddingModel  java.lang.String
    public QueryVector(final Pinecone pinecone, final String embeddingModel) {
        super();

        this.pinecone = pinecone;
        this.embeddingModel = embeddingModel;
    }

    /// Convert the query text to a dense vector.
    ///
    /// @param  queryText  java.lang.String
    /// @return            net.jmp.pinecone.quickstart.query.DenseVector
    public DenseVector queryTextToDenseVector(final String queryText) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryText));
        }

        final DenseVector denseVector = new DenseVector();
        final Map<String, Object> parameters = new HashMap<>();
        final Inference client = this.pinecone.getInferenceClient();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(this.embeddingModel, parameters, List.of(queryText));
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (embeddings != null) {
            final List<Embedding> embeddingsList = embeddings.getData();

            assert embeddingsList.size() == 1;

            final List<Float> values = embeddingsList.getFirst().getDenseEmbedding().getValues();

            denseVector.setDenseValues(values);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Query: {}: {}", queryText, embeddings.toJson());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(denseVector));
        }

        return denseVector;
    }

    /// Convert the query text to a sparse vector.
    ///
    /// @param  queryText  java.lang.String
    /// @return            net.jmp.pinecone.quickstart.query.SparseVector
    public SparseVector queryTextToSparseVector(final String queryText) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryText));
        }

        final SparseVector sparseVector = new SparseVector();
        final Map<String, Object> parameters = new HashMap<>();
        final Inference client = this.pinecone.getInferenceClient();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        EmbeddingsList sparseEmbeddings = null;

        try {
            sparseEmbeddings = client.embed(this.embeddingModel, parameters, List.of(queryText));
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (sparseEmbeddings != null) {
            final List<Embedding> embeddingsList = sparseEmbeddings.getData();

            assert embeddingsList.size() == 1;

            final List<Float> sparseValues = embeddingsList.getFirst().getSparseEmbedding().getSparseValues();
            final List<Long> sparseIndices = embeddingsList.getFirst().getSparseEmbedding().getSparseIndices();

            sparseVector.setSparseValues(sparseValues);
            sparseVector.setSparseIndices(sparseIndices);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Query sparse embeddings: {}: {}", queryText, sparseEmbeddings.toJson());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(sparseVector));
        }

        return sparseVector;
    }
}
