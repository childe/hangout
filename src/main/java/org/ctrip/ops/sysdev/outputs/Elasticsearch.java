package org.ctrip.ops.sysdev.outputs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.render.Formatter;
import org.ctrip.ops.sysdev.render.FreeMarkerRender;
import org.ctrip.ops.sysdev.render.TemplateRender;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;

public class Elasticsearch extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Elasticsearch.class
			.getName());

	private String index;
	private String indexTimezone;
	private BulkProcessor bulkProcessor;
	private TransportClient esclient;
	private TemplateRender indexTypeRender;
	private TemplateRender idRender;

	public Elasticsearch(Map config) {
		super(config);
	}

	protected void prepare() {
		this.index = (String) config.get("index");

		if (config.containsKey("timezone")) {
			this.indexTimezone = (String) config.get("timezone");
		} else {
			this.indexTimezone = "UTC";
		}

		if (config.containsKey("document_id")) {
			try {
				this.idRender = new FreeMarkerRender(
						(String) config.get("document_id"),
						(String) config.get("document_id"));
			} catch (IOException e) {
				logger.fatal(e.getMessage());
				System.exit(1);
			}
		} else {
			this.idRender = null;
		}

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
		try {
			this.initESClient();
		} catch (Exception e) {
			logger.error(e);
			System.exit(1);
		}
	};

	@SuppressWarnings("unchecked")
	private void initESClient() throws NumberFormatException,
			UnknownHostException {

		String clusterName = (String) config.get("cluster");

		Settings settings = Settings.settingsBuilder()
				.put("client.transport.sniff", true)
				.put("cluster.name", clusterName).build();
		esclient = TransportClient.builder().settings(settings).build();

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
			esclient.addTransportAddress(new InetSocketTransportAddress(
					InetAddress.getByName(h), Integer.parseInt(p)));
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
				.builder(esclient, new BulkProcessor.Listener() {

					@Override
					public void afterBulk(long arg0, BulkRequest arg1,
							BulkResponse arg2) {
						logger.info("bulk done with executionId: " + arg0);
						List<ActionRequest> requests = arg1.requests();
						int toberetry = 0;
						int totalFailed = 0;
						for (BulkItemResponse item : arg2.getItems()) {
							if (item.isFailed()) {
								switch (item.getFailure().getStatus()) {
								case TOO_MANY_REQUESTS:
								case SERVICE_UNAVAILABLE:
									if (toberetry == 0) {
										logger.error("bulk has failed item which NEED to retry");
										logger.error(item.getFailureMessage());
									}
									toberetry++;
									bulkProcessor.add(requests.get(item
											.getItemId()));
									break;
								default:
									if (totalFailed == 0) {
										logger.error("bulk has failed item which do NOT need to retry");
										logger.error(item.getFailureMessage());
									}
									break;
								}

								totalFailed++;
							}
						}

						if (totalFailed > 0) {
							logger.info(totalFailed + " doc failed, "
									+ toberetry + " need to retry");
						}

						if (toberetry > 0) {
							try {
								logger.info("sleep " + toberetry / 2
										+ "millseconds after bulk failure");
								Thread.sleep(toberetry / 2);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
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
		String _index = Formatter.format(event, index, indexTimezone);
		String _indexType = indexTypeRender.render(event);
		IndexRequest indexRequest;
		if (this.idRender == null) {
			indexRequest = new IndexRequest(_index, _indexType).source(event);
		} else {
			String _id = idRender.render(event);
			indexRequest = new IndexRequest(_index, _indexType, _id)
					.source(event);
		}
		this.bulkProcessor.add(indexRequest);
	}
}
