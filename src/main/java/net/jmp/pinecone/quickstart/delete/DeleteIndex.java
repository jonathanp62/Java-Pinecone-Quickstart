package net.jmp.pinecone.quickstart.delete;

/*
 * (#)DeleteIndex.java  0.4.0   06/08/2025
 * (#)DeleteIndex.java  0.2.0   05/21/2025
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

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The delete index class.
///
/// @version    0.4.0
/// @since      0.2.0
public final class DeleteIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
    private DeleteIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .indexName(builder.indexName)
                .indexNameHybrid(builder.indexNameHybrid)
                .namespace(builder.namespace)
        );
    }

    /// Return an instance of the builder class.
    ///
    /// @return net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        if (this.indexExists()) {
            this.logger.info("Deleting dense index: {}", this.indexName);

            this.pinecone.deleteIndex(this.indexName);
        } else {
            this.logger.info("Dense index does not exist: {}", this.indexName);
        }

        if (this.hybridIndexExists()) {
            this.logger.info("Deleting hybrid index: {}", this.indexNameHybrid);

            this.pinecone.deleteIndex(this.indexNameHybrid);
        } else {
            this.logger.info("Hybrid index does not exist: {}", this.indexNameHybrid);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The Pinecone client.
        private Pinecone pinecone;

        /// The dense index name.
        private String indexName;

        /// The hybrid index name.
        private String indexNameHybrid;

        /// The namespace.
        private String namespace;

        /// The default constructor.
        private Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    io.pinecone.clients.Pinecone
        /// @return             net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  indexName   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the hybrid index name.
        ///
        /// @param  indexNameHybrid java.lang.String
        /// @return             net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
        public Builder indexNameHybrid(final String indexNameHybrid) {
            this.indexNameHybrid = indexNameHybrid;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.delete.DeleteIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Build the create index object.
        ///
        /// @return net.jmp.pinecone.quickstart.delete.DeleteIndex
        public DeleteIndex build() {
            return new DeleteIndex(this);
        }
    }
}
