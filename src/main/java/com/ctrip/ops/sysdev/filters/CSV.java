package com.ctrip.ops.sysdev.filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import java.util.Iterator;

import org.apache.log4j.Logger;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVParser;


public class CSV extends BaseFilter {
	private static final Logger logger = Logger.getLogger(CSV.class.getName());

    private String source;
	private String target;
    private char separator;
	private char quote_char;
	private char escape = '\0';

    private ArrayList<String> columns;

    private CSVParser csvParser;

	@SuppressWarnings("rawtypes")
	public CSV(Map config) {
		super(config);
	}

	@SuppressWarnings("unchecked")
	protected void prepare() {

		if (this.config.containsKey("source")) {
			this.source = (String) this.config.get("source");
		} else {
			this.source = "message";
		}

		if (!config.containsKey("columns")) {
			logger.error("no columns fields configured in message");
			System.exit(1);
		}
        this.columns = (ArrayList<String>) this.config.get("columns");

		if (this.config.containsKey("target")) {
			this.target = (String) this.config.get("target");
		}

		if (this.config.containsKey("separator")) {
			this.separator = ((String) this.config.get("separator")).charAt(0);
		} else {
			this.separator = ',';
        }

		if (this.config.containsKey("quote_char")) {
			this.quote_char = ((String) this.config.get("quote_char")).charAt(0);
		} else {
			this.quote_char = '"';
        }

		if (this.config.containsKey("tag_on_failure")) {
			this.tagOnFailure = (String) this.config.get("tag_on_failure");
		} else {
			this.tagOnFailure = "csvfail";
        }
        this.csvParser = new CSVParserBuilder().withSeparator(this.separator).withQuoteChar(this.quote_char).withEscapeChar(this.escape).build();

	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Map filter(Map event) {
		if (!event.containsKey(this.source)) {
			return event;
		}

		boolean success = true;

		HashMap targetObj = new HashMap();

		try {
            String sourceStr = (String) event.get(this.source);
            if (sourceStr.length() > 0) {
				String [] line = this.csvParser.parseLine(sourceStr);
				List<String> values = new ArrayList<String>(Arrays.asList(line));
				Iterator<String> value_iterator = (Iterator<String>)values.iterator();
				Iterator<String> field_iterator = (Iterator<String>)this.columns.iterator();
				while (field_iterator.hasNext() && value_iterator.hasNext()) {
                    if (this.target == null) {
                        event.put(field_iterator.next(), value_iterator.next());
                    } else {
					    targetObj.put(field_iterator.next(), value_iterator.next());
                    }
				}
				while (field_iterator.hasNext()) {
                    if (this.target == null) {
                        event.put(field_iterator.next(), "");
                    } else {
					    targetObj.put(field_iterator.next(), "");
                    }
				}
            }
			if (this.target != null) {
				event.put(this.target, targetObj);
			}
		} catch (Exception e) {
            logger.error(e.getMessage());
			logger.warn(event + "csv faild");
			success = false;
		}

		this.postProcess(event, success);

		return event;
	};
}
