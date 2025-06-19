package net.jmp.pinecone.quickstart.update;

/*
 * (#)UpdateIndex.java  0.6.0   06/19/2025
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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import io.pinecone.clients.Pinecone;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import net.jmp.pinecone.quickstart.Operation;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;
import org.bson.conversions.Bson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The update index class.
///
/// @version    0.6.0
/// @since      0.6.0
public final class UpdateIndex extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
    private UpdateIndex(final Builder builder) {
        super(Operation.operationBuilder()
                .pinecone(builder.pinecone)
                .denseIndexName(builder.denseIndexName)
                .sparseIndexName(builder.sparseIndexName)
                .namespace(builder.namespace)
                .mongoClient(builder.mongoClient)
                .collectionName(builder.collectionName)
                .dbName(builder.dbName)
        );
    }

    /// Return an instance of the builder class.
    ///
    /// @return net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final List<UnstructuredTextDocument> documents = this.getDocuments();

        for (final UnstructuredTextDocument document : documents) {
            final String content = document.getContent();
            final StringTokenizer tokenizer = new StringTokenizer(content, " ");
            final int numberOfWords = tokenizer.countTokens();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("{}: {} words", content, numberOfWords);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Get all the documents from the database.
    ///
    /// @return  java.util.List<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    private List<UnstructuredTextDocument> getDocuments() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final List<UnstructuredTextDocument> documents = new LinkedList<>();

        /* Get the text from the database */

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);

        final Bson projectionFields = Projections.fields(
                Projections.include("id", "content")
        );

        try (final MongoCursor<Document> cursor = collection
                .find()
                .projection(projectionFields)
                .sort(Sorts.ascending("id"))
                .iterator()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("There are {} documents available", cursor.available());
            }

            while (cursor.hasNext()) {
                final Document document = cursor.next();
                final String mongoId = document.get("_id").toString();
                final String documentId = document.get("id").toString();
                final String content = document.get("content").toString();

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("MongoId : {}", mongoId);
                    this.logger.debug("DocId   : {}", documentId);
                    this.logger.debug("Content : {}", content);
                }

                documents.add(new UnstructuredTextDocument(mongoId, documentId, content, null));
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(documents));
        }

        return documents;
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

        /// The mongo client.
        private MongoClient mongoClient;

        /// The collection name.
        private String collectionName;

        /// The database name.
        private String dbName;

        /// The default constructor.
        private Builder() {
            super();
        }

        /// Set the Pinecone client.
        ///
        /// @param  pinecone    io.pinecone.clients.Pinecone
        /// @return             net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder pinecone(final Pinecone pinecone) {
            this.pinecone = pinecone;

            return this;
        }

        /// Set the dense index name.
        ///
        /// @param  denseIndexName  java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder denseIndexName(final String denseIndexName) {
            this.denseIndexName = denseIndexName;

            return this;
        }

        /// Set the sparse index name.
        ///
        /// @param  sparseIndexName java.lang.String
        /// @return                 net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder sparseIndexName(final String sparseIndexName) {
            this.sparseIndexName = sparseIndexName;

            return this;
        }

        /// Set the namespace.
        ///
        /// @param  namespace   java.lang.String
        /// @return             net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder namespace(final String namespace) {
            this.namespace = namespace;

            return this;
        }

        /// Set the mongo client.
        ///
        /// @param  mongoClient io.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.update.UpdateIndex.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the update index object.
        ///
        /// @return net.jmp.pinecone.quickstart.update.UpdateIndex
        public UpdateIndex build() {
            return new UpdateIndex(this);
        }
    }
}
