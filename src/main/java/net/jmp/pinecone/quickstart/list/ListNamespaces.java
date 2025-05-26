package net.jmp.pinecone.quickstart.list;

/*
 * (#)ListNamespaces.java   0.2.0   05/22/2025
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

import io.pinecone.proto.DescribeIndexStatsResponse;
import io.pinecone.proto.NamespaceSummary;

import java.util.Map;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The list namespaces class.
///
/// @version    0.2.0
/// @since      0.2.0
public class ListNamespaces extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
    private ListNamespaces(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .indexName(builder.indexName)
                .namespace(builder.namespace)
        );
    }

    /// Return the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        this.logger.info("Listing namespaces for index: {}", this.indexName);

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final DescribeIndexStatsResponse response = index.describeIndexStats();
            final Map<String, NamespaceSummary> namespaces = response.getNamespacesMap();

            for (final Map.Entry<String, NamespaceSummary> namespace : namespaces.entrySet()) {
                this.logger.info("Namespace: {}", namespace.getKey());
            }
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
        /// @return             net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the index name.
        ///
        /// @param  indexName   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public Builder indexName(final String indexName) {
            this.indexName = indexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Return the builder.
        ///
        /// @return net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public ListNamespaces build() {
            return new ListNamespaces(this);
        }
    }
}
