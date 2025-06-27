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

import java.util.*;

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

    /// The constructor.
    ///
    /// @param  queryText   java.lang.String
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

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Document tokens: {}", document.tokens());
            }

            /* Use the second sentence for POS, NER, and constituency and dependency parses */

            for (final CoreSentence sentence : document.sentences()) {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Core sentence: {}", sentence.text());
                }

                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Sentence tokens: {}", sentence.tokensAsStrings());
                }

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

        final List<String> strings = this.tokenizeForEmbeddings(pipeline);

        this.logger.info("Strings for embeddings: {}", strings.size());

        if (this.logger.isDebugEnabled()) {
            strings.forEach(this.logger::debug);
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

    /// Tokenize text for embeddings.
    ///
    /// @param  pipeline edu.stanford.nlp.pipeline.StanfordCoreNLP
    /// @return          java.util.List<java.lang.String>
    private List<String> tokenizeForEmbeddings(final StanfordCoreNLP pipeline) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pipeline));
        }

        final int maxTokens = 128;

        final String text = """
                Four score and seven years ago our fathers brought forth on this continent, a new nation, 
                conceived in liberty, and dedicated to the proposition that all men are created equal.
                
                Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived
                and so dedicated, can long endure. We are met on a great battlefield of that war. We have come
                to dedicate a portion of that field, as a final resting place for those who here gave their
                lives that that nation might live. It is altogether fitting and proper that we should do this.
                
                But in a larger sense, we cannot dedicate - we cannot consecrate - we cannot hallow -
                this ground. The brave men, living and dead, who struggled here, have consecrated it, 
                far above our poor power to add or detract. The world will little note, nor long remember, 
                what we say here, but it can never forget what they did here. It is for us the living,
                rather, to be dedicated here to the unfinished work which they who fought here have thus 
                far so nobly advanced. It is rather for us to be here dedicated to the great task remaining 
                before us - that from these honored dead we take increased devotion to that cause for which
                they gave the last full measure of devotion - that we here highly resolve that these dead
                shall not have died in vain - that this nation, under God, shall have a new birth of
                freedom - and that government of the people, by the people, for the people, shall not
                perish from the earth.
                """;

        final CoreDocument document = new CoreDocument(text);

        pipeline.annotate(document);

        if (this.logger.isTraceEnabled()) {
            this.logger.debug("Document tokens: {}", document.tokens().size());  // This matches total tokens below
        }

        /* Check the document as a whole */

        if (document.tokens().size() <= maxTokens) {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(exitWith(List.of(text)));
            }

            return List.of(text);
        }

        /* Process the document by sentences */

        final List<String> strings = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();

        int totalTokens = 0;

        for (final CoreSentence sentence : document.sentences()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Core sentence: {}", sentence.text());
            }

            final int tokensInSentence = sentence.tokensAsStrings().size();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Sentence tokens: {}", tokensInSentence);
                this.logger.debug("Sentence tokens: {}", sentence.tokensAsStrings());
            }

            if (totalTokens + tokensInSentence <= maxTokens) {  // Sentence fits
                sb.append(sentence.text()); // Add the sentence

                totalTokens += tokensInSentence;
            } else {    // Sentence will exceed the token limit
                strings.add(sb.toString()); // Add to the result strings
                sb.setLength(0);            // Reset the string builder
                sb.append(sentence.text()); // Add the sentence

                totalTokens = tokensInSentence;
            }
        }

        strings.add(sb.toString());         // Add the last sentence to the result strings

        this.logger.info("Total results: {}", strings.size());

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }

        return strings;
    }
}
