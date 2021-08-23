
package org.scijava.ops.engine.impl;

import com.google.common.graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Log describing each execution of an Op. This class is designed to answer two
 * questions:
 * <ol>
 * <li>Given an {@link Object} output (e.g. a {@code List<String>}), what Op(s)
 * mutated that output?
 * <li>Given an {@link Object} op, what {@link OpInfo}s were utilized to
 * implement that Op's functionality?
 * </ol>
 * The answers to these two questions allow users to produce an entire
 * {@code List<Graph<OpInfo>>}, containing all of the information needed to
 * reproduce any {@link Object} output.
 * <p>
 * Note that SciJava Ops is responsible for logging the returns to <b>any</b>
 * matching calls here, but with some effort the user or other applications
 * could also contribute their algorithms to the history.
 *
 * @author Gabe Selzer
 */
@Plugin(type = Service.class)
public class DefaultOpHistory extends AbstractService implements OpHistory {

	// -- DATA STRCUTURES -- //

	/**
	 * {@link Map} responsible for recording the {@link Graph} of {@link OpInfo}s
	 * involved to produce the result of a particular matching call
	 */
	private final Map<RichOp<?>, InfoChain> dependencyChain = new WeakHashMap<>();

	private final Map<Object, List<RichOp<?>>> mutationMap = new WeakHashMap<>();

	// -- USER API -- //

	/**
	 * Returns the list of executions on {@link Object} {@code o} recorded in the
	 * history
	 * <p>
	 * The list of executions is described by a {@link UUID}, which points to a
	 * particular Op execution chain.
	 * 
	 * @param o the {@link Object} of interest
	 * @return an {@link Iterable} of all executions upon {@code o}
	 */
	@Override
	public List<RichOp<?>> executionsUpon(Object o) {
		if (o.getClass().isPrimitive()) throw new IllegalArgumentException(
			"Cannot determine the executions upon a primitive as they are passed by reference!");
		return mutationMap.get(o);
	}

	@Override
	public InfoChain opExecutionChain(Object op) {
		return dependencyChain.get(op);
	}

	// -- HISTORY MAINTENANCE API -- //

	/**
	 * Logs an Op execution in the history
	 * <p>
	 * TODO: It would be nice if different Objects returned different Objects with
	 * the same hash code would hash differently. For example, if two Ops return a
	 * {@link Double} of the same value, they will appear as the same Object, and
	 * asking for the execution history on either of the {@link Object}s will
	 * suggest that both executions mutated both {@link Object}s. This would
	 * really hamper the simplicity of the implementation, however.
	 * 
	 * @param op the {@link RichOp} being executed
	 * @param output the output of the Op execution
	 */
	@Override
	public void addExecution(RichOp<?> op, Object output) {
		if (!mutationMap.containsKey(output)) updateList(output);
		resolveExecution(op, output);
	}

	@Override
	public void logOp(RichOp<?> op) {
		dependencyChain.put(op, op.infoChain());
	}

	// -- HELPER METHODS -- //

	private void updateList(Object output) {
		synchronized (mutationMap) {
			mutationMap.putIfAbsent(output, new ArrayList<>());
		}
	}

	private synchronized void resolveExecution(RichOp<?> op, Object output) {
		List<RichOp<?>> l = mutationMap.get(output);
		synchronized (l) {
			l.add(op);
		}
	}

	public void resetHistory() {
		mutationMap.clear();
		dependencyChain.clear();
	}

}
