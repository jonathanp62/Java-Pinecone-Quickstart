package net.jmp.pinecone.quickstart.query;

/*
 * (#)DenseVector.java  0.7.0   06/23/2025
 * (#)DenseVector.java  0.4.0   06/11/2025
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

import java.util.List;

/// The dense vector class.
///
/// @version    0.7.0
/// @since      0.4.0
public final class DenseVector extends Vector {
    /// The default constructor.
    public DenseVector() {
        super();
    }

    /// Return the dense values.
    ///
    /// @return  java.util.List<java.lang.Float>
    public List<Float> getDenseValues() {
        return this.getValues();
    }

    /// Set the dense values.
    ///
    /// @param  denseValues  java.util.List<java.lang.Float>
    public void setDenseValues(final List<Float> denseValues) {
        this.setValues(denseValues);
    }
}
