package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class Remove extends BaseFilter {
	public Remove(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private ArrayList<String> target;

	protected void prepare() {
		this.target = (ArrayList<String>) config.get("target");
	};

	@Override
	protected void filter(final Map event) {
		for (String t : this.target) {
			event.remove(t);
		}
	}
}
