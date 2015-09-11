package org.ctrip.ops.sysdev.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class Stdout extends BaseOutput {
	private static final Logger logger = Logger.getLogger("Main");

	private Map configs;
	private List<ArrayBlockingQueue> preQueues;

	public Stdout(Map config, List<ArrayBlockingQueue> preQueues) {
		super(config, preQueues);

		this.configs = configs;
		this.preQueues = preQueues;

		System.out.println(preQueues.size());
	}

	@Override
	public void emit() {
		// TODO Auto-generated method stub
		while (true) {
			for (ArrayBlockingQueue messageQueue : this.preQueues) {
				Object m = messageQueue.poll();
				if (m != null) {
					System.out.println(m);
				}
			}
		}
	}

}
