/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops2.geom.geom2d;

import java.util.List;
import java.util.function.Function;

import net.imagej.ops2.geom.GeomUtils;
import net.imglib2.RealLocalizable;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;

/**
 * Generic implementation of {@code geom.mainElongation} based on
 * http://www.math.uci.edu/icamp/summer/research_11/park/
 * shape_descriptors_survey.pdf.
 * 
 * @author Tim-Oliver Buchholz, University of Konstanz
 * @implNote op names='geom.mainElongation', label='Geometric (2D): Elongation'
 */
public class DefaultElongation implements Computers.Arity1<Polygon2D, DoubleType> {

	@OpDependency(name = "geom.smallestEnclosingBoundingBox")
	private Function<Polygon2D, Polygon2D> minimumBoundingBoxFunc;

	/**
	 * TODO
	 *
	 * @param input
	 * @param elongation
	 */
	@Override
	public void compute(final Polygon2D input, final DoubleType output) {
		final List<? extends RealLocalizable> minBB = GeomUtils.vertices(minimumBoundingBoxFunc.apply(input));

		final RealLocalizable p1 = minBB.get(0);
		final RealLocalizable p2 = minBB.get(1);
		final RealLocalizable p3 = minBB.get(2);

		double width = Math.sqrt(Math.pow(p1.getDoublePosition(0) - p2.getDoublePosition(0), 2)
				+ Math.pow(p1.getDoublePosition(1) - p2.getDoublePosition(1), 2));
		double length = Math.sqrt(Math.pow(p2.getDoublePosition(0) - p3.getDoublePosition(0), 2)
				+ Math.pow(p2.getDoublePosition(1) - p3.getDoublePosition(1), 2));

		if (width > length) {
			final double tmp = width;
			width = length;
			length = tmp;
		}
		output.set(1d - (width / length));
	}

}
