
package org.scijava.progress;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A static utility class serving as the interface between progress reporters
 * and progress listeners.
 * 
 * @author Gabriel Selzer
 */
public final class Progress {

	/**
	 * A record of all listeners interested in the progress of a given Object's
	 * executions
	 */
	private static final Map<Object, List<ProgressListener>> progressibleListeners =
		new WeakHashMap<>();

	/**
	 * A record of the progressible {@link Object}s running on each
	 * {@link Thread}.
	 */
	private static final ThreadLocal<ArrayDeque<ProgressibleObject>> progressibleStack =
		new InheritableThreadLocal<>()
		{

			@Override
			protected ArrayDeque<ProgressibleObject> childValue(
				ArrayDeque<ProgressibleObject> parentValue)
		{
				// Child threads should be aware of the Tasks operating on the parent.
				// For example, a progressible Object might wish to parallelize one of
				// its stages; the child threads must know which Task to update
				return parentValue.clone();
			}

			@Override
			protected ArrayDeque<ProgressibleObject> initialValue() {
				return new ArrayDeque<>();
			}
		};

	/**
	 * Records {@link ProgressListener} {@code l} as a callback for progressible
	 * {@link Object} {@code progressible}
	 * 
	 * @param progressible an {@link Object} that reports its progress
	 * @param l a {@link ProgressListener} that would like to know about the
	 *          progress of {@code progressible}
	 */
	public static void addListener(Object progressible, ProgressListener l) {
		if (!progressibleListeners.containsKey(progressible)) {
			createListenerList(progressible);
		}
		addListenerToList(progressible, l);
	}

	private static void addListenerToList(Object progressible,
		ProgressListener l)
	{
		List<ProgressListener> list = progressibleListeners.get(progressible);
		synchronized (list) {
			list.add(l);
		}
	}

	private static synchronized void createListenerList(Object progressible) {
		if (progressibleListeners.containsKey(progressible)) return;
		progressibleListeners.put(progressible, new ArrayList<>());
	}

	/**
	 * Completes the current task on this {@link Thread}'s execution hierarchy,
	 * removing it in the process. This method also takaes care to ping relevant {@link ProgressListener}s.
	 * 
	 * @see Task#complete()
	 */
	public static void complete() {
		// update completed task
		ProgressibleObject completed = progressibleStack.get().pop();
		completed.task().complete();
		// ping relevant listeners
		pingListeners(completed);
		if (progressibleStack.get().peek() != null) {
			pingListeners(progressibleStack.get().peek());
		}
	}

	/**
	 * Creates a new {@link Task} for {@code progressible}. This method makes the
	 * assumption that {@code progressible} is responsible for any calls to
	 * {@link Progress}' progress-reporting API between the time this method is
	 * called and the time when {@link Progress#complete()} is called.
	 * 
	 * @param progressible an {@link Object} that would like to report its progress.
	 */
	public static void register(Object progressible) {
		Task t;
		if (progressibleStack.get().size() == 0) {
			// completely new execution hierarchy
			t = new Task();
		}
		else {
			// part of an existing execution hierarchy
			ProgressibleObject parent = progressibleStack.get().peek();
			t = parent.task().createSubtask();
		}
		progressibleStack.get().push(new ProgressibleObject(progressible, t));
	}

	/**
	 * Activates all callback {@link ProgressListener}s listening for progress
	 * updates on executions of {@code o}
	 * 
	 * @param o an {@link Object} reporting its progress.
	 */
	private static void pingListeners(ProgressibleObject o) {
		List<ProgressListener> list = progressibleListeners.getOrDefault(o.object(),
			Collections.emptyList());
		synchronized (list) {
			list.forEach(l -> l.acknowledgeUpdate(o.task()));
		}
	}

	/**
	 * Returns the currently-executing {@link Task} on this {@link Thread}
	 * 
	 * @return the currently-execution {@link Task}
	 */
	private static Task currentTask() {
		ProgressibleObject o = progressibleStack.get().peek();
		return o.task();
	}

	/**
	 * Updates the progress of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 * 
	 * @see Task#update(long)
	 */
	public static void update() {
		update(1);
	}

	/**
	 * Updates the progress of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 * 
	 * @see Task#update(long)
	 */
	public static void update(long numElements) {
		currentTask().update(numElements);
		pingListeners(progressibleStack.get().peek());
	}

	/**
	 * Sets the status of the current {@link Task}, pinging any interested
	 * {@link ProgressListener}s.
	 * 
	 * @see Task#setStatus(String)
	 */
	public static void setStatus(String status) {
		currentTask().setStatus(status);
		pingListeners(progressibleStack.get().peek());
	}

	/**
	 * Defines the total progress of the current {@link Task}
	 * 
	 * @see Task#defineTotalProgress(int)
	 */
	public static void defineTotalProgress(int numStages) {
		currentTask().defineTotalProgress(numStages);
	}

	/**
	 * Defines the total progress of the current {@link Task}
	 * 
	 * @see Task#defineTotalProgress(int, int)
	 */
	public static void defineTotalProgress(int numStages, int numSubTasks) {
		currentTask().defineTotalProgress(numStages, numSubTasks);
	}

	/**
	 * Defines the number of updates expected by the end of the current stage of
	 * the current {@link Task}
	 * 
	 * @see Task#setStageMax(long)
	 */
	public static void setStageMax(long max) {
		currentTask().setStageMax(max);
	}

	/**
	 * Private constructor designed to prevent instantiation.
	 */
	private Progress() {}

}

/**
 * An {@link Object} that reports its progress
 * 
 * @author Gabriel Selzer
 */
class ProgressibleObject {

	private final Object o;
	private final Task t;

	public ProgressibleObject(Object o, Task t) {
		this.o = o;
		this.t = t;
	}

	public Object object() {
		return o;
	}

	public Task task() {
		return t;
	}

}
