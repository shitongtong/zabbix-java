package cn.stt.zabbix.api3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author stt
 */
public class ZabbixApiUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZabbixApiUtil.class);

    private CloseableHttpClient httpClient;

    private URI uri;

    private volatile String auth;

    public ZabbixApiUtil(String url) {
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new RuntimeException("url invalid", e);
        }
    }

    public ZabbixApiUtil(URI uri) {
        this.uri = uri;
    }

    public ZabbixApiUtil(String url, CloseableHttpClient httpClient) {
        this(url);
        this.httpClient = httpClient;
    }

    public ZabbixApiUtil(URI uri, CloseableHttpClient httpClient) {
        this(uri);
        this.httpClient = httpClient;
    }

    public void init() {
        if (httpClient == null) {
            httpClient = HttpClients.custom().build();
        }
    }

    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                LOGGER.error("close httpclient error!", e);
            }
        }
    }

    public boolean login(String user, String password) {
        this.auth = null;
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().paramEntry("user", user).paramEntry("password", password)
                .method("user.login").build();
        JSONObject response = call(request);
        String auth = response.getString("result");
        if (auth != null && !auth.isEmpty()) {
            this.auth = auth;
            return true;
        } else {
            LOGGER.info("login faild:{}", response.getString("error"));
            return false;
        }
    }

    public String apiVersion() {
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("apiinfo.version").build();
        JSONObject response = call(request);
        return response.getString("result");
    }

    public boolean hostExists(String name) {
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("host.exists").paramEntry("name", name).build();
        JSONObject response = call(request);
        return response.getBooleanValue("result");
    }

    public String hostCreate(String host, String groupId) {
        JSONArray groups = new JSONArray();
        JSONObject group = new JSONObject();
        group.put("groupid", groupId);
        groups.add(group);
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("host.create").paramEntry("host", host)
                .paramEntry("groups", groups).build();
        JSONObject response = call(request);
        return response.getJSONObject("result").getJSONArray("hostids").getString(0);
    }

    public boolean hostgroupExists(String name) {
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("hostgroup.exists").paramEntry("name", name).build();
        JSONObject response = call(request);
        return response.getBooleanValue("result");
    }

    /**
     * @param name
     * @return groupId
     */
    public String hostgroupCreate(String name) {
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("hostgroup.create").paramEntry("name", name).build();
        JSONObject response = call(request);
        return response.getJSONObject("result").getJSONArray("groupids").getString(0);
    }

    public String getGroupIdByName(String groupName) {
        JSONObject filterObject = new JSONObject();
        filterObject.put("name", groupName);
        ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("hostgroup.get")
                .paramEntry("filter", filterObject)
                .build();
        JSONObject response = call(request);
        LOGGER.debug("getGroupIdByName({}).result:{}", groupName, request);
        if (StringUtils.isNotBlank(response.getString("error"))) {
            return null;
        } else {
            return response.getJSONArray("result").getJSONObject(0).getString("groupid");
        }
    }

    public JSONObject call(ZabbixRequest request) {
        if (request.getAuth() == null) {
            request.setAuth(this.auth);
        }
        try {
            HttpUriRequest httpRequest = org.apache.http.client.methods.RequestBuilder.post().setUri(uri)
                    .addHeader("Content-Type", "application/json")
                    .setEntity(new StringEntity(JSON.toJSONString(request), ContentType.APPLICATION_JSON)).build();
            CloseableHttpResponse response = httpClient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            byte[] data = EntityUtils.toByteArray(entity);
            return (JSONObject) JSON.parse(data);
        } catch (IOException e) {
            throw new RuntimeException("zabbixApi call exception!", e);
        }
    }

}
