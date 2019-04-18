/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.ops.transform;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.plugin.SingletonPlugin;

/**
 * Interface describing an OpTransformer that is able to transform between Ops
 * (the actual Op object and a matching ref).
 * 
 * @author David Kolb
 */
public interface OpTransformer extends SingletonPlugin {

	/**
	 * Version of {@link #transform(OpService, OpRef, Object)} doing some error
	 * handling. All exceptions occurring during transformation will be wrapped
	 * into a {@link OpTransformationException} by the default implementation.
	 * 
	 * @param opService
	 * @param ref
	 * @param src
	 * @return
	 * @throws OpTransformationException
	 */
	default Object transformFailSafe(OpService opService, OpRef ref, Object src) throws OpTransformationException {
		if (srcClass().isAssignableFrom(src.getClass())) {
			try {
				return transform(opService, ref, src);
			} catch (Exception e) {
				throw new OpTransformationException("Exception during Op transformation", e);
			}
		}
		throw new OpTransformationException("Object to transfrom: " + src.getClass().getName()
				+ " is not of required class: " + srcClass().getName());
	}

	/**
	 * Get the (possible raw) class of the source type this transformer operates
	 * on.
	 * 
	 * @return
	 */
	Class<?> srcClass();

	/**
	 * Transforms the specified Op object. If the transformation depends on
	 * other Ops, the given {@link OpService} will be used to retrieve them. The
	 * specified {@link OpRef} will be the ref matching the target Op of this
	 * transformation.
	 * 
	 * @param opService
	 * @param ref
	 * @param src
	 * @return
	 */
	Object transform(OpService opService, OpRef ref, Object src) throws Exception;

	/**
	 * Create and return an {@link OpRef} that matches an Op which can be
	 * transformed into another Op, matching the specified ref, by this
	 * transformer. This method should apply the backwards transformation
	 * compared to {@link #transform(OpService, OpRef, Object)} to the specified
	 * target ref. This is used to inquire about Ops that can be transformed
	 * into the specified target using this transformation.
	 * 
	 * @param targetRef
	 * @return
	 */
	OpRef getRefTransformingTo(OpRef targetRef);

}
