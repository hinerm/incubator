
package org.scijava.progress;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests that improper progress reporting results in failure
 *
 * @author Gabriel Selzer
 */
public class ImproperReportingTest {

	/**
	 * A progressible task that tries to update its progress without defining what
	 * that progress means
	 */
	public final Function<Integer, int[]> arrayCreator = (size) -> {
		int[] arr = new int[size];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = 1;
			Progress.update();
		}
		return arr;
	};

	/**
	 * A progressible Task that defines fewer stages than it completes
	 */
	public final Supplier<Integer> tooFewStageTask = () -> {
		Progress.defineTotalProgress(2);
		int totalStages = 3;
		for (int i = 0; i < totalStages; i++) {
			Progress.setStageMax(1);
			Progress.update();
		}
		return totalStages;
	};

	/**
	 * A progressible task that defines more stages than it completes
	 */
	public final Supplier<Integer> tooManyStageTask = () -> {
		Progress.defineTotalProgress(3);
		int totalStages = 2;
		for (int i = 0; i < totalStages; i++) {
			Progress.setStageMax(1);
			Progress.update();
		}
		return totalStages;
	};

	/**
	 * A progressible task that defines fewer subtasks than it runs
	 * 
	 * @param subtask the subtask being run
	 * @return the summation of each subtask
	 */
	public static Integer tooFewSubTaskTask(Function<Integer, Integer> subtask) {
		Progress.defineTotalProgress(0, 2);
		return IntStream.range(0, 3) //
			.map(i -> subtask.apply(4)) //
			.sum();
	}

	/**
	 * A progressible task that defines more subtasks than it runs
	 * 
	 * @param subtask the subtask being run
	 * @return the summation of each subtask
	 */
	public static Integer tooManySubTaskTask(Function<Integer, Integer> subtask) {
		Progress.defineTotalProgress(0, 3);
		return IntStream.range(0, 2) //
			.map(i -> subtask.apply(4)) //
			.sum();
	}

	/**
	 * A testing subtask
	 */
	private final Function<Integer, Integer> subtask = (in) -> in;

	/**
	 * Tests that tasks who update progress without defining total progress result
	 * in a thrown error.
	 */
	@Test
	public void testUpdateWithoutSetMax() {
		Function<Integer, int[]> task = arrayCreator;
		Progress.register(task);
		Assertions.assertThrows(IllegalStateException.class, () -> task.apply(3));
		Progress.complete();
	}

	/**
	 * Tests that tasks who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewStages() {
		Supplier<Integer> task = tooFewStageTask;
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Progress.register(task);
			task.get();
			Progress.complete();
		});
	}

	/**
	 * Tests that tasks who did not complete as many stages as they said they
	 * would results in a thrown error.
	 */
	@Test
	public void testDefineTooManyStages() {
		Supplier<Integer> task = tooManyStageTask;
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Progress.register(task);
			task.get();
			Progress.complete();
		});
	}

	/**
	 * Tests that tasks who updated progress past the defined maximum result in a
	 * thrown error.
	 */
	@Test
	public void testDefineTooFewSubTasks() {
		Supplier<Integer> task = () -> tooFewSubTaskTask(subtask);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Progress.register(task);
			task.get();
			Progress.complete();
		});
	}

	/**
	 * Tests that tasks who did not complete as many subtasks as they said they
	 * would results in a thrown error.
	 */
	@Test
	public void testDefineTooManySubTasks() {
		Supplier<Integer> task = () -> tooManySubTaskTask(subtask);
		Assertions.assertThrows(IllegalStateException.class, () -> {
			Progress.register(task);
			task.get();
			Progress.complete();
		});
	}

}
