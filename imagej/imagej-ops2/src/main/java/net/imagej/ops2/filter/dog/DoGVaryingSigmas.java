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

package net.imagej.ops2.filter.dog;

import java.util.concurrent.ExecutorService;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import org.scijava.function.Computers;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.Optional;

/**
 * Difference of Gaussians (DoG) implementation where sigmas can vary by
 * dimension.
 * 
 * @author Christian Dietz (University of Konstanz)
 * @param <T>
 *@implNote op names='filter.DoG'
 */
public class DoGVaryingSigmas<T extends NumericType<T> & NativeType<T>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, double[], double[], ExecutorService, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> {

	@OpDependency(name = "filter.gauss")
	public Computers.Arity4<RandomAccessibleInterval<T>, ExecutorService, double[], //
			OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> defaultGaussRA;

	@OpDependency(name = "filter.DoG")
	private Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, //
			Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> dogOp;

	//TODO: make the outOfBoundsFactory optional (see DoGTest for the default).
	/**
	 * TODO
	 *
	 * @param t
	 * @param sigmas1
	 * @param sigmas2
	 * @param fac
	 * @param es
	 * @param output
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> t, final double[] sigmas1, //
			final double[] sigmas2, final ExecutorService es, //
			@Optional OutOfBoundsFactory<T, RandomAccessibleInterval<T>> fac, //
			final RandomAccessibleInterval<T> output) {
		if (sigmas1.length != sigmas2.length || sigmas1.length != t.numDimensions())
			throw new IllegalArgumentException("Do not have enough sigmas to apply to each dimension of the input!");

		if (fac == null)
			fac = new OutOfBoundsMirrorFactory<>(OutOfBoundsMirrorFactory.Boundary.SINGLE);

		final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> oobf = fac;
		Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss1 = (in, out) -> defaultGaussRA
				.compute(in, es, sigmas1, oobf, out);
		Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> gauss2 = (in, out) -> defaultGaussRA
				.compute(in, es, sigmas2, oobf, out);

		dogOp.compute(t, gauss1, gauss2, output);
	}

}

/**
 *@implNote op names='filter.DoG'
 */
class DoGSingleSigma<T extends NumericType<T> & NativeType<T>> implements
		Computers.Arity5<RandomAccessibleInterval<T>, Double, Double, ExecutorService, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> {

	@OpDependency(name = "filter.DoG")
	private Computers.Arity5<RandomAccessibleInterval<T>, double[], double[], //
			ExecutorService, OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, RandomAccessibleInterval<T>> dogOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param sigma1
	 * @param sigma2
	 * @param es
	 * @param oobf (required = false)
	 * @param out
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> input, final Double sigma1, final Double sigma2,
			final ExecutorService es, @Optional OutOfBoundsFactory<T, RandomAccessibleInterval<T>> oobf,
			RandomAccessibleInterval<T> out) {
		double[] sigmas1 = new double[input.numDimensions()];
		double[] sigmas2 = new double[input.numDimensions()];
		for (int i = 0; i < input.numDimensions(); i++) {
			sigmas1[i] = sigma1;
			sigmas2[i] = sigma2;
		}

		dogOp.compute(input, sigmas1, sigmas2, es, oobf, out);

	}

}
