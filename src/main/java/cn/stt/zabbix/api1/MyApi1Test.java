package cn.stt.zabbix.api1;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author shitt7
 * @Date 2020/3/10 14:22
 */
@Slf4j
public class MyApi1Test {

    private ZabbixApi zabbixApi;

    private boolean login() {
        String url = "http://localhost/zabbix/api_jsonrpc.php";
        String username = "username";
        String password = "password";
        zabbixApi = new DefaultZabbixApi(url);
        zabbixApi.init();
        return zabbixApi.login(username, password);
    }

    /**
     * 通过host.get获取监控项最新数据
     */
    @Test
    public void test1() {
        boolean loginFlag = login();
        if (loginFlag) {
            //根据groupName获取groupId(方便适用各种不同环境)
            List<String> groupIds = getGroupIds();
            for (String groupId : groupIds) {
                //获取指标性能数据
                JSONArray performanceData = getPerformanceData(groupId);
                log.info("performanceData={}", performanceData);
            }
        }
    }

    private List<String> getGroupIds() {
        List<String> groupIdList = new ArrayList<>();
        String groupName = "zabbix236";
        String groupId = getGroupIdByName(groupName);
        if (StringUtils.isNotBlank(groupId)) {
            groupIdList.add(groupId);
        }
        return groupIdList;
    }

    private String getGroupIdByName(String groupName) {
        JSONObject filterObject = new JSONObject();
//        filterObject.put("name",new String[]{"cnxl-server","zabbix-jvm"});
        filterObject.put("name", groupName);
        Request request = RequestBuilder.newBuilder().method("hostgroup.get")
                .paramEntry("filter", filterObject)
                .build();
        JSONObject resObject = zabbixApi.call(request);
        log.info("hostgroup.get.resObject={}", resObject);
        if (StringUtils.isNotBlank(resObject.getString("error"))) {
            return null;
        } else {
            return resObject.getJSONArray("result").getJSONObject(0).getString("groupid");
        }
    }

    private JSONArray getPerformanceData(String groupId) {
        JSONArray resultArray = new JSONArray();
        JSONObject itemFilter = new JSONObject();
        itemFilter.put("key_", new String[]{"system.uptime", "system.cpu.util"});
        Request request = RequestBuilder.newBuilder().method("host.get")
                .paramEntry("groupids", groupId)
                .paramEntry("output", new String[]{"hostid", "name", "host"})
                .paramEntry("selectInterfaces", "extend")
//                .paramEntry("selectItems", "extend")
                .paramEntry("selectItems", new String[]{"itemid", "name", "value_type", "lastclock", "prevvalue", "status", "units", "key_", "lastvalue"})
                .paramEntry("filter", itemFilter)
                .paramEntry("search", itemFilter)
                .build();
        JSONObject resObject = zabbixApi.call(request);
        log.info("host.get.resObject={}", resObject);
        if (StringUtils.isNotBlank(resObject.getString("error"))) {
            return null;
        }
        JSONArray hostArray = resObject.getJSONArray("result");
        //过滤items
        List<String> filterItems = new ArrayList<>();
        filterItems.add("system.uptime");
        filterItems.add("system.cpu.util"); //cpu利用率
        filterItems.add("agent.ping");
        filterItems.add("vfs.fs.size[/,pused]");
        filterItems.add("vfs.fs.size[C:,pused]");
        filterItems.add("vm.memory.size[pavailable]");//内存利用率
        for (int i = 0; i < hostArray.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            JSONObject host = hostArray.getJSONObject(i);
            String ip = host.getJSONArray("interfaces").getJSONObject(0).getString("ip");
            String hostid = host.getString("hostid");
            String hostname = host.getString("host");
            jsonObject.put("ip", ip);
            jsonObject.put("hostid", hostid);
            jsonObject.put("hostname", hostname);
            JSONArray items = host.getJSONArray("items");
            for (int j = 0; j < items.size(); j++) {
                JSONObject itemJson = items.getJSONObject(j);
                if (filterItems.contains(itemJson.getString("key_"))) {
                    jsonObject.put(itemJson.getString("key_"), itemJson.getString("lastvalue"));
                }
            }
            resultArray.add(jsonObject);
        }
        return resultArray;
    }

    /**
     * 通过application.get获取监控项最新数据
     */
    @Test
    public void test2() {
        boolean loginFlag = login();
        if (loginFlag) {
            getLatestItemData(null, null);
        }
    }

    public void getLatestItemData(List<String> hostIds, List<String> appNames) {
        JSONObject itemFilter = new JSONObject();
//        itemFilter.put("name", new String[]{"CPU", "Filesystem /","Filesystem /datastore","Memory"});
        List<String> list = new ArrayList<>();
        list.add("CPU");
        list.add("Filesystem /");
        list.add("Filesystem /datastore");
        list.add("Memory");
        itemFilter.put("name", list);
        Request request = RequestBuilder.newBuilder().method("application.get")
//                .paramEntry("groupids", "4")
                .paramEntry("hostids", new String[]{"10313", "10084", "10338"})
//                .paramEntry("output", new String[]{"hostid", "name", "host"})
//                .paramEntry("output", "extend")
//                .paramEntry("selectHost", "extend")
                .paramEntry("selectHost", new String[]{"host", "name"})
//                .paramEntry("selectItems", "extend")
                .paramEntry("selectItems", new String[]{"itemid", "name", "value_type", "lastclock", "prevvalue", "status", "units", "key_", "lastvalue"})
                .paramEntry("filter", itemFilter)
                .build();
        JSONObject resObject = zabbixApi.call(request);
        log.info("application.get.resObject={}", resObject);
    }
}
