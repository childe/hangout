package com.ctrip.ops.sysdev.outputs;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.formatter.DateFormatter;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.TemplateRender;
import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

public class ElasticsearchHTTP extends BaseOutput {
    private static final Logger logger = Logger.getLogger(ElasticsearchHTTP.class
            .getName());

    private String index;
    private String indexTimezone;
    private RestClient restClient;
    private TemplateRender indexTypeRender;
    private TemplateRender idRender;

    public ElasticsearchHTTP(Map config) {
        super(config);
        logger.error("this is not implemented");
        System.exit(1);
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
    }

    @SuppressWarnings("unchecked")
    private void initESClient() throws NumberFormatException,
            UnknownHostException {
        restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"), new HttpHost("localhost", 9201, "http")).build();

    }

    protected void emit(final Map event) {
        String _index = DateFormatter.format(event, index, indexTimezone);
        String _indexType = indexTypeRender.render(event).toString();
        IndexRequest indexRequest;
    }

    public void shutdown() {
        logger.info("close restClient");
        try {
            restClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
