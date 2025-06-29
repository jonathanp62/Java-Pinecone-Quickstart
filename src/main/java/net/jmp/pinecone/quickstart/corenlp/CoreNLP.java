package net.jmp.pinecone.quickstart.corenlp;

/*
 * (#)CoreNLP.java  0.9.0   06/27/2025
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
/// @version    0.9.0
/// @since      0.5.0
public final class CoreNLP extends Operation {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The Gettysburg address.
    private final String gettysburgAddress = """
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
                
                President Abraham Lincoln - November 19, 1863
                """;

    /// The average length of an English word.
    private final int averageEnglishWordLength = 5;

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

        /* Set up the pipeline */

        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        /* Perform some basic analysis */

        this.basicAnalysis(pipeline);

        /* Chunk text for embeddings */

        final List<String> strings = this.chunkTextForEmbeddings(pipeline, this.gettysburgAddress, 64);

        this.logger.info("Strings for embeddings: {}", strings.size());

        if (this.logger.isInfoEnabled()) {
            strings.forEach(this.logger::info);
        }

        /* Use the text splitter */

        final TextSplitter textSplitter = TextSplitter.builder()
                .document(this.gettysburgAddress)
                .maxTokens(64)
                .build();

        final List<String> splits = textSplitter.split();

        this.logger.info("Splits for embeddings: {}", splits.size());

        if (this.logger.isInfoEnabled()) {
            splits.forEach(this.logger::info);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Perform some basic analysis.
    ///
    /// @param  pipeline edu.stanford.nlp.pipeline.StanfordCoreNLP
    private void basicAnalysis(final StanfordCoreNLP pipeline) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pipeline));
        }

        final List<String> documents = List.of(
                this.queryText,
                "Famous historical structures and monuments",
                "Tell me about famous persons in history and science",
                "Tell me about the physics of light",
                "The Great Wall of China was built to protect against invasions",
                "The Pyramids of Giza are among the Seven Wonders of the Ancient World.",
                "Albert Einstein developed the theory of relativity."
        );

        documents.forEach(documentText -> this.basicDocumentHandler(pipeline, new CoreDocument(documentText)));

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }
    }

    /// Basic document handler.
    ///
    /// @param  pipeline edu.stanford.nlp.pipeline.StanfordCoreNLP
    /// @param  document edu.stanford.nlp.pipeline.CoreDocument
    private void basicDocumentHandler(final StanfordCoreNLP pipeline, final CoreDocument document) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pipeline, document));
        }

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

    /// Chunk text for embeddings.
    ///
    /// @param  pipeline    edu.stanford.nlp.pipeline.StanfordCoreNLP
    /// @param  text        java.lang.String
    /// @param  maxTokens   int
    /// @return             java.util.List<java.lang.String>
    private List<String> chunkTextForEmbeddings(final StanfordCoreNLP pipeline,
                                                final String text,
                                                final int maxTokens) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(pipeline, text, maxTokens));
        }

        final List<String> strings = new ArrayList<>();
        final String[] paragraphs = text.split("\\R\\R");

        this.logger.debug("Paragraphs: {}", paragraphs.length);

        for (final String paragraph : paragraphs) {
            final CoreDocument document = new CoreDocument(paragraph);

            pipeline.annotate(document);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Paragraph tokens: {}", document.tokens().size());  // This matches total tokens below
            }

            /* Check the paragraph as a whole */

            if (document.tokens().size() <= maxTokens) {
                strings.add(paragraph);

                continue;
            }

            /* Process the paragraph by sentences */

            strings.addAll(this.handleLongParagraph(document, maxTokens));
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Total result strings: {}", strings.size());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }

        return strings;
    }

    /// Handle a long paragraph by breaking it into sentences.
    ///
    /// @param  document    edu.stanford.nlp.pipeline.CoreDocument
    /// @param  maxTokens   int
    /// @return             java.util.List<java.lang.String>
    private List<String> handleLongParagraph(final CoreDocument document, final int maxTokens) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(document, maxTokens));
        }

        final List<String> strings = new ArrayList<>();

        final StringBuilder sentenceBuilder = new StringBuilder(maxTokens * this.averageEnglishWordLength);

        int totalTokens = 0;

        for (final CoreSentence sentence : document.sentences()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Core sentence: {}", sentence.text());
            }

            final int tokensInSentence = sentence.tokensAsStrings().size();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Sentence tokens: {}", tokensInSentence);
                this.logger.debug("Sentence tokens: {}", sentence.tokensAsStrings());
            }

            totalTokens = this.handleSentence(sentence, sentenceBuilder, strings, totalTokens, maxTokens);
        }

        if (!sentenceBuilder.isEmpty()) {
            strings.add(sentenceBuilder.toString());    // Add any remaining sentences to the result strings
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(strings));
        }

        return strings;
    }

    /// Handle a sentence. The updated
    /// total number of tokens is returned.
    ///
    /// @param  sentence        edu.stanford.nlp.trees.CoreSentence
    /// @param  sentenceBuilder java.lang.StringBuilder
    /// @param  strings         java.util.List<java.lang.String>
    /// @param  totalTokens     int
    /// @param  maxTokens       int
    /// @return                 int
    private int handleSentence(final CoreSentence sentence,
                                final StringBuilder sentenceBuilder,
                                final List<String> strings,
                                final int totalTokens,
                                final int maxTokens) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence, sentenceBuilder, strings, totalTokens, maxTokens));
        }

        int countTokens = totalTokens;

        final int tokensInSentence = sentence.tokensAsStrings().size();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Sentence tokens: {}", tokensInSentence);
            this.logger.debug("Sentence tokens: {}", sentence.tokensAsStrings());
        }

        if (tokensInSentence > maxTokens) {
            /* Flush any sentences in the sentence builder to the result strings */

            if (!sentenceBuilder.isEmpty()) {
                strings.add(sentenceBuilder.toString());    // Add to the result strings
                sentenceBuilder.setLength(0);               // Reset the sentence builder
            }

            /* Process the sentence by words */

            strings.addAll(this.handleLongSentence(sentence, maxTokens));
        } else {
            if (totalTokens + tokensInSentence <= maxTokens) {          // Sentence fits
                sentenceBuilder.append(sentence.text()).append(" ");    // Add the sentence

                countTokens += tokensInSentence;
            } else {
                if (!sentenceBuilder.isEmpty()) {                       // Sentence will exceed the token limit
                    strings.add(sentenceBuilder.toString());            // Add to the result strings
                    sentenceBuilder.setLength(0);                       // Reset the sentence builder
                }

                sentenceBuilder.append(sentence.text()).append(" ");    // Add the sentence

                countTokens = tokensInSentence;
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(countTokens));
        }

        return countTokens;
    }

    /// Handle a long sentence by breaking it into words.
    ///
    /// @param  sentence    edu.stanford.nlp.trees.CoreSentence
    /// @param  maxTokens   int
    /// @return             java.util.List<java.lang.String>
    private List<String> handleLongSentence(final CoreSentence sentence, final int maxTokens) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence, maxTokens));
        }

        final List<String> strings = new ArrayList<>();

        /* Process the sentence by words */

        final StringBuilder wordBuilder = new StringBuilder(maxTokens * this.averageEnglishWordLength);

        int wordTokens = 0;

        for (final String word : sentence.tokensAsStrings()) {
            if (wordTokens + 1 <= maxTokens) {
                wordBuilder.append(word).append(" ");

                ++wordTokens;
            } else {
                strings.add(wordBuilder.toString());    // Add to the result strings
                wordBuilder.setLength(0);               // Reset the word builder
                wordBuilder.append(word).append(" ");   // Add the word

                wordTokens = 1;
            }
        }

        strings.add(wordBuilder.toString());    // Add any remaining words to the result strings

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(strings));
        }

        return strings;
    }
}
