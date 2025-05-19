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

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import io.pinecone.clients.Pinecone;

import static io.pinecone.commons.IndexInterface.buildUpsertVectorWithUnsignedIndices;

import io.pinecone.proto.ListResponse;
import io.pinecone.proto.UpsertResponse;

import io.pinecone.unsigned_indices_model.VectorWithUnsignedIndices;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

import static net.jmp.util.logging.LoggerUtils.*;

import org.openapitools.db_control.client.model.DeletionProtection;
import org.openapitools.db_control.client.model.IndexList;
import org.openapitools.db_control.client.model.IndexModel;

import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.Embedding;
import org.openapitools.inference.client.model.EmbeddingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The quickstart class.
///
/// @version    0.1.0
/// @since      0.1.0
final class Quickstart {
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
        final boolean deleteIndex = Boolean.parseBoolean(System.getProperty("app.deleteIndex"));
        final Pinecone pinecone = new Pinecone.Builder(apiKey).build();
        final String indexName = "quickstart";
        final String namespace = "quickstart-namespace";

        this.listIndexes(pinecone);

        if (this.createIndex(pinecone, indexName)) {
            this.loadIndex(pinecone, indexName, namespace);
        } else {
            final Index index = pinecone.getIndexConnection(indexName);
            final ListResponse result = index.list(namespace);

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Vectors count: {}", result.getVectorsCount());
            }
        }

        this.describeIndex(pinecone, indexName);

        if (deleteIndex) {
            this.deleteIndex(pinecone, indexName);
        }
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

        if (this.logger.isInfoEnabled()) {
            this.logger.info("Index list: {}", indexList.toJson());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Create the index. Return true
    /// if the index was created.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    /// @return             boolean
    private boolean createIndex(final Pinecone pinecone, final String indexName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName));
        }

        boolean result = false;

        if (!indexExists(pinecone, indexName)) {
            this.logger.info("Creating index: {}", indexName);

            /* Embedding model llama-text-embed-v2 */

            IndexModel indexModel = pinecone.createServerlessIndex(
                    indexName,
                    "cosine",   // cosine, euclidean, dot product
                    1024,              // 1024, 2048, 768, 512, 384
                    "aws",
                    "us-east-1",
                    DeletionProtection.DISABLED,
                    new HashMap<>());   // Tags are key-value pairs that you can use to categorize and identify the index

            /* The tags could have just as easily been included during create */

            indexModel = pinecone.configureServerlessIndex(
                    indexName,
                    DeletionProtection.DISABLED,
                    Map.of("env", "development")
            );

            this.indexStatus(indexModel);

            result = true;
        } else {
            this.logger.info("Index already exists: {}", indexName);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }

    /// Describe the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    private void describeIndex(final Pinecone pinecone, final String indexName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName));
        }

        final IndexModel indexModel = pinecone.describeIndex(indexName);

        if (this.logger.isInfoEnabled()) {
            this.logger.info("Index: {}", indexModel.toJson());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Delete the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    private void deleteIndex(final Pinecone pinecone, final String indexName) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName));
        }

        this.logger.info("Deleting index: {}", indexName);

        pinecone.deleteIndex(indexName);

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

                    this.indexStatus(indexModel);
                    break;
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
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

    /// Load the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    /// @param  namespace   java.lang.String
    private void loadIndex(final Pinecone pinecone, final String indexName, final String namespace) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName, namespace));
        }

        final List<Embedding> embeddings = this.createEmbeddings(pinecone);
        final List<Struct> metadata = this.createMetadata(pinecone);

        assert embeddings.size() == metadata.size();

        final List<VectorWithUnsignedIndices> vectors = new ArrayList<>(embeddings.size());

        for (int i = 0; i < embeddings.size(); i++) {
            final Embedding embedding = embeddings.get(i);
            final Struct metadataStruct = metadata.get(i);

            vectors.add(buildUpsertVectorWithUnsignedIndices("V" + i + 1, embedding.getDenseEmbedding().getValues(), null, null, metadataStruct));
        }

        this.logger.info("Loading index: {}", indexName);

        final Index index = pinecone.getIndexConnection(indexName);

        final UpsertResponse result = index.upsert(vectors, namespace);
        final int upsertedCount = result.getUpsertedCount();
        final int serializedSize = result.getSerializedSize();

        this.logger.info("Upserted {} vectors", upsertedCount);
        this.logger.info("Serialized size: {}", serializedSize);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Create embeddings.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @return             java.util.List<io.pinecone.clients.model.Embedding>
    private List<Embedding> createEmbeddings(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        List<Embedding> embeddingList = new ArrayList<>();

        final Inference client = pinecone.getInferenceClient();
        final UnstructuredText unstructuredText = new UnstructuredText();
        final List<UnstructuredText.Text> textList = unstructuredText.getTextList();
        final List<String> inputs = new ArrayList<>();

        for (final UnstructuredText.Text text : textList) {
            inputs.add(text.getText());
        }

        final String embeddingModel = "llama-text-embed-v2";

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Embedding model: {}", embeddingModel);
            this.logger.debug("Parameters: {}", parameters);
            this.logger.debug("Embedding text: {}", inputs);
        }

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(embeddingModel, parameters, inputs);
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (embeddings != null) {
            embeddingList = embeddings.getData();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Embeddings: {}", embeddings.toJson());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(embeddingList));
        }

        return embeddingList;
    }

    /// Create metadata.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @return             java.util.List<com.google.protobuf.Struct>
    private List<Struct> createMetadata(final Pinecone pinecone) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone));
        }

        List<Struct> metadataList = new ArrayList<>();

        final UnstructuredText unstructuredText = new UnstructuredText();
        final List<UnstructuredText.Text> textList = unstructuredText.getTextList();

        for (final UnstructuredText.Text text : textList) {
            final Struct metadataStruct = Struct.newBuilder()
                    .putFields("id", Value.newBuilder().setStringValue(text.getId()).build())
                    .putFields("category", Value.newBuilder().setStringValue(text.getCategory()).build())
                    .build();

            metadataList.add(metadataStruct);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Metadata: {}", metadataList);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(metadataList));
        }

        return metadataList;
    }
}
