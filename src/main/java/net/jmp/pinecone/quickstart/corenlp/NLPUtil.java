package net.jmp.pinecone.quickstart.corenlp;

/*
 * (#)NLPUtil.java  0.5.0   06/13/2025
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

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.jmp.util.logging.LoggerUtils.*;

/// The natural language processing utility class.
///
/// @version    0.5.0
/// @since      0.5.0
public final class NLPUtil {
    /// The logger.
    private static final Logger logger = LoggerFactory.getLogger(NLPUtil.class.getName());

    /// The default constructor.
    private NLPUtil() {
        super();
    }

    /// Gets the significant words from the text.
    ///
    /// @param  text    java.lang.String
    /// @return         java.util.Set<java.lang.String> j
    public static Set<String> getSignificantWords(final String text) {
        if (logger.isTraceEnabled()) {
            logger.trace(entryWith(text));
        }

        Set<String> significantWords = new HashSet<>();

        final Properties props = new Properties();    // Set up pipeline properties

        /* Set the list of annotators to run - The order is significant */

        props.setProperty("annotators", "tokenize,pos,lemma,ner,parse,depparse");

        final StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        final CoreDocument document = new CoreDocument(text);   // Create a document object

        pipeline.annotate(document);    // Annotate the document

        /* Use the second sentence for POS, NER, and constituency and dependency parses */

        for (final CoreSentence sentence : document.sentences()) {
            if (logger.isDebugEnabled()) {
                logger.info("Core sentence: {}", sentence.text());
            }

            significantWords.addAll(getNouns(sentence));
            significantWords.addAll(getAdjectives(sentence));

            if (logger.isDebugEnabled()) {
                logger.debug("Significant words: {}", significantWords);
            }

            logger.info("Significant words: {}", significantWords);
        }

        if (logger.isTraceEnabled()) {
            logger.trace(exitWith(significantWords));
        }

        return significantWords;
    }

    /// Get the nouns.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @return          java.util.Set<java.lang.String>
    private static Set<String> getNouns(final CoreSentence sentence) {
        if (logger.isTraceEnabled()) {
            logger.trace(entryWith(sentence));
        }

        final Set<String> nouns = getWordsMatchingPOS(sentence, "NN");

        if (logger.isTraceEnabled()) {
            logger.trace(exitWith(nouns));
        }

        return nouns;
    }

    /// Get the adjectives.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @return          java.util.Set<java.lang.String>
    private static Set<String> getAdjectives(final CoreSentence sentence) {
        if (logger.isTraceEnabled()) {
            logger.trace(entryWith(sentence));
        }

        final Set<String> adjectives = getWordsMatchingPOS(sentence, "JJ");

        if (logger.isTraceEnabled()) {
            logger.trace(exitWith(adjectives));
        }

        return adjectives;
    }

    /// Get the words matching the specified part of speech.
    ///
    /// @param  sentence edu.stanford.nlp.trees.CoreSentence
    /// @param  posTag   java.lang.String
    /// @return          java.util.Set<java.lang.String>
    private static Set<String> getWordsMatchingPOS(final CoreSentence sentence, final String posTag) {
        if (logger.isTraceEnabled()) {
            logger.trace(entryWith(sentence, posTag));
        }

        final Set<String> results = new HashSet<>();

        final List<String> posTags = sentence.posTags();
        final List<String> words = sentence.tokensAsStrings();

        if (logger.isDebugEnabled()) {
            logger.debug("POS tags: {}", posTags);
            logger.debug("Tokens  : {}", sentence.tokensAsStrings());
        }

        for (int i = 0; i < posTags.size(); i++) {
            final String tag = posTags.get(i);

            if (tag.startsWith(posTag)) {
                results.add(words.get(i));
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace(exit());
        }

        return results;
    }
}
