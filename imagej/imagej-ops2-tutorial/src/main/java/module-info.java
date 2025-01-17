/*-
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
module net.imagej.ops2.tutorial {
	exports net.imagej.ops2.tutorial;
	
	// -- Open plugins to scijava-ops, therapi
	opens net.imagej.ops2.tutorial to therapi.runtime.javadoc, org.scijava.ops.engine;

	requires io.scif;
	requires java.scripting;
	requires net.imagej.mesh2;
	requires net.imagej.ops2;
	requires net.imglib2;
	requires net.imglib2.algorithm;
	requires net.imglib2.algorithm.fft2;
	requires net.imglib2.roi;
	requires org.joml;
	requires org.scijava.collections;
	requires org.scijava.function;
	requires org.scijava.meta;
	requires org.scijava.ops.api;
	requires org.scijava.ops.engine;
	requires org.scijava.ops.spi;
	requires org.scijava.parsington;
	requires org.scijava.priority;
	requires org.scijava.progress;
	requires org.scijava.types;
	requires org.scijava;

	// FIXME: these module names derive from filenames and are thus unstable
	requires commons.math3;
	requires ojalgo;
	requires jama;
	requires mines.jtk;

	uses org.scijava.ops.api.features.MatchingRoutine;

	provides org.scijava.ops.spi.OpCollection with
			net.imagej.ops2.tutorial.OpAdaptation,
			net.imagej.ops2.tutorial.OpReduction,
			net.imagej.ops2.tutorial.OpSimplification,
			net.imagej.ops2.tutorial.ReportingProgress,
			net.imagej.ops2.tutorial.WritingOpCollections;
}
