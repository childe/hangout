package org.ctrip.ops.sysdev.outputs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.render.Formatter;
import org.ctrip.ops.sysdev.render.FreeMarkerRender;
import org.ctrip.ops.sysdev.render.TemplateRender;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

public class Elasticsearch extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Elasticsearch.class
			.getName());

	private String index;
	private BulkProcessor bulkProcessor;
	private TransportClient esclient;
	private TemplateRender indexRender;
	private TemplateRender indexTypeRender;

	public Elasticsearch(Map config) {
		super(config);
	}

	protected void prepare() {
		try {
			this.indexRender = new FreeMarkerRender(
					(String) config.get("index"), (String) config.get("index"));
		} catch (IOException e) {
			logger.fatal(e.getMessage());
			System.exit(1);
		}
		this.index = (String) config.get("index");

		if (config.containsKey("index_type")) {
			try {
				this.indexTypeRender = new FreeMarkerRender(
						(String) config.get("index_type"),
						(String) config.get("index_type"));
			} catch (IOException e) {
				logger.fatal(e.getMessage());
				System.exit(1);
			}
		} else {
			try {
				this.indexTypeRender = new FreeMarkerRender("logs", "logs");
			} catch (IOException e) {
				logger.fatal(e.getMessage());
				System.exit(1);
			}
		}

		this.initESClient();
	};

	private void initESClient() {

		String clusterName = (String) config.get("cluster");

		Settings settings = ImmutableSettings.settingsBuilder()
				.put("client.transport.sniff", true)
				.put("cluster.name", clusterName).build();

		this.esclient = new TransportClient(settings);

		ArrayList<String> hosts = (ArrayList<String>) config.get("hosts");
		for (String host : hosts) {
			String[] hp = host.split(":");
			String h = null, p = null;
			if (hp.length == 2) {
				h = hp[0];
				p = hp[1];
			} else if (hp.length == 1) {
				h = hp[0];
				p = "9300";
			}
			this.esclient.addTransportAddress(new InetSocketTransportAddress(h,
					Integer.parseInt(p)));
		}

		int bulkActions = 20000, bulkSize = 15, flushInterval = 10, concurrentRequests = 0;
		if (config.containsKey("bulk_actions")) {
			bulkActions = (int) config.get("bulk_actions");
		}
		if (config.containsKey("bulk_size")) {
			bulkSize = (int) config.get("bulk_size");
		}
		if (config.containsKey("flush_interval")) {
			flushInterval = (int) config.get("flush_interval");
		}
		if (config.containsKey("concurrent_requests")) {
			concurrentRequests = (int) config.get("concurrent_requests");
		}

		bulkProcessor = BulkProcessor
				.builder(this.esclient, new BulkProcessor.Listener() {
					@Override
					public void afterBulk(long arg0, BulkRequest arg1,
							BulkResponse arg2) {
						logger.info("bulk done with executionId: " + arg0);
						List<ActionRequest> requests = arg1.requests();
						int idx = 0;
						for (BulkItemResponse item : arg2.getItems()) {
							if (item.isFailed()) {
								if (idx == 0) {
									logger.error("bulk failed");
									logger.error(item.getFailureMessage());
								}
								switch (item.getFailure().getStatus()) {
								case TOO_MANY_REQUESTS:
								case SERVICE_UNAVAILABLE:
									bulkProcessor.add(requests.get(item
											.getItemId()));
								}
							}

							idx += 1;
						}
					}

					@Override
					public void afterBulk(long arg0, BulkRequest arg1,
							Throwable arg2) {
						logger.error("bulk got exception");
						logger.error(arg2.getMessage());
					}

					@Override
					public void beforeBulk(long arg0, BulkRequest arg1) {
						logger.info("executionId: " + arg0);
						logger.info("numberOfActions: "
								+ arg1.numberOfActions());
					}
				}).setBulkActions(bulkActions)
				.setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB))
				.setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
				.setConcurrentRequests(concurrentRequests).build();
	}

	protected void emit(final Map event) {
		// String _index = indexRender.render(event);
		String _index = Formatter.format(event, index);
		String _indexType = indexTypeRender.render(event);

		IndexRequest indexRequest = new IndexRequest(_index, _indexType)
				.source(event);
		this.bulkProcessor.add(indexRequest);
	}
}
