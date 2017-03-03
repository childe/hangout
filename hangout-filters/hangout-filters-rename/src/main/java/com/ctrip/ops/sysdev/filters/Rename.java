package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//TODO setfield and render

@SuppressWarnings("ALL")
public class Rename extends BaseFilter {
	public Rename(Map config) {
		super(config);
	}

	private Map<String, String> fields;

	protected void prepare() {
		this.fields = (Map<String, String>) config.get("fields");
	}

    @Override
	protected Map filter(final Map event) {
		Iterator<Entry<String, String>> it = fields.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();

			String oldname = entry.getKey();
			String newname = entry.getValue();

			if (event.containsKey(oldname)) {
				event.put(newname, event.remove(oldname));
			}
		}
		return event;
	}
}
