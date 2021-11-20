/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
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

/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/

package org.scijava.ops.adapt.lift;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.scijava.function.Computers;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpCollection;
import org.scijava.plugin.Plugin;

/**
 * Tests the adaptation of {@link Computers} running on a type into
 * {@link Computers} running on arrays of that type.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class ComputerToArraysTest extends AbstractTestEnvironment {
	
	/**
	 * @author Gabriel Selzer
	 */
	private class NumericalThing extends AbstractTestEnvironment {

		private int number;

		public NumericalThing() {
			number = -1;
		}

		public NumericalThing(int num) {
			number = num;
		}

		public void setNumber(int newNum) {
			number = newNum;
		}

		public int getNumber() {
			return number;
		}
	}
	
	@OpField(names = "test.liftArrayC")
	public final Computers.Arity0<NumericalThing> alterThing0 = (out) -> {out.setNumber(0);};

	@Test
	public void testComputer0ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input().output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(0 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity1<NumericalThing, NumericalThing> alterThing1 = (in, out) -> {out.setNumber(in.getNumber());};

	@Test
	public void testComputer1ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(1 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity2<NumericalThing, NumericalThing, NumericalThing> alterThing2 = (in1, in2, out) -> {out.setNumber(in1.getNumber() + in2.getNumber());};

	@Test
	public void testComputer2ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(2 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity3<NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing3 = (in1, in2, in3, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber());};

	@Test
	public void testComputer3ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(3 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity4<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing4 = (in1, in2, in3, in4, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber());};

	@Test
	public void testComputer4ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(4 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity5<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing5 = (in1, in2, in3, in4, in5, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber());};

	@Test
	public void testComputer5ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(5 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity6<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing6 = (in1, in2, in3, in4, in5, in6, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber());};

	@Test
	public void testComputer6ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(6 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity7<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing7 = (in1, in2, in3, in4, in5, in6, in7, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber());};

	@Test
	public void testComputer7ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(7 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity8<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing8 = (in1, in2, in3, in4, in5, in6, in7, in8, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber());};

	@Test
	public void testComputer8ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(8 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity9<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing9 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber());};

	@Test
	public void testComputer9ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(9 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity10<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing10 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber());};

	@Test
	public void testComputer10ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(10 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity11<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing11 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber());};

	@Test
	public void testComputer11ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(11 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity12<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing12 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber());};

	@Test
	public void testComputer12ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(12 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity13<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing13 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber());};

	@Test
	public void testComputer13ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(13 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity14<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing14 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber());};

	@Test
	public void testComputer14ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(14 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity15<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing15 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber() + in15.getNumber());};

	@Test
	public void testComputer15ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(15 * i, output[i].getNumber());
		}
	}

	@OpField(names = "test.liftArrayC")
	public final Computers.Arity16<NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing, NumericalThing> alterThing16 = (in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16, out) -> {out.setNumber(in1.getNumber() + in2.getNumber() + in3.getNumber() + in4.getNumber() + in5.getNumber() + in6.getNumber() + in7.getNumber() + in8.getNumber() + in9.getNumber() + in10.getNumber() + in11.getNumber() + in12.getNumber() + in13.getNumber() + in14.getNumber() + in15.getNumber() + in16.getNumber());};

	@Test
	public void testComputer16ToArrays() {
		NumericalThing[] input = {new NumericalThing(0), new NumericalThing(1), new NumericalThing(2)};
		NumericalThing[] output = {new NumericalThing(), new NumericalThing(), new NumericalThing()};
		ops.env().op("test.liftArrayC").input(input, input, input, input, input, input, input, input, input, input, input, input, input, input, input, input).output(output).compute();

		for(int i = 0; i < output.length; i++) {
			Assertions.assertEquals(16 * i, output[i].getNumber());
		}
	}

}
