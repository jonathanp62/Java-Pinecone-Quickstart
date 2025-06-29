package net.jmp.pinecone.quickstart.list;

/*
 * (#)ListNamespaces.java   0.4.0   06/09/2025
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
/// @version    0.4.0
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
                .denseIndexName(builder.denseIndexName)
                .sparseIndexName(builder.sparseIndexName)
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

        this.logger.info("Listing namespaces for dense index: {}", this.denseIndexName);

        try (final Index index = this.pinecone.getIndexConnection(this.denseIndexName)) {
            final DescribeIndexStatsResponse response = index.describeIndexStats();
            final Map<String, NamespaceSummary> namespaces = response.getNamespacesMap();

            for (final Map.Entry<String, NamespaceSummary> namespace : namespaces.entrySet()) {
                this.logger.info("Namespace: {}", namespace.getKey());
            }
        }

        this.logger.info("Listing namespaces for sparse index: {}", this.sparseIndexName);

        try (final Index index = this.pinecone.getIndexConnection(this.denseIndexName)) {
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

        /// The dense index name.
        private String denseIndexName;

        /// The sparse index name.
        private String sparseIndexName;

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

        /// Set the dense index name.
        ///
        /// @param  denseIndexName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public Builder denseIndexName(final String denseIndexName) {
            this.denseIndexName = denseIndexName;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  sparseIndexName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.list.ListNamespaces.Builder
        public Builder sparseIndexName(final String sparseIndexName) {
            this.sparseIndexName = sparseIndexName;

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
