package net.jmp.pinecone.quickstart.corenlp;

/*
 * (#)CoreNLP.java  0.5.0   06/12/2025
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

import edu.stanford.nlp.pipeline.*;

import edu.stanford.nlp.semgraph.SemanticGraph;

import edu.stanford.nlp.trees.Tree;

import java.util.List;
import java.util.Properties;

import net.jmp.pinecone.quickstart.Operation;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The core natural language processing class.
///
/// @version    0.5.0
/// @since      0.5.0
public final class CoreNLP extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The default constructor.
    public CoreNLP(final String queryText) {
        super(Operation.operationBuilder().queryText(queryText));
    }

    /// The operate method.
    @Override
    public void operate() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final Properties props = new Properties();    // Set up pipeline properties

        /* Set the list of annotators to run - The order is significant */

        props.setProperty("annotators", "tokenize,pos,lemma,ner,parse,depparse");

        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        final CoreDocument document = new CoreDocument(this.queryText);   // Create a document object

        pipeline.annotate(document);    // Annotate the document

        /* Use the second sentence for POS, NER, and constituency and dependency parses */

        for (final CoreSentence sentence : document.sentences()) {
            this.logger.info("Core sentence:");
            this.logger.info("{}", sentence.text());

            final List<String> posTags = sentence.posTags();    // List of the part-of-speech tags

            this.logger.info("POS tags:");
            this.logger.info("{}", posTags);

            final List<String> nerTags = sentence.nerTags();  // List of the named-entity-recognition tags

            this.logger.info("NER tags:");
            this.logger.info("{}", nerTags);

            final Tree constituencyParse = sentence.constituencyParse();

            this.logger.info("Constituency parse:");
            this.logger.info("{}", constituencyParse);

            final SemanticGraph dependencyParse = sentence.dependencyParse();

            this.logger.info("Dependency parse:");
            this.logger.info("{}", dependencyParse);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }
    }
}
