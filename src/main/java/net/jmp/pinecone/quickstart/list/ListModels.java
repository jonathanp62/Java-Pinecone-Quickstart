package net.jmp.pinecone.quickstart.list;

/*
 * (#)ListModels.java   0.6.0   06/20/2025
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

import java.util.List;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.inference.client.ApiException;

import org.openapitools.inference.client.model.ModelInfo;
import org.openapitools.inference.client.model.ModelInfoList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The list models class.
///
/// @version    0.6.0
/// @since      0.6.0
public final class ListModels extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.list.ListModels.Builder
    private ListModels(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
        );
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.list.ListModels.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String embed = "embed";
        final Inference inferenceClient = this.pinecone.getInferenceClient();

        try {
            final ModelInfoList allModels = inferenceClient.listModels();
            final ModelInfoList rerankModels = inferenceClient.listModels("rerank");
            final ModelInfoList embeddingModels = inferenceClient.listModels(embed);
            final ModelInfoList denseEmbeddingModels = inferenceClient.listModels(embed, "dense");
            final ModelInfoList sparseEmbeddingModels = inferenceClient.listModels(embed, "sparse");

            this.listModels(allModels.getModels(), "All models");
            this.listModels(rerankModels.getModels(), "Reranking models");
            this.listModels(embeddingModels.getModels(), "Embedding models");
            this.listModels(denseEmbeddingModels.getModels(), "Dense embedding models");
            this.listModels(sparseEmbeddingModels.getModels(), "Sparse embedding models");

            this.describeModels(inferenceClient, allModels.getModels());
        } catch (final ApiException ae) {
            this.logger.error(catching(ae));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// List the models.
    ///
    /// @param  models  java.util.List<io.pinecone.clients.Inference.ModelInfo>
    /// @param  title   String
    private void listModels(final List<ModelInfo> models, final String title) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(models, title));
        }

        for (final ModelInfo model : models) {
            this.logger.info("{}: {}", title, model.getModel());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Describe the models.
    ///
    /// @param  inferenceClient io.pinecone.clients.Inference
    /// @param  models          java.util.List<io.pinecone.clients.Inference.ModelInfo>
    /// @throws                 org.openapitools.inference.client.ApiException  If an error occurs using the inference client
    private void describeModels(final Inference inferenceClient, final List<ModelInfo> models) throws ApiException {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(inferenceClient, models));
        }

        final ModelInfoList allModels = inferenceClient.listModels();
        final List<ModelInfo> allModelsList = allModels.getModels();

        assert allModelsList != null;

        for (final ModelInfo model : allModelsList) {
            final ModelInfo modelInfo = inferenceClient.describeModel(model.getModel());

            this.logger.info("Name                : {}", modelInfo.getModel());
            this.logger.info("Description         : {}", modelInfo.getShortDescription());
            this.logger.info("Type                : {}", modelInfo.getType());
            this.logger.info("Vector type         : {}", modelInfo.getVectorType());
            this.logger.info("Dimension           : {}", modelInfo.getDefaultDimension());
            this.logger.info("Modality            : {}", modelInfo.getModality());
            this.logger.info("Provider            : {}", modelInfo.getProviderName());
            this.logger.info("Max Sequence Length : {}", modelInfo.getMaxSequenceLength());
            this.logger.info("Max Batch Size      : {}", modelInfo.getMaxBatchSize());
            this.logger.info("Supported Metrics   : {}", modelInfo.getSupportedMetrics());
            this.logger.info("Supported Dimensions: {}", modelInfo.getSupportedDimensions());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The default constructor.
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    io.pinecone.clients.Pinecone
        /// @return             net.jmp.pinecone.quickstart.list.ListModels.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Build the list models.
        ///
        /// @return net.jmp.pinecone.quickstart.list.ListModels
        public ListModels build() {
            return new ListModels(this);
        }
    }
}
