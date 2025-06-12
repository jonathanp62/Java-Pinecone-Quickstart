package net.jmp.pinecone.quickstart.query;

/*
 * (#)CategoryUtil.java 0.4.0   06/09/2025
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

import java.util.HashSet;
import java.util.Set;

import static net.jmp.util.logging.LoggerUtils.*;

import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The category utility class.
///
/// @version    0.4.0
/// @since      0.4.0
final class CategoryUtil {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The MongoDB client.
    private final MongoClient mongoClient;

    /// The MongoDB database name.
    private final String mongoDbName;

    /// The constructor.
    ///
    /// @param mongoClient  com.mongodb.client.MongoClient
    /// @param mongoDbName  java.lang.String
    CategoryUtil(final MongoClient mongoClient, final String mongoDbName) {
        super();

        this.mongoClient = mongoClient;
        this.mongoDbName = mongoDbName;
    }

    /// Get any categories found in the query text.
    ///
    /// @param  queryText   java.lang.String
    /// @return             java.util.Set<java.lang.String>
    Set<String> getCategories(final String queryText) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(queryText));
        }

        final Set<String> categories = new HashSet<>();

        String textToSplit;

        if (queryText.endsWith(".")) {
            textToSplit = queryText.substring(0, queryText.length() - 1);
        } else {
            textToSplit = queryText;
        }

        final String[] splits = textToSplit.split(" ");

        for (final String split : splits) {
            if (this.isWordACategory(split)) {
                categories.add(split);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(categories));
        }

        return categories;
    }

    /// Check if the word is a category.
    ///
    /// @param  word    java.lang.String
    /// @return         boolean
    private boolean isWordACategory(final String word) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(word));
        }

        boolean result = false;

        final MongoDatabase database = this.mongoClient.getDatabase(this.mongoDbName);
        final MongoCollection<Document> categoriesCollection = database.getCollection("categories");

        result = categoriesCollection.find(new Document("category", word)).first() != null;

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(result));
        }

        return result;
    }
}
