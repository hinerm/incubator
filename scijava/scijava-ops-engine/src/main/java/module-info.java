module org.scijava.ops.engine {

/*
 * This is autogenerated source code -- DO NOT EDIT. Instead, edit the
 * corresponding template in templates/ and rerun bin/generate.groovy.
 */


	exports org.scijava.ops.engine;
	exports org.scijava.ops.engine.conversionLoss;
	exports org.scijava.ops.engine.util;

	opens org.scijava.ops.engine.util.internal to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.monitor to therapi.runtime.javadoc;
	opens org.scijava.ops.engine to therapi.runtime.javadoc, org.scijava;
	opens org.scijava.ops.engine.create to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.matcher.impl to therapi.runtime.javadoc, org.scijava;
	opens org.scijava.ops.engine.conversionLoss to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.copy to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.log to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.simplify to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.impl to therapi.runtime.javadoc, org.scijava;
	opens org.scijava.ops.engine.conversionLoss.impl to therapi.runtime.javadoc, org.scijava;
	opens org.scijava.ops.engine.adapt.complexLift to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.adapt.lift to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.struct to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.adapt.functional to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.hint to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.stats to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.util to therapi.runtime.javadoc;
	opens org.scijava.ops.engine.math to therapi.runtime.javadoc;

	requires java.compiler;

	requires org.scijava;
	requires org.scijava.discovery;
	requires org.scijava.discovery.plugin;
	requires org.scijava.discovery.therapi;
	requires org.scijava.function;
	requires org.scijava.progress;
	requires org.scijava.struct;
	requires transitive org.scijava.ops.api;
	requires org.scijava.ops.serviceloader;
	requires org.scijava.ops.spi;
	requires org.scijava.types;

	requires javassist;

	requires therapi.runtime.javadoc;

	uses javax.annotation.processing.Processor;
	provides org.scijava.ops.spi.OpCollection with org.scijava.ops.engine.copy.CopyOpCollection;
	provides org.scijava.ops.spi.Op with org.scijava.ops.engine.stats.Mean.MeanFunction;
}
