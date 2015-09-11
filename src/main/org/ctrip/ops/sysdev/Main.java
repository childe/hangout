package org.ctrip.ops.sysdev;

import org.ctrip.ops.sysdev.inputs.BaseInput;
import org.ctrip.ops.sysdev.outputs.BaseOutput;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.configs.HangoutConfig;

public class Main {
	private static final Logger logger = Logger.getLogger("Main");

	public static void main(String[] args) throws Exception {

		// parse configure file
		Map configs = HangoutConfig.parse(args[0]);
		logger.debug(configs);

		ArrayList<ArrayBlockingQueue> preQueues = new ArrayList<ArrayBlockingQueue>();

		// for input in all_inputs
		ArrayList<Map> inputs = (ArrayList<Map>) configs.get("inputs");

		for (Map input : inputs) {
			Iterator<Entry<String, Map>> inputIT = input.entrySet().iterator();

			while (inputIT.hasNext()) {
				Map.Entry<String, Map> inputEntry = inputIT.next();
				String inputType = inputEntry.getKey();
				System.out.println(inputType);
				Map inputConfig = inputEntry.getValue();
				System.out.println(inputConfig);

				Class<?> inputClass = Class
						.forName("org.ctrip.ops.sysdev.inputs." + inputType);
				Constructor<?> ctor = inputClass.getConstructor(Map.class);
				BaseInput inputInstance = (BaseInput) ctor
						.newInstance(inputConfig);
				preQueues.add(inputInstance.getMessageQueue());
				inputInstance.emit();
			}
		}

		// for filter in filters
		if (configs.containsKey("filters")) {
			ArrayList<Map> filters = (ArrayList<Map>) configs.get("filters");

			for (Map filter : filters) {
				Iterator<Entry<String, Map>> filterIT = filter.entrySet()
						.iterator();

				while (filterIT.hasNext()) {
					Map.Entry<String, Map> filterEntry = filterIT.next();
					String filterType = filterEntry.getKey();
					System.out.println(filterType);
					Map filterConfig = filterEntry.getValue();
					System.out.println(filterConfig);

					Class<?> filterClass = Class
							.forName("org.ctrip.ops.sysdev.filters."
									+ filterType);
					Constructor<?> ctor = filterClass.getConstructor(Map.class,
							List.class);
					// BaseFilter filterInstance = (BaseInput) ctor.newInstance(
					// filterConfig, preQueues);
					// filterInstance.process();
				}
			}
		}

		// for output in output
		ArrayList<Map> outputs = (ArrayList<Map>) configs.get("outputs");

		for (Map output : outputs) {
			Iterator<Entry<String, Map>> outputIT = output.entrySet()
					.iterator();

			while (outputIT.hasNext()) {
				Map.Entry<String, Map> outputEntry = outputIT.next();
				String outputType = outputEntry.getKey();
				System.out.println(outputType);
				Map outputConfig = outputEntry.getValue();
				System.out.println(outputConfig);

				Class<?> outputClass = Class
						.forName("org.ctrip.ops.sysdev.outputs." + outputType);
				Constructor<?> ctor = outputClass.getConstructor(Map.class,
						List.class);
				BaseOutput outputInstance = (BaseOutput) ctor.newInstance(
						outputConfig, preQueues);
				outputInstance.emit();
			}
		}

	}

}
