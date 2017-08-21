package com.ctrip.ops.sysdev.outputs;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import org.json.simple.JSONValue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Stdout.class
			.getName());

	private String format;
	public Stdout(Map config) {
		super(config);
	}

	@Override
	protected void prepare() {
		if (config.containsKey("format")) {
			this.format = (String) config.get("format");
		} else {
			this.format = "plain";
		}

	}

	@Override
	protected void emit(Map event) {
		if (this.format.equals("json")) {
			System.out.println(JSONValue.toJSONString(event));
		} else {
		 	System.out.println(event);
		}
	}
}
