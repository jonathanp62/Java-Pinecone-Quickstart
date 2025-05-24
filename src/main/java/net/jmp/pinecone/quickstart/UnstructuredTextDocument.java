package net.jmp.pinecone.quickstart;

/*
 * (#)UnstructuredTextDocument.java 0.2.0   05/24/2025
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

/// The unstructured text document class.
///
/// @version    0.2.0
/// @since      0.2.0
final class UnstructuredTextDocument {
    /// The MongoDB identifier.
    final String mongoId;

    /// The document identifier.
    final String documentId;

    /// The document content.
    final String content;

    /// The document category.
    final String category;

    /// The constructor.
    ///
    /// @param  mongoId     The MongoDB identifier.
    /// @param  documentId  The document identifier.
    /// @param  content     The document content.
    /// @param  category    The document category.
    UnstructuredTextDocument(final String mongoId,
                             final String documentId,
                             final String content,
                             final String category) {
        super();

        this.mongoId = mongoId;
        this.documentId = documentId;
        this.content = content;
        this.category = category;
    }

    /// Returns the MongoDB identifier.
    ///
    /// @return java.lang.String
    public String getMongoId() {
        return this.mongoId;
    }

    /// Returns the document identifier.
    ///
    /// @return java.lang.String
    public String getDocumentId() {
        return this.documentId;
    }

    /// Returns the document content.
    ///
    /// @return java.lang.String
    public String getContent() {
        return this.content;
    }

    /// Returns the document category.
    ///
    /// @return java.lang.String
    public String getCategory() {
        return this.category;
    }
}
