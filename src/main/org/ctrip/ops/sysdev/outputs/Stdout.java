package org.ctrip.ops.sysdev.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger("BaseOutput");

	public Stdout(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	@Override
	public void emit() {
		while (true) {
			Object m = this.inputQueue.poll();
			if (m != null) {
				System.out.println(m);
			}
		}
	}
}
