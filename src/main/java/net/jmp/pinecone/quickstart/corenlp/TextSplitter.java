package net.jmp.pinecone.quickstart.corenlp;

/*
 * (#)TextSplitter.java 0.9.0   06/29/2025
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

import java.util.*;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The text splitter class.
///
/// @version    0.9.0
/// @since      0.9.0
public final class TextSplitter {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The document.
    private final String document;

    /// The max tokens.
    private final int maxTokens;

    /// The average length of an English word.
    private static final int AVERAGE_ENGLISH_WORD_LENGTH = 5;

    private static final String SENTENCE_TOKENS = "Sentence tokens: {}";

    /// The core NLP pipeline.
    final StanfordCoreNLP pipeline;

    /// The constructor.
    ///
    /// @param  builder net.jmp.pinecone.quickstart.corenlp.TextSplitter.Builder
    private TextSplitter(final Builder builder) {
        super();

        this.document = builder.document;
        this.maxTokens = builder.maxTokens;

        final Properties props = new Properties();    // Set up pipeline properties

        /* Set the list of annotators to run - The order is significant */

        props.setProperty("annotators", "tokenize,pos,lemma,ner,parse,depparse");

        /* Set up the pipeline */

        this.pipeline = new StanfordCoreNLP(props);
    }

    /// Return an instance of the builder.
    ///
    /// @return net.jmp.pinecone.quickstart.corenlp.TextSplitter.Builder
    public static Builder builder() {
        return new Builder();
    }

    /// Split the document into text strings.
    ///
    /// @return java.util.List<java.lang.String>
    public List<String> split() {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entry());
        }

        final List<String> strings = new ArrayList<>();
        final String[] paragraphs = this.document.split("\\R\\R");

        this.logger.debug("Max tokens: {}", this.maxTokens);
        this.logger.debug("Paragraphs: {}", paragraphs.length);

        for (final String paragraph : paragraphs) {
            final CoreDocument coreDocument = new CoreDocument(paragraph);

            this.pipeline.annotate(coreDocument);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Paragraph tokens: {}", coreDocument.tokens().size());  // This matches total tokens below
            }

            /* Check the paragraph as a whole */

            if (coreDocument.tokens().size() <= this.maxTokens) {
                strings.add(paragraph);

                continue;
            }

            /* Process the paragraph by sentences */

            strings.addAll(this.handleLongParagraph(coreDocument));
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Total split strings: {}", strings.size());
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exit());
        }

        return strings;
    }

    /// Handle a long paragraph by breaking it into sentences.
    ///
    /// @param  coreDocument    edu.stanford.nlp.pipeline.CoreDocument
    /// @return                 java.util.List<java.lang.String>
    private List<String> handleLongParagraph(final CoreDocument coreDocument) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(coreDocument));
        }

        final List<String> strings = new ArrayList<>();

        final StringBuilder sentenceBuilder = new StringBuilder(this.maxTokens * AVERAGE_ENGLISH_WORD_LENGTH);

        int totalTokens = 0;

        for (final CoreSentence sentence : coreDocument.sentences()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Core sentence: {}", sentence.text());
            }

            final int tokensInSentence = sentence.tokensAsStrings().size();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug(SENTENCE_TOKENS, tokensInSentence);
                this.logger.debug(SENTENCE_TOKENS, sentence.tokensAsStrings());
            }

            totalTokens = this.handleSentence(sentence, sentenceBuilder, strings, totalTokens);
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
    /// @return                 int
    private int handleSentence(final CoreSentence sentence,
                               final StringBuilder sentenceBuilder,
                               final List<String> strings,
                               final int totalTokens) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence, sentenceBuilder, strings, totalTokens));
        }

        int countTokens = totalTokens;

        final int tokensInSentence = sentence.tokensAsStrings().size();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug(SENTENCE_TOKENS, tokensInSentence);
            this.logger.debug(SENTENCE_TOKENS, sentence.tokensAsStrings());
        }

        if (tokensInSentence > this.maxTokens) {
            /* Flush any sentences in the sentence builder to the result strings */

            if (!sentenceBuilder.isEmpty()) {
                strings.add(sentenceBuilder.toString());    // Add to the result strings
                sentenceBuilder.setLength(0);               // Reset the sentence builder
            }

            /* Process the sentence by words */

            strings.addAll(this.handleLongSentence(sentence));
        } else {
            if (totalTokens + tokensInSentence <= this.maxTokens) {     // Sentence fits
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
    /// @return             java.util.List<java.lang.String>
    private List<String> handleLongSentence(final CoreSentence sentence) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(sentence));
        }

        final List<String> strings = new ArrayList<>();

        /* Process the sentence by words */

        final StringBuilder wordBuilder = new StringBuilder(this.maxTokens * AVERAGE_ENGLISH_WORD_LENGTH);

        int wordTokens = 0;

        for (final String word : sentence.tokensAsStrings()) {
            if (wordTokens + 1 <= this.maxTokens) {
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

    /// The builder class.
    public static class Builder {
        /// The document.
        private String document;

        /// The max tokens.
        private int maxTokens;

        /// The default constructor.
        private Builder () {
            super();
        }

        /// Set the document.
        ///
        /// @param  document    java.lang.String
        /// @return             net.jmp.pinecone.quickstart.corenlp.TextSplitter.Builder
        public Builder document(final String document) {
            this.document = document;

            return this;
        }

        /// Set the max tokens.
        ///
        /// @param  maxTokens   int
        /// @return             net.jmp.pinecone.quickstart.corenlp.TextSplitter.Builder
        public Builder maxTokens(final int maxTokens) {
            this.maxTokens = maxTokens;

            return this;
        }

        /// Build the text splitter object.
        ///
        /// @return net.jmp.pinecone.quickstart.corenlp.TextSplitter
        public TextSplitter build() {
            return new TextSplitter(this);
        }
    }
}
