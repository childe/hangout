package com.ctrip.ops.sysdev.outputs;

import com.ctrip.ops.sysdev.baseplugin.BaseOutput;
import com.ctrip.ops.sysdev.render.DateFormatter;
import com.ctrip.ops.sysdev.render.FreeMarkerRender;
import com.ctrip.ops.sysdev.render.RenderUtils;
import com.ctrip.ops.sysdev.render.TemplateRender;
import lombok.extern.log4j.Log4j;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

@Log4j
public class ElasticsearchHTTP extends BaseOutput {
    private final static int BULKACTION = 20000;

    private String index;
    private String indexTimezone;
    private RestClient restClient;
    private TemplateRender indexTypeRender;
    private TemplateRender idRender;
    private int bulkActions;
    private List eventList = new ArrayList();

    public ElasticsearchHTTP(Map config) {
        super(config);
    }

    protected void prepare() {
        this.index = getConfig(config,"index",null,true);
        this.bulkActions = getConfig(config,"bulk_actions", BULKACTION,false);
        this.indexTimezone = getConfig(config,"timezone", "UTC",false);

        try {
            this.idRender = RenderUtils.esConfigRender(config, "document_id", null);
            this.indexTypeRender = RenderUtils.esConfigRender(config, "index_type", new FreeMarkerRender("logs", "logs"));
            this.initESClient();
        } catch (Exception e) {
            log.error(e);
            System.exit(1);
        }
    }

    private void initESClient() throws NumberFormatException,
            UnknownHostException {
        ArrayList<String> hosts = getConfig(config,"hosts",null,true);
        List<HttpHost> httpHostList = hosts.stream().map(hostString -> {
            String[] parsedHost = hostString.split(":");
            String host = parsedHost[0];
            int port = parsedHost.length == 2 ? Integer.valueOf(parsedHost[1]) : 9200;
            return new HttpHost(host, port);
        }).collect(toList());
        List<HttpHost> clusterHosts = unmodifiableList(httpHostList);
        restClient = RestClient.builder(clusterHosts.toArray(new HttpHost[clusterHosts.size()])).build();

    }

    protected void emit(final Map event) {
        String _index = DateFormatter.format(event, index, indexTimezone);
        String _indexType = (String) indexTypeRender.render(event);
        String bulkPath = bulkPathBuilder(_index,_indexType);

        addEventList(event);
        if(this.eventList.size()/2>=this.bulkActions) {
            try {
                log.info(String.join("\n", eventList));
                Response reponse = restClient.performRequest(
                        "POST",
                        bulkPath,
                        Collections.<String, String>emptyMap(),
                        new NStringEntity(
                                String.join("", eventList),
                                ContentType.APPLICATION_JSON
                        )
                );
                log.info(reponse.toString());
            } catch (IOException e) {
                log.error(e);
            } finally {
                eventList.clear();
            }
        }
    }

    private String bulkPathBuilder(String index, String indexType) {
        StringBuilder sb = new StringBuilder();
        sb.append("/");
        sb.append(index);
        sb.append("/");
        sb.append(indexType);
        sb.append("/_bulk");
        return sb.toString();
    }

    private void addEventList(Map event) {
        event.put("@timestamp",event.get("@timestamp").toString());
        String jsonEventStr = JSONValue.toJSONString(event);
        eventList.add("{\"index\":{}}\n");
        eventList.add(jsonEventStr+"\n");
    }

    public void shutdown() {
        log.info("close restClient");
        try {
            restClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
