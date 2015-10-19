package org.ctrip.ops.sysdev.outputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ctrip.ops.sysdev.render.Formatter;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Elasticsearch extends BaseOutput {
	private static final Logger logger = Logger.getLogger(Elasticsearch.class
			.getName());
	private String indexType;
	private String index;
	private TransportClient esclient;
	private BulkRequest bulkRequest;
	private int bulkActions, flushInterval;
	private ScheduledThreadPoolExecutor scheduler;
	private ScheduledFuture scheduledFuture;
	private long executionID;

	public Elasticsearch(Map config) {
		super(config);
	}

	protected void prepare() {
		this.index = (String) config.get("index");

		if (config.containsKey("index_type")) {
			this.indexType = (String) config.get("index_type");
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

		// bulkRequest = this.esclient.prepareBulk();

		if (config.containsKey("bulk_actions")) {
			bulkActions = (int) config.get("bulk_actions");
		} else {
			bulkActions = 20000;
		}

		if (config.containsKey("flush_interval")) {
			flushInterval = (int) config.get("flush_interval") * 1000;
		} else {
			flushInterval = 30000;
		}

		bulkRequest = new BulkRequest();

		scheduler = (ScheduledThreadPoolExecutor) Executors
				.newScheduledThreadPool(1);
		this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		this.scheduler
				.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		this.scheduledFuture = this.scheduler.scheduleWithFixedDelay(
				new Flush(), flushInterval, flushInterval,
				TimeUnit.MILLISECONDS);

		executionID = 0;
	}

	private synchronized int executeBulk() {
		executionID++;
		logger.info(executionID + ": number of bulk actions "
				+ bulkRequest.numberOfActions());

		final BulkRequest bulkRequest = this.bulkRequest;
		this.bulkRequest = new BulkRequest();

		BulkResponse bulkItemResponses = esclient.bulk(bulkRequest).actionGet();
		logger.info("got bulk response: " + executionID);
		List<ActionRequest> requests = bulkRequest.requests();
		int idx = 0;
		int toberetry = 0;
		for (BulkItemResponse item : bulkItemResponses.getItems()) {
			if (item.isFailed()) {
				if (idx == 0) {
					logger.error("bulk failed: " + executionID);
					logger.error(item.getFailureMessage());
				}
				switch (item.getFailure().getStatus()) {
				case TOO_MANY_REQUESTS:
				case SERVICE_UNAVAILABLE:
					toberetry++;
					this.bulkRequest.add(requests.get(item.getItemId()));
				}
			}
			idx++;
		}
		if (toberetry > 0) {
			try {
				logger.info("sleep " + toberetry / 10
						+ "millseconds after bulk failure");
				Thread.sleep(toberetry / 10);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		logger.info("bulk finished: " + executionID);
		return toberetry;
	}

	@Override
	public void emit(final Map event) {
		String _index = Formatter.format(event, index);
		String _indexType = Formatter.format(event, indexType);
		bulkRequest.add(new IndexRequest(_index, _indexType).source(event));
		if (bulkRequest.numberOfActions() >= this.bulkActions) {
			executeBulk();
		}
	}

	class Flush implements Runnable {
		@Override
		public void run() {
			synchronized (Elasticsearch.this) {
				if (bulkRequest.numberOfActions() == 0) {
					return;
				}
				executeBulk();
			}
		}
	}

}
