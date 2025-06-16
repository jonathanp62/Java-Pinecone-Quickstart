package net.jmp.pinecone.quickstart;

/*
 * (#)Main.java 0.5.0   06/16/2025
 * (#)Main.java 0.4.0   06/04/2025
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

import ch.qos.logback.classic.Level;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The main application class.
///
/// @version    0.5.0
/// @since      0.1.0
public final class Main implements Runnable {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// Any command line arguments.
    private final String[] args;

    /// The constructor.
    ///
    /// @param  args java.lang.String[]
    private Main(final String[] args) {
        super();

        this.args = args;
    }

    /// The run method.
    @Override
    public void run() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        this.logger.info("Pinecone Quickstart");

        this.handleCommandLineArguments();

        final String operation = System.getProperty("app.operation");

        final String chatModel = System.getProperty("app.chatModel");
        final String denseEmbeddingModel = System.getProperty("app.denseEmbeddingModel");
        final String denseIndexName = System.getProperty("app.denseIndexName");
        final String mongoDbCollection = System.getProperty("app.mongoDbCollection");
        final String mongoDbName = System.getProperty("app.mongoDbName");
        final String mongoDbUriFile = System.getProperty("app.mongoDbUri");
        final String namespace = System.getProperty("app.namespace");
        final String rerankingModel = System.getProperty("app.rerankingModel");
        final String queryText = System.getProperty("app.queryText");
        final String sparseEmbeddingModel = System.getProperty("app.sparseEmbeddingModel");
        final String sparseIndexName = System.getProperty("app.sparseIndexName");
        final String topK = System.getProperty("app.topK");

        this.logger.info("Operation             : {}", operation);
        this.logger.info("Chat Model            : {}", chatModel);
        this.logger.info("Dense Embedding Model : {}", denseEmbeddingModel);
        this.logger.info("Dense Index Name      : {}", denseIndexName);
        this.logger.info("MongoDB Collection    : {}", mongoDbCollection);
        this.logger.info("MongoDB Name          : {}", mongoDbName);
        this.logger.info("MongoDB URI File      : {}", mongoDbUriFile);
        this.logger.info("Namespace             : {}", namespace);
        this.logger.info("Reranking Model       : {}", rerankingModel);
        this.logger.info("Query Text            : {}", queryText);
        this.logger.info("Sparse Embedding Model: {}", sparseEmbeddingModel);
        this.logger.info("Sparse Index Name     : {}", sparseIndexName);
        this.logger.info("TopK                  : {}", topK);

        final Quickstart quickstart = Quickstart.builder()
            .chatModel(chatModel)
            .denseEmbeddingModel(denseEmbeddingModel)
            .sparseEmbeddingModel(sparseEmbeddingModel)
            .denseIndexName(denseIndexName)
            .sparseIndexName(sparseIndexName)
            .mongoDbCollection(mongoDbCollection)
            .mongoDbName(mongoDbName)
            .mongoDbUriFile(mongoDbUriFile)
            .namespace(namespace)
            .rerankingModel(rerankingModel)
            .queryText(queryText)
            .topK(Integer.parseInt(topK))
            .build();

        quickstart.start(operation);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Handle any command line arguments.
    private void handleCommandLineArguments() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        for (final String arg : this.args) {
            this.logger.info("Command line argument : {}", arg);

            switch (arg) {
                case "--log-debug": this.setLogLevel(Level.DEBUG); break;
                case "--log-error": this.setLogLevel(Level.ERROR); break;
                case "--log-info": this.setLogLevel(Level.INFO); break;
                case "--log-off": this.setLogLevel(Level.OFF); break;
                case "--log-trace": this.setLogLevel(Level.TRACE); break;
                case "--log-warn": this.setLogLevel(Level.WARN); break;
                default: throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Set the log level.
    ///
    /// @param  level   ch.qos.logback.classic.Level
    private void setLogLevel(final Level level) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(level));
        }

        final Class<?> clazz = this.getClass();
        final String packageName = clazz.getPackage().getName();
        final Logger packageLogger = LoggerFactory.getLogger(packageName);

        /* Get the Logback logger and change it to the new level */

        ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) packageLogger;

        logbackLogger.setLevel(level);

        this.logger.info("{} level logging enabled for package: {}", level.levelStr, packageName);

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// The main application entry point.
    ///
    /// @param  args    java.lang.String[]
    public static void main(String[] args) {
        new Main(args).run();
    }
}
