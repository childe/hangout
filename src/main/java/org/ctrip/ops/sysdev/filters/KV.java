package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;

public class KV extends BaseFilter {
	private static final Logger logger = Logger.getLogger(KV.class.getName());

	private String tagOnFailure;
	private String source;
	private String target;
	private String field_split;
	private String value_split;
	private String trim;
	private String trimkey;

	private ArrayList<String> removeFields;

	public KV(Map config) {
		super(config);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {

		this.removeFields = (ArrayList<String>) this.config
				.get("remove_fields");

		if (this.config.containsKey("source")) {
			this.source = (String) this.config.get("source");
		} else {
			this.source = "message";
		}

		if (this.config.containsKey("target")) {
			this.target = (String) this.config.get("target");
		}

		if (this.config.containsKey("field_split")) {
			this.field_split = (String) this.config.get("field_split");
		} else {
			this.field_split = " ";
		}
		if (this.config.containsKey("value_split")) {
			this.value_split = (String) this.config.get("value_split");
		} else {
			this.value_split = "=";
		}

		if (this.config.containsKey("trim")) {
			this.trim = (String) this.config.get("trim");
			this.trim = "^[" + this.trim + "]+|[" + this.trim + "]+$";
		}

		if (this.config.containsKey("trimkey")) {
			this.trimkey = (String) this.config.get("trimkey");
			this.trimkey = "^[" + this.trimkey + "]+|[" + this.trimkey + "]+$";
		}

		if (this.config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) this.config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "KVfail";
		}
	};

	@Override
	protected Map filter(Map event) {
		if (!event.containsKey(this.source)) {
			return event;
		}
		boolean success = true;

		HashMap targetObj = new HashMap();

		try {
			String sourceStr = (String) event.get(this.source);
			for (String kv : sourceStr.split(this.field_split)) {
				String[] kandv = kv.split(this.value_split, 2);
				if (kandv.length != 2) {
					success = false;
					continue;
				}

				String k = kandv[0];
				String v = kandv[1];

				if (this.trim != null) {
					v = v.replaceAll(this.trim, "");
				}
				if (this.trimkey != null) {
					k = k.replaceAll(this.trimkey, "");
				}

				if (this.target != null) {
					targetObj.put(k, v);
				} else {
					event.put(k, v);
				}
			}

			if (this.target != null) {
				event.put(this.target, targetObj);
			}
		} catch (Exception e) {
			logger.warn(event + "kv faild");
			success = false;
		}

		if (success == false) {
			if (!event.containsKey("tags")) {
				event.put("tags",
						new ArrayList<String>(Arrays.asList(this.tagOnFailure)));
			} else {
				Object tags = event.get("tags");
				if (tags.getClass() == ArrayList.class
						&& ((ArrayList) tags).indexOf(this.tagOnFailure) == -1) {
					((ArrayList) tags).add(this.tagOnFailure);
				}
			}
		} else if (this.removeFields != null) {
			for (String f : this.removeFields) {
				event.remove(f);
			}
		}

		return event;
	};
}
