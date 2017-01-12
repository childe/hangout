package com.ctrip.ops.sysdev.filters;

import com.ctrip.ops.sysdev.baseplugin.BaseFilter;

import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("ALL")
public class Remove extends BaseFilter {
	public Remove(Map config ) {
		super(config);
	}

	private ArrayList<String> fields;

	protected void prepare() {
		this.fields = (ArrayList<String>) config.get("fields");
	}

    @Override
	protected Map filter(final Map event) {
		for (String t : this.fields) {
			event.remove(t);
		}

		return event;
	}
}
