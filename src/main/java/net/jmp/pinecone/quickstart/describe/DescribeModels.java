package net.jmp.pinecone.quickstart.describe;

/*
 * (#)DescribeModels.java   0.3.0   05/27/2025
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

import java.util.List;
import java.util.Map;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.db_control.client.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The describe namespace class.
///
/// @version    0.3.0
/// @since      0.3.0
public class DescribeModels extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.describe.DescribeModels.Builder
    private DescribeModels(final Builder builder) {
        super(Operation.operationBuilder().pinecone(builder.pinecone));
    }

    /// Return an instance of the builder class.
    ///
    /// @return net.jmp.pinecone.quickstart.describe.DescribeModels.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final IndexList indexList = this.pinecone.listIndexes();
        final List<IndexModel> indexModels = indexList.getIndexes();

        if (indexModels != null) {
            for (final IndexModel indexModel : indexModels) {
                this.describeIndex(indexModel);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Describe the index.
    ///
    /// @param  indexModel  org.openapitools.db_control.client.model.IndexModel
    private void describeIndex(final IndexModel indexModel) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        try (final Index index = this.pinecone.getIndexConnection(indexModel.getName())) {
            final DescribeIndexStatsResponse response = index.describeIndexStats();

            this.logger.info("Index name         : {}", indexModel.getName());
            this.logger.info("Index fullness     : {}", response.getIndexFullness());
            this.logger.info("Total vector count : {}", response.getTotalVectorCount());
            this.logger.info("Is initialized     : {}", response.isInitialized());
            this.logger.info("Host               : {}", indexModel.getHost());
            this.logger.info("Deletion protection: {}", indexModel.getDeletionProtection());
            this.logger.info("Dimensions         : {}", indexModel.getDimension());
            this.logger.info("Metric             : {}", indexModel.getMetric());
            this.logger.info("Vector type        : {}", indexModel.getVectorType());

            final Map<String, NamespaceSummary> namespaces = response.getNamespacesMap();

            if (namespaces != null) {
                for (final String namespace : namespaces.keySet()) {
                    this.logger.info("Namespace          : {}", namespace);
                }
            }

            final Map<String, String> tags = indexModel.getTags();

            if (tags != null) {
                for (final Map.Entry<String, String> tag : tags.entrySet()) {
                    this.logger.info("Tag:               : {}:{}", tag.getKey(), tag.getValue());
                }
            }

            final IndexModelSpec spec = indexModel.getSpec();

            if (spec.getServerless() != null) {
                this.logger.info("Serverless cloud   : {}", spec.getServerless().getCloud());
                this.logger.info("Serverless region  : {}", spec.getServerless().getRegion());
            }

            final IndexModelStatus status = indexModel.getStatus();

            this.logger.info("Status Ready       : {}", status.getReady());
            this.logger.info("Status State       : {}", status.getState());
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
        /// @return             net.jmp.pinecone.quickstart.describe.DescribeModels.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Build the object.
        ///
        /// @return net.jmp.pinecone.quickstart.describe.DescribeModels
        public DescribeModels build() {
            return new DescribeModels(this);
        }
    }
}
