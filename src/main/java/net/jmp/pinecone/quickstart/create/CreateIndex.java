package net.jmp.pinecone.quickstart.create;

/*
 * (#)CreateIndex.java  0.2.0   05/21/2025
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

import io.pinecone.clients.Pinecone;

import java.util.HashMap;
import java.util.Map;

import net.jmp.pinecone.quickstart.IndexOperation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The create index class.
///
/// @version    0.2.0
/// @since      0.2.0
public final class CreateIndex extends IndexOperation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder     net.jmp.pinecone.quickstart.CreateIndex.Builder
    private CreateIndex(final Builder builder) {
        super(builder.pinecone, builder.indexName, builder.namespace);
    }

    /// Return an instance of the builder class.
    ///
    /// @return net.jmp.pinecone.quickstart.CreateIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (!this.indexExists()) {
            this.logger.info("Creating index: {}", this.indexName);

            /* Embedding model llama-text-embed-v2 */

            this.pinecone.createServerlessIndex(
                    this.indexName,
                    "cosine",   // cosine, euclidean, dot product
                    1024,              // 1024, 2048, 768, 512, 384
                    "aws",
                    "us-east-1",
                    DeletionProtection.DISABLED,
                    new HashMap<>());   // Tags are key-value pairs that you can use to categorize and identify the index

            /* The tags could have just as easily been included during create */

            final IndexModel indexModel = this.pinecone.configureServerlessIndex(
                    this.indexName,
                    DeletionProtection.DISABLED,
                    Map.of("env", "development")
            );

            this.indexStatus(indexModel);
        } else {
            this.logger.info("Index already exists: {}", this.indexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Index status.
    ///
    /// @param  indexModel  org.openapitools.db_control.client.model.IndexModel
    private void indexStatus(final IndexModel indexModel) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(indexModel));
        }

        this.logger.info("Index status: {}", indexModel.getStatus());

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The index name.
        private String indexName;

        /// The namespace.
        private String namespace;

        /// The default constructor.
        private Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    io.pinecone.clients.Pinecone
        /// @return             net.jmp.pinecone.quickstart.CreateIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.CreateIndex.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.CreateIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Build the create index object.
        ///
        /// @return net.jmp.pinecone.quickstart.CreateIndex
        public CreateIndex build() {
            return new CreateIndex(this);
        }
    }
}
