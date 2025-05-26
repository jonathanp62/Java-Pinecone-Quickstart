package net.jmp.pinecone.quickstart.query;

/*
 * (#)DocumentFetcher.java  0.2.0   05/26/2025
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
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.Projections;

import java.util.Optional;

import net.jmp.pinecone.quickstart.text.UnstructuredTextDocument;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.bson.conversions.Bson;

import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The document fetcher class.
///
/// @version    0.2.0
/// @since      0.2.0
final class DocumentFetcher {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The MongoDB client.
    private final MongoClient mongoClient;

    /// The collection name.
    private final String collectionName;

    /// The database name.
    private final String dbName;

    /// The constructor.
    ///
    /// @param  mongoClient     com.mongodb.client.MongoClient
    /// @param  collectionName  java.lang.String
    /// @param  dbName          java.lang.String
    DocumentFetcher(final MongoClient mongoClient, final String collectionName, final String dbName) {
        super();

        this.mongoClient = mongoClient;
        this.collectionName = collectionName;
        this.dbName = dbName;
    }

    /// Get a document from MongoDB.
    ///
    /// @param  mongoId java.lang.String
    /// @return         java.util.Optional<net.jmp.pinecone.quickstart.text.UnstructuredTextDocument>
    Optional<UnstructuredTextDocument> getDocument(final String mongoId) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(mongoId));
        }

        final MongoDatabase database = this.mongoClient.getDatabase(this.dbName);
        final MongoCollection<Document> collection = database.getCollection(this.collectionName);

        final Bson projectionFields = Projections.fields(
                Projections.include("id", "content", "category")
        );

        final Document mongoDocument = collection
                .find(eq(new ObjectId(mongoId)))
                .projection(projectionFields)
                .first();

        UnstructuredTextDocument document = null;

        if (mongoDocument != null) {
            document = new UnstructuredTextDocument(
                    mongoId,
                    mongoDocument.get("id").toString(),
                    mongoDocument.get("content").toString(),
                    mongoDocument.get("category").toString()
            );
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(document));
        }

        return Optional.ofNullable(document);
    }
}
