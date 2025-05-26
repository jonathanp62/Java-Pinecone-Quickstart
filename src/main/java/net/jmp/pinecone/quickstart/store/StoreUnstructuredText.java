package net.jmp.pinecone.quickstart.store;

/*
 * (#)StoreUnstructuredText.java    0.2.0   05/24/2025
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

import com.mongodb.MongoBulkWriteException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.result.InsertManyResult;

import java.util.ArrayList;
import java.util.List;

import net.jmp.pinecone.quickstart.Operation;

import net.jmp.pinecone.quickstart.text.UnstructuredText;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The store unstructured text class.
///
/// @version    0.2.0
/// @since      0.2.0
public final class StoreUnstructuredText extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.store.StoreUnstructuredText.Builder
    private StoreUnstructuredText(final Builder builder) {
        super(Operation.operationBuilder()
                .dbName(builder.dbName)
                .collectionName(builder.collectionName)
                .mongoClient(builder.mongoClient)
        );
    }

    /// Return the builder.
    ///
    /// @return  net.jmp.pinecone.quickstart.store.StoreUnstructuredText.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// The store method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);

        final List<Document> documents = new ArrayList<>();
        final UnstructuredText unstructuredText = new UnstructuredText();

        unstructuredText.getTextMap().forEach((key, value) -> {
            final Document document = new Document("id", key).append("content", value.getContent()).append("category", value.getCategory());

            documents.add(document);
        });

        try {
            final InsertManyResult result = collection.insertMany(documents);

            result.getInsertedIds().values()
                    .forEach(id -> this.logger.debug("Inserted document: {}", id.asObjectId().getValue()));
        } catch (final MongoBulkWriteException mbwe) {
            this.logger.error(catching(mbwe));

            mbwe.getWriteResult().getInserts()
                    .forEach(doc -> this.logger.info("Inserted document: {}", doc.getId().asObjectId().getValue()));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The builder class.
    public static class Builder {
        /// The mongo client.
        private MongoClient mongoClient;

        /// The collection name.
        private String collectionName;

        /// The database name.
        private String dbName;

        /// The default constructor.
        public Builder() {
            super();
        }
        /// Set the mongo client.
        ///
        /// @param  mongoClient io.mongodb.client.MongoClient
        /// @return             net.jmp.pinecone.quickstart.store.StoreUnstructuredText.Builder
        public Builder mongoClient(final MongoClient mongoClient) {
            this.mongoClient = mongoClient;

            return this;
        }

        /// Set the collection name.
        ///
        /// @param  collectionName java.lang.String
        /// @return                net.jmp.pinecone.quickstart.store.StoreUnstructuredText.Builder
        public Builder collectionName(final String collectionName) {
            this.collectionName = collectionName;

            return this;
        }

        /// Set the database name.
        ///
        /// @param  dbName java.lang.String
        /// @return        net.jmp.pinecone.quickstart.store.StoreUnstructuredText.Builder
        public Builder dbName(final String dbName) {
            this.dbName = dbName;

            return this;
        }

        /// Build the object.
        ///
        /// @return net.jmp.pinecone.quickstart.StoreUnstructuredText
        public StoreUnstructuredText build() {
            return new StoreUnstructuredText(this);
        }
    }
}
