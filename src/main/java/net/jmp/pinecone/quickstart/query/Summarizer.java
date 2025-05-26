package net.jmp.pinecone.quickstart.query;

/*
 * (#)Summarizer.java   0.2.0   05/26/2025
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

import com.openai.client.OpenAIClient;

import com.openai.client.okhttp.OpenAIOkHttpClient;

import com.openai.models.ChatModel;

import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.util.List;

import static net.jmp.util.logging.LoggerUtils.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/// The summarizer class.
///
/// @version    0.2.0
/// @since      0.2.0
final class Summarizer {
    /// The logger.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    /// The OpenAI API key.
    private final String openAiApiKey;

    /// The query text.
    private final String queryText;

    /// The constructor.
    ///
    /// @param  openAiApiKey    java.lang.String
    /// @param  queryText       java.lang.String
    Summarizer(final String openAiApiKey, final String queryText) {
        super();

        this.openAiApiKey = openAiApiKey;
        this.queryText = queryText;
    }

    /// Generate a summary.
    ///
    /// @param  rankedContent   java.util.List<java.lang.String>
    /// @return                 java.lang.String
    String summarize(final List<String> rankedContent) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(entryWith(rankedContent));
        }

        String response = "";
        OpenAIClient openai = null;

        try {
            openai = OpenAIOkHttpClient.builder()
                    .apiKey(this.openAiApiKey)
                    .build();

            /* Construct the prompt */

            final StringBuilder sb = new StringBuilder("Use the following content to answer the question:\n\n");

            for (final String content : rankedContent) {
                sb.append(content);
                sb.append("\n");
            }

            sb.append("\nQuestion: ").append(this.queryText).append("\n");
            final String prompt = sb.toString();

            this.logger.info("Prompt: {}", prompt);
            this.logger.info("Size  : {}", prompt.length());

            final ChatCompletionCreateParams chatCompletionCreateParams = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4_1)
                    .addUserMessage(prompt)
                    .build();

            /* Send the prompt to OpenAI */

            final ChatCompletion chatCompletion = openai.chat().completions().create(chatCompletionCreateParams);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug(chatCompletion.toString());   // See etc/open-ai-chat-completion.txt
            }

            response = chatCompletion.choices().getFirst().message().content().orElse("No response returned");
        } finally {
            if (openai != null) {
                openai.close();
            }
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace(exitWith(response));
        }

        return response;
    }
}
