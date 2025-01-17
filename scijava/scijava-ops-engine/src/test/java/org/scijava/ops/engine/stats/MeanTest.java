package org.scijava.ops.engine.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.math.Add;
import org.scijava.ops.engine.math.MathOpCollection;
import org.scijava.types.Nil;

public class MeanTest <N extends Number> extends AbstractTestEnvironment {

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new Size());
		ops.register(new Mean.MeanFunction());
		ops.register(new Add());
		ops.register(new MathOpCollection());
	}

	@Test
	public void regressionTest() {

		Function<Iterable<Integer>, Double> goodFunc = OpBuilder.matchFunction(ops, "stats.mean", new Nil<Iterable<Integer>>() {}, new Nil<Double>() {});

		List<Integer> goodNums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		double expected = 5.5;

		double actual = goodFunc.apply(goodNums);

		assertEquals(expected, actual, 0);
	}

}
