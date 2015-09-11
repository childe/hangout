package org.ctrip.ops.sysdev.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger("Main");

	public Stdout(Map config, ArrayBlockingQueue preQueue) {
		super(config, preQueue);
	}

	@Override
	public void emit() {
		// TODO Auto-generated method stub
		while (true) {
			Object m = this.inputQueue.poll();
			if (m != null) {
				System.out.println(m);
			}
		}
	}
}
