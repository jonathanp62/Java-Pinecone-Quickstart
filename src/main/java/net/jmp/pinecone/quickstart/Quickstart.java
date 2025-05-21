package net.jmp.pinecone.quickstart;

/*
 * (#)Quickstart.java   0.2.0   05/21/2025
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

import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
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
/// @version    0.2.0
/// @since      0.1.0
final class Quickstart {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The embedding model.
    private static final String EMBEDDING_MODEL = "llama-text-embed-v2";

    /// The text map.
    ///
    /// @since  0.2.0
    final Map<String, UnstructuredText.Text> textMap = new UnstructuredText().getTextMap();

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
        this.queryIndex(pinecone, indexName, namespace);

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
            final String id = metadataStruct.getFieldsMap().get("id").getStringValue();

            vectors.add(buildUpsertVectorWithUnsignedIndices(id, embedding.getDenseEmbedding().getValues(), null, null, metadataStruct));
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
        final List<String> inputs = new ArrayList<>();

        for (final UnstructuredText.Text text : this.textMap.values()) {
            inputs.add(text.getContent());
        }

        /* The parameters relate to the embedding model */

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Embedding model: {}", EMBEDDING_MODEL);
            this.logger.debug("Parameters: {}", parameters);
            this.logger.debug("Embedding text: {}", inputs);
        }

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(EMBEDDING_MODEL, parameters, inputs);
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

        for (final Map.Entry<String, UnstructuredText.Text> entry : this.textMap.entrySet()) {
            final Struct metadataStruct = Struct.newBuilder()
                    .putFields("id", Value.newBuilder().setStringValue(entry.getKey()).build())
                    .putFields("category", Value.newBuilder().setStringValue(entry.getValue().getCategory()).build())
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

    /// Query the index.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  indexName   java.lang.String
    /// @param  namespace   java.lang.String
    private void queryIndex(final Pinecone pinecone, final String indexName, final String namespace) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pinecone, indexName, namespace));
        }

        final String query = "Famous historical structures and monuments";
        final List<Float> queryVector = this.queryToVector(pinecone, query);

        this.logger.info("Querying index: {}", indexName);

        final Index index = pinecone.getIndexConnection(indexName);
        final QueryResponseWithUnsignedIndices queryResponse =
                index.query(10,
                        queryVector,
                        null,
                        null,
                        null,
                        namespace,
                        null,
                        true,
                        true);

        final List<ScoredVectorWithUnsignedIndices> matches = queryResponse.getMatchesList();

        for (final ScoredVectorWithUnsignedIndices match : matches) {
            final Struct metadata = match.getMetadata();
            final Map<String, Value> fields = metadata.getFieldsMap();

            if (this.logger.isInfoEnabled()) {
                this.logger.info("Vector ID : {}", match.getId());
                this.logger.info("Score     : {}", match.getScore());
                this.logger.info("Category  : {}", fields.get("category").getStringValue());
                this.logger.info("Content ID: {}", fields.get("id").getStringValue());
                this.logger.info("Content   : {}", this.textMap.get(fields.get("id").getStringValue()).getContent());
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Query to vector.
    ///
    /// @param  pinecone    io.pinecone.clients.Pinecone
    /// @param  query       java.lang.String
    /// @return             java.util.List<java.lang.Float>
    private List<Float> queryToVector(final Pinecone pinecone, final String query) {
        final Map<String, Object> parameters = new HashMap<>();
        final Inference client = pinecone.getInferenceClient();

        List<Float> values = new ArrayList<>();

        parameters.put("input_type", "query");
        parameters.put("truncate", "END");

        EmbeddingsList embeddings = null;

        try {
            embeddings = client.embed(EMBEDDING_MODEL, parameters, List.of(query));
        } catch (ApiException e) {
            this.logger.error(e.getMessage());
        }

        if (embeddings != null) {
            final List<Embedding> embeddingsList = embeddings.getData();

            assert embeddingsList.size() == 1;

            values = embeddingsList.getFirst().getDenseEmbedding().getValues();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Query: {}: {}", query, embeddings.toJson());
            }
        }

        return values;
    }
}
