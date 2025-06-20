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

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

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

        final Inference inferenceClient = this.pinecone.getInferenceClient();

        try {
            ModelInfoList models = inferenceClient.listModels();
            this.logger.info("Models: {}", models);

            for (final ModelInfo model : models.getModels()) {
                this.logger.info("Model: {}", model.getModel());
            }

            // list renaking models by filtering with type
            ModelInfoList rerankMmodels = inferenceClient.listModels("rerank");

            for (final ModelInfo model : rerankMmodels.getModels()) {
                this.logger.info("Reranking model: {}", model.getModel());
            }

            // list embedding models by filtering with type
            ModelInfoList embeddingModels = inferenceClient.listModels("embed");

            for (final ModelInfo model : embeddingModels.getModels()) {
                this.logger.info("Embedding model: {}", model.getModel());
            }

            // list dense embedding models by filtering with type and vector type
            ModelInfoList denseEmbeddingModels = inferenceClient.listModels("embed", "dense");

            for (final ModelInfo model : denseEmbeddingModels.getModels()) {
                this.logger.info("Dense embedding model: {}", model.getModel());
            }

            // list sparse embedding models by filtering with type and vector type
            ModelInfoList sparseEmbeddingModels = inferenceClient.listModels("embed", "sparse");

            for (final ModelInfo model : sparseEmbeddingModels.getModels()) {
                this.logger.info("Sparse embedding model: {}", model.getModel());
            }

            // describe a model
            ModelInfo modelInfo = inferenceClient.describeModel("llama-text-embed-v2");
            this.logger.info("Model info for llama-text-embed-v2: {}", modelInfo);

            // Display the model name,
            // type,
            // vector type,
            // default dimension,
            // modality,
            // max sequence length,
            // max batch size,
            // provider name,
            // supported dimensions,
            // supported metrics,
            // description
        } catch (final Exception e) {
            this.logger.error("Failed to list models", e);
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
