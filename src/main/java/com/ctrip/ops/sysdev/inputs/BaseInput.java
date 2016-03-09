package com.ctrip.ops.sysdev.inputs;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import com.ctrip.ops.sysdev.outputs.BaseOutput;
import com.ctrip.ops.sysdev.decoder.IDecode;
import com.ctrip.ops.sysdev.decoder.JsonDecoder;
import com.ctrip.ops.sysdev.decoder.PlainDecoder;
import com.ctrip.ops.sysdev.filters.BaseFilter;

public abstract class BaseInput {
	private static final Logger logger = Logger.getLogger(BaseInput.class
			.getName());

	protected Map<String, Object> config;
	protected IDecode decoder;
	protected BaseFilter[] filterProcessors;
	protected BaseOutput[] outputProcessors;
	protected ArrayList<Map> filters;
	protected ArrayList<Map> outputs;

	public BaseFilter[] createFilterProcessors() {
		if (filters != null) {
			filterProcessors = new BaseFilter[filters.size()];

			int idx = 0;
			for (Map filter : filters) {
				Iterator<Entry<String, Map>> filterIT = filter.entrySet()
						.iterator();

				while (filterIT.hasNext()) {
					Map.Entry<String, Map> filterEntry = filterIT.next();
					String filterType = filterEntry.getKey();
					Map filterConfig = filterEntry.getValue();

					try {
						Class<?> filterClass = Class
								.forName("com.ctrip.ops.sysdev.filters."
										+ filterType);
						Constructor<?> ctor = filterClass
								.getConstructor(Map.class);

						BaseFilter filterInstance = (BaseFilter) ctor
								.newInstance(filterConfig);
						filterProcessors[idx] = filterInstance;
					} catch (Exception e) {
						logger.error(e);
						System.exit(1);
					}
					idx++;
				}
			}
		} else {
			filterProcessors = null;
		}
		return filterProcessors;
	}

	public BaseOutput[] createOutputProcessors() {
		outputProcessors = new BaseOutput[outputs.size()];
		int idx = 0;
		for (Map output : outputs) {
			Iterator<Entry<String, Map>> outputIT = output.entrySet()
					.iterator();

			while (outputIT.hasNext()) {
				Map.Entry<String, Map> outputEntry = outputIT.next();
				String outputType = outputEntry.getKey();
				Map outputConfig = outputEntry.getValue();
				Class<?> outputClass;
				try {
					outputClass = Class.forName("com.ctrip.ops.sysdev.outputs."
							+ outputType);
					Constructor<?> ctor = outputClass.getConstructor(Map.class);

					outputProcessors[idx] = (BaseOutput) ctor
							.newInstance(outputConfig);
					idx++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return outputProcessors;
	}

	public IDecode createDecoder() {
		String codec = (String) this.config.get("codec");
		if (codec != null && codec.equalsIgnoreCase("plain")) {
			return new PlainDecoder();
		} else {
			return new JsonDecoder();
		}
	}

	public BaseInput(Map config, ArrayList<Map> filters, ArrayList<Map> outputs)
			throws Exception {
		this.config = config;
		this.filters = filters;
		this.outputs = outputs;

	}

	protected abstract void prepare();

	public abstract void emit();

	public void process(String message) {
		Map<String, Object> event = this.decoder.decode(message);

		if (this.filterProcessors != null) {
			for (BaseFilter bf : filterProcessors) {
				if (event == null) {
					break;
				}
				event = bf.process(event);
			}
		}
		if (event != null) {
			for (BaseOutput bo : outputProcessors) {
				bo.process(event);
			}
		}
	};

}
