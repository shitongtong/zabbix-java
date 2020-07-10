package cn.stt.zabbix.api1;

import com.alibaba.fastjson.JSONObject;

/**
 * @author stt
 */
public interface ZabbixApi {

	void init();

	void destroy();

	String apiVersion();

	JSONObject call(Request request);

	boolean login(String user, String password);
}
