package org.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;

import scala.Tuple2;

public class Grok extends BaseFilter {
	private static final Logger logger = Logger.getLogger(Grok.class.getName());

	private String tagOnFailure;
	private String src;
	private List<Regex> matches;

	private ArrayList<String> removeFields;

	public Grok(Map config, ArrayBlockingQueue preQueue) {
		super(config, preQueue);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {
		this.matches = new ArrayList<Regex>();
		for (String matchString : (ArrayList<String>) this.config.get("match")) {
			Regex regex = new Regex(matchString.getBytes(), 0,
					matchString.getBytes().length, Option.NONE,
					UTF8Encoding.INSTANCE);

			matches.add(regex);
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


	@Override
	protected void filter(Map event) {
		if (!event.containsKey(this.src)) {
			return;
		}
		boolean success = false;
		String input = ((String) event.get(this.src));
		byte[] bs = input.getBytes();

		for (Regex regex : this.matches) {
			try {
				Matcher matcher = regex.matcher(bs);
				int result = matcher.search(0, bs.length, Option.DEFAULT);

				if (result != -1) {
					success = true;

					Region region = matcher.getEagerRegion();
					for (Iterator<NameEntry> entry = regex
							.namedBackrefIterator(); entry.hasNext();) {
						NameEntry e = entry.next();
						int number = e.getBackRefs()[0]; // can have many refs
															// per name
						int begin = region.beg[number];
						int end = region.end[number];
						if (begin != -1) {
							event.put(new String(e.name, e.nameP, e.nameEnd
									- e.nameP), new String(bs, begin, end
									- begin));
						}

					}
					break;
				}

			} catch (Exception e) {
				System.out.println("grok failed:" + event);
				System.out.println(e.getLocalizedMessage());
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

	public static void main(String[] argv) {
		byte[] pattern = "(?<name>a*) (-|(?<age>\\d+)) (?<level>\\w+)"
				.getBytes();
		String input = "aaa - debug";
		byte[] str = input.getBytes();

		Regex regex = new Regex(pattern, 0, pattern.length, Option.NONE,
				UTF8Encoding.INSTANCE);
		Matcher matcher = regex.matcher(str);
		int result = matcher.search(0, str.length, Option.DEFAULT);
		if (result != -1) {
			Region region = matcher.getEagerRegion();
			for (Iterator<NameEntry> entry = regex.namedBackrefIterator(); entry
					.hasNext();) {
				NameEntry e = entry.next();
				int number = e.getBackRefs()[0]; // can have many refs per name
				int begin = region.beg[number];
				int end = region.end[number];
				// System.out.println(e.toString());
				// System.out.println(number);
				// System.out.println(begin);
				// System.out.println(end);
				if (begin != -1) {
					System.out.println(new String(e.name, e.nameP, e.nameEnd
							- e.nameP)
							+ ": " + input.substring(begin, end));
				}

			}
		}
	}

}
