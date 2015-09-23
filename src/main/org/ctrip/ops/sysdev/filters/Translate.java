package org.ctrip.ops.sysdev.filters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import scala.Tuple2;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.render.FreeMarkerRender;
import org.ctrip.ops.sysdev.render.TemplateRender;
import org.yaml.snakeyaml.Yaml;

public class Translate extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Translate.class
			.getName());

	public Translate(Map config, ArrayBlockingQueue inputQueue) {
		super(config, inputQueue);
	}

	private String target;
	private String source;
	private String dictionaryPath;
	private int refreshInterval = 300;
	private long nextLoadTime;
	private HashMap dictionary = null;

	private void loadDictionary() {
		Yaml yaml = new Yaml();
		FileInputStream input;
		try {
			input = new FileInputStream(new File(dictionaryPath));
			dictionary = (HashMap) yaml.load(input);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			dictionary = null;
		}
	}

	protected void prepare() {
		String target = (String) config.get("target");
		String source = (String) config.get("source");

		dictionaryPath = (String) config.get("dictionary_path");

		loadDictionary();

		if (config.containsKey("refresh_interval")) {
			this.refreshInterval = (int) config.get("refresh_interval");
		}
		nextLoadTime = System.currentTimeMillis() + refreshInterval * 1000;
	};

	@Override
	protected void filter(final Map event) {
		if (dictionary == null || !event.containsKey(this.source)) {
			return;
		}
		if (System.currentTimeMillis() >= nextLoadTime) {
			loadDictionary();
			nextLoadTime += refreshInterval * 1000;
		}
		Object t = dictionary.get(event.get(source));
		if (t != null) {
			event.put(target, t);
		}
	}
}
