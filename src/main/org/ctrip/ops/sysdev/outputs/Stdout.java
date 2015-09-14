package org.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Stdout.class.getName());

	public Stdout(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	@Override
	public void emit(Object event) {
		System.out.println(event);
	}
}
