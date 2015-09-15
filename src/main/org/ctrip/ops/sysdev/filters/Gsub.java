package org.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.ctrip.ops.sysdev.utils.jinfilter.JinManager;

import com.hubspot.jinjava.Jinjava;

public class Gsub extends BaseFilter {
	public Gsub(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private Map<String, List<String>> fields;

	protected void prepare() {
		this.fields = (Map<String, List<String>>) config.get("fields");
	};

	@Override
	protected void filter(final Map event) {
		Iterator<Entry<String, List<String>>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> entry = it.next();

			String field = entry.getKey();
			String regex = entry.getValue().get(0);
			String replacement = entry.getValue().get(1);

			if (event.containsKey(field)) {
				event.put(field, ((String) event.remove(field)).replaceAll(
						regex, replacement));
			}
		}
	}
}
