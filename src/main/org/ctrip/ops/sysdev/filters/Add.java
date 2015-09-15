package org.ctrip.ops.sysdev.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

public class Add extends BaseFilter {
	public Add(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private Map<String, String> fields;

	protected void prepare() {
		this.fields = (Map<String, String>) config.get("fields");
	};

	@Override
	protected void filter(final Map event) {
		Iterator<Entry<String, String>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();

			String field = entry.getKey();
			String value = entry.getValue();
			event.put(field, jinjava.render(value, new HashMap() {
				{
					put("event", event);
				}
			}));
		}

	}
}
