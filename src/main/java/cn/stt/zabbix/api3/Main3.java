package cn.stt.zabbix.api3;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author shitt7
 * @Date 2020/7/9 13:48
 */
@Slf4j
public class Main3 {

    public void getHistoryData(){
        ZabbixApiUtil zabbixApiUtil = new ZabbixApiUtil("http://172.25.242.118/zabbix/api_jsonrpc.php");
        zabbixApiUtil.init();
        boolean login = zabbixApiUtil.login("Admin", "zabbix");
        if (login){
            //已知groupName和itemKey，求itemKey的历史数据
            String groupName = "znjk-server";
            String groupId = zabbixApiUtil.getGroupIdByName(groupName);
            //根据groupName获取其下的主机id列表
            ZabbixRequest request1 = ZabbixRequestBuilder.newBuilder().method("host.get")
//                    .paramEntry("output", "extend")
                    .paramEntry("output", "hostid")
                    .paramEntry("groupids", groupId)
//                .paramEntry("selectItems", "extend")
//                    .paramEntry("selectItems", "hostid")
//                    .paramEntry("selectItems", new String[]{"hostid"})
//                    .paramEntry("filter", itemFilter)
                    .build();
            JSONObject response1 = zabbixApiUtil.call(request1);
            log.info("host.get.response1={}", response1);

            //根据groupName获取其下的主机所有监控项列表，取itemId和key的对应关系
            ZabbixRequest request2 = ZabbixRequestBuilder.newBuilder().method("item.get")
//                    .paramEntry("output", "extend")
                    .paramEntry("output", new String[]{"itemid","key_"})
                    .paramEntry("groupids", groupId)
//                .paramEntry("selectItems", "extend")
//                    .paramEntry("selectItems", "hostid")
//                    .paramEntry("selectItems", new String[]{"hostid"})
//                    .paramEntry("filter", itemFilter)
                    .build();
            JSONObject response2 = zabbixApiUtil.call(request2);
            log.info("host.get.response2={}", response2);

            ZabbixRequest request = ZabbixRequestBuilder.newBuilder().method("history.get")
                    .paramEntry("output", "extend")
                    .paramEntry("history", 0)
                    .paramEntry("hostids", new String[]{"10313"})
                    // or .paramEntry("itemids", new String[]{""})
                    //timestamp 开始时间
                    .paramEntry("time_from", 1594275000)
                    //结束时间
                    .paramEntry("time_till", 1594275600)
                    //结果排序 itemid and clock
                    .paramEntry("sortfield", "clock")
                    //正序or倒序
                    .paramEntry("sortorder", "DESC")
//                .paramEntry("selectItems", "extend")
//                    .paramEntry("selectItems", new String[]{"itemid", "name", "value_type", "lastclock", "prevvalue", "status", "units", "key_", "lastvalue"})
//                    .paramEntry("filter", itemFilter)
                    .build();
            JSONObject response = zabbixApiUtil.call(request);
            log.info("history.get.response={}", response);
        }
    }

    public static void main(String[] args) {
        Main3 main3 = new Main3();
        main3.getHistoryData();
    }
}
