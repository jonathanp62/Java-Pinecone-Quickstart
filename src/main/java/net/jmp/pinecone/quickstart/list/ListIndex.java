package net.jmp.pinecone.quickstart.list;

/*
 * (#)ListIndex.java    0.2.0   05/22/2025
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

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import io.pinecone.proto.ListItem;
import io.pinecone.proto.ListResponse;

import java.util.List;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The list index class.
///
/// @version    0.2.0
/// @since      0.2.0
public final class ListIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.list.ListIndex.Builder
    private ListIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .indexName(builder.indexName)
                .namespace(builder.namespace)
        );
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.list.ListIndex.Builder
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
            this.logger.info("Listing index: {}", this.indexName);

            try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
                final ListResponse response = index.list(this.namespace);
                final List<ListItem> vectors = response.getVectorsList();

                for (final ListItem vector : vectors) {
                    this.logger.info("ID: {}", vector.getId());
                }
            }
        } else {
            this.logger.info("Index does not exist or is not loaded: {}", this.indexName);
        }

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
        public Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    io.pinecone.clients.Pinecone
        /// @return             net.jmp.pinecone.quickstart.list.ListIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.list.ListIndex.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.list.ListIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Build the object.
        ///
        /// @return net.jmp.pinecone.quickstart.list.ListIndex
        public ListIndex build() {
            return new ListIndex(this);
        }
    }
}
