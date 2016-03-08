package com.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Stdout.class
			.getName());

	public Stdout(Map config) {
		super(config);
	}

	@Override
	protected void prepare() {

	}

	@Override
	protected void emit(Map event) {
		System.out.println(event);
	}
}
