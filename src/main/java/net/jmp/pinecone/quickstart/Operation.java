package net.jmp.pinecone.quickstart;

/*
 * (#)Operation.java    0.2.0   05/21/2025
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

import com.mongodb.client.MongoClient;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;

import io.pinecone.proto.ListResponse;

import java.util.List;
import java.util.Map;

import net.jmp.pinecone.quickstart.text.UnstructuredText;

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
public abstract class Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Pinecone client.
    protected final Pinecone pinecone;

    /// The embedding model.
    protected final String embeddingModel;

    /// The index name.
    protected final String indexName;

    /// The namespace.
    protected final String namespace;

    /// The reranking model.
    protected final String rerankingModel;

    /// The query text.
    protected final String queryText;

    /// The OpenAI API key.
    protected final String openAiApiKey;

    /// The MongoDB client.
    protected final MongoClient mongoClient;

    /// The MongoDB collection name.
    protected final String collectionName;

    /// The MongoDB database name.
    protected final String dbName;

    /// The text map.
    protected final Map<String, UnstructuredText.Text> textMap = new UnstructuredText().getTextMap();

    /// The constructor.
    ///
    /// @param pinecone         io.pinecone.clients.Pinecone
    /// @param embeddingModel   java.lang.String
    /// @param indexName        java.lang.String
    /// @param namespace        java.lang.String
    /// @param rerankingModel   java.lang.String
    /// @param queryText        java.lang.String
    /// @param openAiApiKey     java.lang.String
    /// @param mongoClient      com.mongodb.client.MongoClient
    /// @param collectionName   java.lang.String
    /// @param dbName           java.lang.String
    protected Operation(final Pinecone pinecone,
                         final String embeddingModel,
                         final String indexName,
                         final String namespace,
                         final String rerankingModel,
                         final String queryText,
                         final String openAiApiKey,
                         final MongoClient mongoClient,
                         final String collectionName,
                         final String dbName) {
        super();

        this.pinecone = pinecone;
        this.embeddingModel = embeddingModel;
        this.indexName = indexName;
        this.namespace = namespace;
        this.rerankingModel = rerankingModel;
        this.queryText = queryText;
        this.openAiApiKey = openAiApiKey;
        this.mongoClient = mongoClient;
        this.collectionName = collectionName;
        this.dbName = dbName;
    }

    /// The operate method.
    public abstract void operate();

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

        int vectorsCount = 0;

        try (final Index index = this.pinecone.getIndexConnection(this.indexName)) {
            final ListResponse response = index.list(this.namespace);

            vectorsCount = response.getVectorsCount();
        }

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
