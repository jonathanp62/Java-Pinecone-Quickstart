package net.jmp.pinecone.quickstart;

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

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The list index class.
///
/// @version    0.2.0
/// @since      0.2.0
final class ListIndex extends IndexOperation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    /// @param  namespace   java.lang.String
    ListIndex(final Pinecone pinecone, final String indexName, final String namespace) {
        super(pinecone, indexName, namespace);
    }

    /// The operate method.
    @Override
    protected void operate() {
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
}
