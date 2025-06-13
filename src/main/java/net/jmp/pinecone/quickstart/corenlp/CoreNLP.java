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

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

        final List<String> documents = List.of(
            this.queryText,
            "Famous historical structures and monuments",
            "Tell me about famous persons in history and science",
            "Tell me about the physics of light",
            "The Great Wall of China was built to protect against invasions",
            "The Pyramids of Giza are among the Seven Wonders of the Ancient World.",
            "Albert Einstein developed the theory of relativity."
        );

        for (final String documentText : documents) {
            final CoreDocument document = new CoreDocument(documentText);   // Create a document object

            pipeline.annotate(document);    // Annotate the document

            /* Use the second sentence for POS, NER, and constituency and dependency parses */

            for (final CoreSentence sentence : document.sentences()) {
                this.logger.info("Core sentence: {}", sentence.text());

                final Set<String> significantWords = this.getNouns(sentence);

                significantWords.addAll(this.getAdjectives(sentence));

                this.logger.info("Significant words: {}", significantWords);

                if (this.logger.isDebugEnabled()) {
                    final Tree constituencyParse = sentence.constituencyParse();
                    final SemanticGraph dependencyParse = sentence.dependencyParse();

                    this.logger.debug("Constituency parse: {}", constituencyParse);
                    this.logger.debug("Dependency parse: {}", dependencyParse);
                }
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Get the nouns.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @return          java.util.Set<java.lang.String>
    private Set<String> getNouns(final CoreSentence sentence) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence));
        }

        final Set<String> nouns = this.getWordsMatchingPOS(sentence, "NN");

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(nouns));
        }

        return nouns;
    }

    /// Get the adjectives.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @return          java.util.Set<java.lang.String>
    private Set<String> getAdjectives(final CoreSentence sentence) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence));
        }

        final Set<String> adjectives = this.getWordsMatchingPOS(sentence, "JJ");

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(adjectives));
        }

        return adjectives;
    }

    /// Get the words matching the specified part of speech.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @param  posTag   java.lang.String
    /// @return          java.util.Set<java.lang.String>
    private Set<String> getWordsMatchingPOS(final CoreSentence sentence, final String posTag) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence, posTag));
        }

        final Set<String> results = new HashSet<>();

        final List<String> posTags = sentence.posTags();
        final List<String> words = sentence.tokensAsStrings();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("POS tags: {}", posTags);
            this.logger.debug("Tokens  : {}", sentence.tokensAsStrings());
        }

        for (int i = 0; i < posTags.size(); i++) {
            final String tag = posTags.get(i);

            if (tag.startsWith(posTag)) {
                results.add(words.get(i));
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }

        return results;
    }
}
