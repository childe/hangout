package org.ctrip.ops.sysdev.outputs;

import java.util.ArrayList;
import java.util.Map;


import org.apache.log4j.Logger;
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
	private String indexType;
	private BulkProcessor bulkProcessor;
	private TransportClient esclient;

	public Elasticsearch(Map config) {
		super(config);
	}

	protected void prepare() {
		this.index = (String) config.get("index");

		if (config.containsKey("index_type")) {
			this.indexType = (String) config.get("index_type");
		} else {
			this.indexType = "logs";
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

		int bulkActions = 20000, bulkSize = 15, flushInterval = 10, concurrentRequests = 1;
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
						// TODO Auto-generated method stub
						if (arg2.hasFailures()) {

							logger.error("bulk failed");
							logger.trace(arg2.buildFailureMessage());

							for (BulkItemResponse item : arg2.getItems()) {
								logger.trace(item.getFailureMessage());
								// logger.info(item.getId());
								// logger.info(item.getIndex());
								// logger.info(item.getType());
								// logger.info(item.getItemId());
								// logger.info(arg1.getContext());
								// logger.info(arg1.requests().get(0).getContext());

								switch (item.getFailure().getStatus()) {
								case TOO_MANY_REQUESTS:
								case SERVICE_UNAVAILABLE:
									// bulkProcessor.add);
								}
							}
						}
					}

					@Override
					public void afterBulk(long arg0, BulkRequest arg1,
							Throwable arg2) {
						logger.error("bulk got exception");
						logger.trace(arg2.getMessage());
					}

					@Override
					public void beforeBulk(long arg0, BulkRequest arg1) {
					}
				}).setBulkActions(bulkActions)
				.setBulkSize(new ByteSizeValue(bulkSize, ByteSizeUnit.MB))
				.setFlushInterval(TimeValue.timeValueSeconds(flushInterval))
				.setConcurrentRequests(concurrentRequests).build();
	}

	@Override
	public void emit(Map event) {
		String _index = jinjava.render(index, event);
		String _type = jinjava.render(indexType, event);

		IndexRequest indexRequest = new IndexRequest(_index, _type)
				.source(event);
		System.out.println("id: " + indexRequest.id());
		logger.info(indexRequest.id());
		this.bulkProcessor.add(new IndexRequest(_index, _type).source(event));
	}
}
