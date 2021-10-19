/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2021 SciJava developers.
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

package org.scijava.log2;

import java.io.PrintStream;
import java.util.function.Function;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.Service;

/**
 * Implementation of {@link LogService} using the standard error stream.
 * <p>
 * Actually, this service is somewhat misnamed now, since it prints {@code WARN}
 * and {@code ERROR} messages to stderr, but messages at lesser severities to
 * stdout.
 * </p>
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class, priority = Priority.LOW)
public class StderrLogService extends AbstractLogService {

	private Function<Integer, PrintStream> levelToStream =
		level -> (level <= LogLevel.WARN) ? System.err : System.out;

	public void setPrintStreams(Function<Integer, PrintStream> levelToStream) {
		this.levelToStream = levelToStream;
	}

	@Override
	protected void messageLogged(LogMessage message) {
		final PrintStream out = levelToStream.apply(message.level());
		out.print(message);
	}
}
