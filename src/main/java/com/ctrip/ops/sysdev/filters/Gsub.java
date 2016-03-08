package com.ctrip.ops.sysdev.filters;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Gsub extends BaseFilter {
	public Gsub(Map config) {
		super(config);
	}

	private Map<String, List<String>> fields;

	protected void prepare() {
		this.fields = (Map<String, List<String>>) config.get("fields");
	};

	@Override
	protected Map filter(final Map event) {
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

		return event;
	}
}
