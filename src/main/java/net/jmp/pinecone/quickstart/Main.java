package net.jmp.pinecone.quickstart;

/*
 * (#)Main.java 0.2.0   05/22/2025
 * (#)Main.java 0.1.0   05/17/2025
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

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The main application class.
///
/// @version    0.2.0
/// @since      0.1.0
public final class Main implements Runnable {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The default constructor.
    private Main() {
        super();
    }

    /// The run method.
    @Override
    public void run() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final String operation = System.getProperty("app.operation");
        final String embeddingModel = System.getProperty("app.embeddingModel");
        final String indexName = System.getProperty("app.indexName");
        final String mongoDbCollection = System.getProperty("app.mongoDbCollection");
        final String mongoDbName = System.getProperty("app.mongoDbName");
        final String mongoDbUriFile = System.getProperty("app.mongoDbUri");
        final String namespace = System.getProperty("app.namespace");
        final String rerankingModel = System.getProperty("app.rerankingModel");
        final String queryText = System.getProperty("app.queryText");

        this.logger.info("Pinecone Quickstart");

        this.logger.info("Operation         : {}", operation);
        this.logger.info("Embedding Model   : {}", embeddingModel);
        this.logger.info("Index Name        : {}", indexName);
        this.logger.info("MongoDB Collection: {}", mongoDbCollection);
        this.logger.info("MongoDB Name      : {}", mongoDbName);
        this.logger.info("MongoDB URI File  : {}", mongoDbUriFile);
        this.logger.info("Namespace         : {}", namespace);
        this.logger.info("Reranking Model   : {}", rerankingModel);
        this.logger.info("Query Text        : {}", queryText);

        final Quickstart quickstart = Quickstart.builder()
            .embeddingModel(embeddingModel)
            .indexName(indexName)
            .mongoDbCollection(mongoDbCollection)
            .mongoDbName(mongoDbName)
            .mongoDbUriFile(mongoDbUriFile)
            .namespace(namespace)
            .rerankingModel(rerankingModel)
            .queryText(queryText)
            .build();

        quickstart.start(operation);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The main application entry point.
    ///
    /// @param  args    java.lang.String[]
    public static void main(String[] args) {
        new Main().run();
    }
}
