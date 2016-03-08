package com.ctrip.ops.sysdev.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;

public class BaseFilter {

	private static final Logger logger = Logger.getLogger(BaseFilter.class
			.getName());

	protected Map config;
	protected List<TemplateRender> IF;
	protected TemplateRender render;
	protected String tagOnFailure;
	protected ArrayList<String> removeFields;

	public BaseFilter(Map config) {
		this.config = config;

		if (this.config.containsKey("if")) {
			IF = new ArrayList<TemplateRender>();
			for (String c : (List<String>) this.config.get("if")) {
				try {
					IF.add(new FreeMarkerRender(c, c));
				} catch (IOException e) {
					logger.fatal(e.getMessage());
					System.exit(1);
				}
			}
		} else {
			IF = null;
		}

		if (config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) config.get("tag_on_failure");
		} else {
			this.tagOnFailure = null;
		}

		this.removeFields = (ArrayList<String>) this.config
				.get("remove_fields");

		this.prepare();
	}

	protected void prepare() {
	};

	public Map process(Map event) {
		boolean succuess = true;
		if (this.IF != null) {
			for (TemplateRender render : this.IF) {
				if (!render.render(event).equals("true")) {
					succuess = false;
					break;
				}
			}
		}
		if (succuess == true) {
			event = this.filter(event);
		}

		return event;
	};

	protected Map filter(Map event) {
		return null;
	}

	public void postProcess(Map event, boolean ifsuccess) {
		if (ifsuccess == false) {
			if (this.tagOnFailure == null) {
				return;
			}
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
	}
}
