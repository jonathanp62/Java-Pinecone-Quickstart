package net.jmp.pinecone.quickstart;

/*
 * (#)IndexOperation.java   0.2.0   05/21/2025
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

import io.pinecone.proto.ListResponse;

import java.util.List;
import java.util.Map;

import static net.jmp.util.logging.LoggerUtils.entry;
import static net.jmp.util.logging.LoggerUtils.exitWith;

import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

///
/// The index operation class.
///
/// @version    0.2.0
/// @since      0.2.0
abstract class IndexOperation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    protected static final String EMBEDDING_MODEL = "llama-text-embed-v2";

    /// The Pinecone client.
    protected final Pinecone pinecone;

    /// The index name.
    protected final String indexName;

    /// The namespace.
    protected final String namespace;

    /// The text map.
    protected final Map<String, UnstructuredText.Text> textMap = new UnstructuredText().getTextMap();

    /// The constructor.
    ///
    /// @param pinecone  io.pinecone.clients.Pinecone
    /// @param indexName java.lang.String
    /// @param namespace java.lang.String
    protected IndexOperation(final Pinecone pinecone, final String indexName, final String namespace) {
        super();

        this.pinecone = pinecone;
        this.indexName = indexName;
        this.namespace = namespace;
    }

    /// The operate method.
    protected abstract void operate();

    /// Check if the index exists.
    ///
    /// @return boolean
    protected boolean indexExists() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        boolean result = false;

        final IndexList indexList = this.pinecone.listIndexes();
        final List<IndexModel> indexes = indexList.getIndexes();

        if (indexes != null) {
            for (final IndexModel indexModel : indexes) {
                if (indexModel.getName().equals(this.indexName)) {
                    result = true;

                    break;
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Check if the index is loaded.
    ///
    /// @return boolean
    protected boolean isIndexLoaded() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Index index = this.pinecone.getIndexConnection(this.indexName);
        final ListResponse response = index.list(this.namespace);
        final int vectorsCount = response.getVectorsCount();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Vectors count: {}", vectorsCount);
        }

        final boolean result = vectorsCount > 0;

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }
}
