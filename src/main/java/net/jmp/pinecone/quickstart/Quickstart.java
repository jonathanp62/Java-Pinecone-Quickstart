package net.jmp.pinecone.quickstart;

/*
 * (#)Quickstart.java   0.1.0   05/17/2025
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

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The quickstart class.
///
/// @version    0.1.0
/// @since      0.1.0
public final class Quickstart {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The default constructor.
    Quickstart() {
        super();
    }

    /// The start method.
    void start() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String apiKey = this.getApiKey().orElseThrow(() -> new RuntimeException("Pinecone API key not found"));
        final Pinecone pinecone = new Pinecone.Builder(apiKey).build();

        this.listIndexes(pinecone);
        this.createIndex(pinecone);
    }

    /// Get the API key.
    ///
    /// @return     java.util.Optional<java.lang.String>
    private Optional<String> getApiKey() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String apiKeyFileName = System.getProperty("app.apiKey");

        String apiKey = null;

        try {
            apiKey = Files.readString(Paths.get(apiKeyFileName)).trim();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Pinecone API key file: {}", apiKeyFileName);
                this.logger.debug("Pinecone API key: {}", apiKey);
            }
        } catch (final IOException ioe) {
            this.logger.error("Unable to read API key file: {}", apiKeyFileName, ioe);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(apiKey));
        }

        return Optional.ofNullable(apiKey);
    }

    /// List the indexes.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void listIndexes(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final IndexList indexList = pinecone.listIndexes();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Index list: {}", indexList.toJson());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Create the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    private void createIndex(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        final String indexName = "quickstart";

        if (!indexExists(pinecone, indexName)) {
            this.logger.info("Creating index: {}", indexName);

            /* Embedding model llama-text-embed-v2 */

            final IndexModel indexModel = pinecone.createServerlessIndex(
                    indexName,
                    "cosine",   // cosine, euclidean, dot product
                    1024,              // 1024, 2048, 768, 512, 384
                    "aws",
                    "us-east-1",
                    DeletionProtection.DISABLED,
                    new HashMap<>());
        } else {
            this.logger.info("Index already exists: {}", indexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Check if the index exists.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    /// @return             boolean
    private boolean indexExists(final Pinecone pinecone, final String indexName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName));
        }

        boolean result = false;

        final IndexList indexList = pinecone.listIndexes();
        final List<IndexModel> indexes = indexList.getIndexes();

        if (indexes != null) {
            for (final IndexModel indexModel : indexes) {
                if (indexModel.getName().equals(indexName)) {
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
}
