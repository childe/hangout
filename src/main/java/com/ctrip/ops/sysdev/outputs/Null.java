package com.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Null extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Null.class.getName());

	public Null(Map config) {
		super(config);
	}

	@Override
	protected void prepare() {

	}

	@Override
	protected void emit(Map event) {
	}
}
