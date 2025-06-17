package net.jmp.pinecone.quickstart.query;

/*
 * (#)SparseVector.java 0.6.0   06/17/2025
 * (#)SparseVector.java 0.4.0   06/11/2025
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

import static java.lang.Integer.toUnsignedLong;

import java.util.ArrayList;
import java.util.List;

/// The sparse vector class.
///
/// @version    0.6.0
/// @since      0.4.0
final class SparseVector extends Vector {
    /// The sparse indices.
    private List<Long> sparseIndices = new ArrayList<>();

    /// The default constructor.
    SparseVector() {
        super();
    }

    /// Return the sparse values.
    ///
    /// @return  java.util.List<java.lang.Float>
    List<Float> getSparseValues() {
        return this.getValues();
    }

    /// Set the sparse values.
    ///
    /// @param  sparseValues  java.util.List<java.lang.Float>
    void setSparseValues(final List<Float> sparseValues) {
        this.setValues(sparseValues);
    }

    /// Return the sparse indices.
    ///
    /// @return  java.util.List<java.lang.Long>
    List<Long> getSparseIndices() {
        return this.sparseIndices;
    }

    /// Set the sparse indices.
    ///
    /// @param  sparseIndices  java.util.List<java.lang.Long>
    void setSparseIndices(final List<Long> sparseIndices) {
        this.sparseIndices = sparseIndices;
    }

    /// Set the sparse indices from a list of integers.
    ///
    /// @param  sparseIndices  java.util.List<java.lang.Integer>
    @Deprecated
    void setIntSparseIndices(final List<Integer> sparseIndices) {
        final List<Long> sparseIndicesAsLongs = new ArrayList<>(sparseIndices.size());

        for (Integer sparseIndex : sparseIndices) {
            sparseIndicesAsLongs.add(toUnsignedLong(sparseIndex));
        }

        this.sparseIndices = sparseIndicesAsLongs;
    }
}
