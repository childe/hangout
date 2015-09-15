package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;

import scala.Tuple2;

public class Grok extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Grok.class.getName());

	private String tagOnFailure;
	private String src;
	private List<Tuple2> matches;

	private ArrayList<String> removeFields;

	public Grok(Map config, ArrayBlockingQueue preQueue) {
		super(config, preQueue);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {
		this.matches = new ArrayList<Tuple2>();
		for (String matchString : (ArrayList<String>) this.config.get("match")) {
			Regex regex = new Regex(matchString.getBytes(), 0,
					matchString.getBytes().length, Option.NONE,
					UTF8Encoding.INSTANCE);

			matches.add(new Tuple2(regex, this
					.getNamedGroupCandidates(matchString)));
		}

		this.removeFields = (ArrayList<String>) this.config
				.get("remove_fields");

		if (this.config.containsKey("src")) {
			this.src = (String) this.config.get("src");
		} else {
			this.src = "message";
		}

		if (this.config.containsKey("tagOnFailure")) {
			this.tagOnFailure = (String) this.config.get("tagOnFailure");
		} else {
			this.tagOnFailure = "grokfail";
		}
	};

	private List<String> getNamedGroupCandidates(String regex) {
		ArrayList<String> namedGroups = new ArrayList<String>();

		java.util.regex.Matcher m = Pattern.compile(
				"\\(\\?<([a-zA-Z][-_a-zA-Z0-9]*)>").matcher(regex);

		while (m.find()) {
			namedGroups.add(m.group(1));
		}

		return namedGroups;
	}

	@Override
	protected void filter(Map event) {
		if (!event.containsKey(this.src)) {
			return;
		}
		boolean success = false;
		String input = ((String) event.get(this.src));

		for (Tuple2 match : this.matches) {
			try {
				Regex regex = (Regex) match._1;

				Matcher matcher = regex.matcher(input.getBytes());
				int result = matcher.search(0, input.getBytes().length,
						Option.DEFAULT);

				if (result != -1) {
					success = true;
					Region region = matcher.getEagerRegion();
					ArrayList<String> groupnames = (ArrayList<String>) match._2;
					for (int i = 1; i < region.numRegs; i++) {
						if (region.beg[i] != -1) {
							event.put(groupnames.get(i - 1), input.substring(
									region.beg[i], region.end[i]));
						}
					}
					break;
				}

			} catch (Exception e) {
				logger.warn("grok failed:" + event);
				logger.trace(e.getLocalizedMessage());
				success = false;
			}
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
	};

}
