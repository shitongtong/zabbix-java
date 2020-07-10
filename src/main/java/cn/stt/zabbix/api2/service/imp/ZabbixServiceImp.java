package cn.stt.zabbix.api2.service.imp;


import cn.stt.zabbix.api2.dto.GraphConfig;
import cn.stt.zabbix.api2.dto.GraphItem;
import cn.stt.zabbix.api2.dto.ObjectMap;
import cn.stt.zabbix.api2.dto.PandoraConfig;
import cn.stt.zabbix.api2.dto.ProblemAcknowledge;
import cn.stt.zabbix.api2.dto.TriggerTags;
import cn.stt.zabbix.api2.dto.ZabbixApplication;
import cn.stt.zabbix.api2.dto.ZabbixApplicationSortByHostId;
import cn.stt.zabbix.api2.dto.ZabbixHistory;
import cn.stt.zabbix.api2.dto.ZabbixHost;
import cn.stt.zabbix.api2.dto.ZabbixItem;
import cn.stt.zabbix.api2.dto.ZabbixProblem;
import cn.stt.zabbix.api2.dto.ZabbixRequestParam;
import cn.stt.zabbix.api2.dto.ZabbixResponse;
import cn.stt.zabbix.api2.dto.ZabbixTrigger;
import cn.stt.zabbix.api2.service.ZabbixService;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wangzhen3
 * @version 2017/12/01
 */
@Service
public class ZabbixServiceImp implements ZabbixService {

    private final static Logger logger = LoggerFactory.getLogger(ZabbixServiceImp.class);
    //zabbix 授权码
    public static String authenticationToken = null;

    //RequestId 自增
    public static AtomicLong requestId = new AtomicLong(1);

    public static String zabbixUrl = "";

    private PandoraConfig pandoraConfig = new PandoraConfig();

    @Override
    public Boolean login() {
        synchronized (ZabbixServiceImp.class) {
            authenticationToken = null;
            Map<String, String> loginParam = new HashMap<>();

            //执行test 时，注释此块
            zabbixUrl = String.format("http://%s/zabbix/api_jsonrpc.php", pandoraConfig.getZabbixIp());
            loginParam.put("user", pandoraConfig.getZabbixUser());
            loginParam.put("password", pandoraConfig.getZabbixPassword());
            //执行test 时，注释此块

            //执行test 时，将此块解除注释
            //for Test -begin  test 用例时 无法引用 pandoraConfig
//            zabbixUrl = "http://10.6.129.20/zabbix/api_jsonrpc.php";
//            loginParam.put("user","Admin");
//            loginParam.put("password","zabbix");
            //for Test -end

            ZabbixRequestParam requestParam = new ZabbixRequestParam("user.login", loginParam);
            RestTemplate restTemplate = new RestTemplate();

            ZabbixResponse response = null;
            String failReason = "";
            do {
                try {
                    response = restTemplate.postForObject(zabbixUrl, requestParam, ZabbixResponse.class);
                } catch (Exception e) {
                    failReason = "http请求异常" + e.getMessage();
                    break;
                }

                if (responseResultAvailable(response)) {
                    authenticationToken = (String) response.getResult();
                    if (!StringUtils.isEmpty(authenticationToken)) {
                        logger.info("zabbix后台登陆成功! url= {}", zabbixUrl);
                        return true;
                    } else {
                        failReason = "返回token为空串!";
                        break;
                    }
                } else {
                    if (responseErrorAvailable(response)) {
                        failReason = response.getError().toString();
                    } else {
                        failReason = "返回结果为空!";
                    }
                }
            } while (false);

            logger.error("zabbix后台登陆失败! url= {} 失败原因:{}", zabbixUrl, failReason);

            return false;
        }

    }

    /**
     * 判断zabbix是否已 在线（可用，并已登陆），没有的话 尝试登陆，并刷新相关资源id
     *
     * @return 在线/重连成功，返回1；自系统启动首次登陆成功返回2； 不在线，且重连失败，返回0
     */
    @Override
    public Integer OnLine() {
        do {
            if (ObjectUtils.isEmpty(authenticationToken)) {
                logger.warn("zabbix 未登陆，尝试登陆!");
                if (!login()) {
                    logger.error("zabbix 登陆失败!");
                    break;
                }
                logger.info("zabbix登陆成功!(首次)");
                return 2;
            } else {
                //已登陆过，通过测试接口查看是否过期
                Map<String, Object> param = new HashMap<>();
                List<String> output = new ArrayList<>();
                output.add("groupid");
                output.add("name");
                param.put("output", output);
                ZabbixRequestParam requestParam = new ZabbixRequestParam("hostgroup.get", param);
                RestTemplate restTemplate = new RestTemplate();
                ZabbixResponse response = restTemplate.postForObject(zabbixUrl, requestParam, ZabbixResponse.class);
                if (!responseResultAvailable(response)) {
                    logger.warn("zabbix 登陆过期，尝试重新登陆!");
                    if (!login()) {
                        logger.error("zabbix 登陆失败!");
                        break;
                    }
                    logger.info("zabbix已重连!");
                } else {
                    logger.info("zabbix连接正常!");
                    return 1;
                }

            }

        } while (false);

        return 0;
    }

    @Override
    public String getTemplateIdByHostName(String tempLateHostName) {
        String templateId = "";
        Map filter = new HashMap() {{
            put("host", tempLateHostName);
        }};
        Map params = new HashMap() {{
            put("output", "templateid");
            put("filter", filter);
        }};
        ZabbixResponse response = requestZabbixForObject("template.get", params);
        if (responseResultAvailable(response)) {
            try {
                List templateList = (List) response.getResult();
                Map template = (Map) templateList.get(0);
                templateId = (String) template.get("templateid");
            } catch (Exception e) {
                logger.error("解析templateId失败!");
            }
        } else {
            logger.error("查询template[{}] Id失败!", tempLateHostName);
            if (responseErrorAvailable(response)) {
                logger.error("error:[{}]", response.getError().toString());
            }
        }
        return templateId;
    }

    @Override
    public String getGroupIdByName(String groupName) {
        String groupId = "";
        Map filter = new HashMap() {{
            put("name", new ArrayList() {{
                add(groupName);
            }});
        }};
        Map params = new HashMap() {{
            put("output", "groupid");
            put("filter", filter);
        }};
        ZabbixResponse response = requestZabbixForObject("hostgroup.get", params);
        if (responseResultAvailable(response)) {
            try {
                List groupList = (List) response.getResult();
                Map group = (Map) groupList.get(0);
                groupId = (String) group.get("groupid");
            } catch (Exception e) {
                logger.error("解析templateId失败!");
            }
        } else {
            logger.error("查询template[{}] Id失败!", groupName);
            if (responseErrorAvailable(response)) {
                logger.error("error:[{}]", response.getError().toString());
            }
        }
        return groupId;

    }

    @Override
    public Boolean importConfiguration(Document doc) {
        Boolean success = false;

        String sourceXml = doc.asXML();
        //logger.debug("请求导入模板信息[{}]",sourceXml);
        Map create = new HashMap() {{
            put("createMissing", true);
        }};
        Map update = new HashMap() {{
            put("updateExisting", true);
        }};
        Map delete = new HashMap() {{
            put("deleteMissing", true);
        }};
        Map createAndUpdate = new HashMap() {{
            putAll(create);
            putAll(update);
        }};
        Map createAndDelete = new HashMap() {{
            putAll(create);
            putAll(delete);
        }};
        Map createAndUpdateAndDelete = new HashMap() {{
            putAll(createAndUpdate);
            putAll(delete);
        }};

        Map<String, Object> rules = new HashMap<>();

        //设置 更新 删除 新增 规则，若不设true ，zabbix后台将不会做任何改动
        rules.put("groups", create);
        rules.put("templates", createAndUpdate);
        rules.put("applications", createAndDelete);
        rules.put("items", createAndUpdateAndDelete);
        rules.put("discoveryRules", createAndUpdateAndDelete);
        rules.put("triggers", createAndUpdateAndDelete);
        rules.put("graphs", createAndUpdateAndDelete);
        rules.put("httptests", createAndUpdate);
        rules.put("valueMaps", createAndUpdate);

        Map<String, Object> params = new HashMap<>();
        params.put("format", "xml");
        params.put("rules", rules);
        params.put("source", sourceXml);

        ZabbixResponse response = requestZabbixForObject("configuration.import", params);
        if (responseResultAvailable(response)) {
            success = (Boolean) response.getResult();
            if (!success) {
                logger.error("导入模板信息失败!{}", params);
            }
        } else {
            logger.error("importConfiguration 请求zabbix后台失败!{}", params);
            if (responseErrorAvailable(response)) {
                logger.error("error[{}]", response.getError().toString());
            }
        }
        return success;
    }

    @Override
    public ZabbixResponse requestZabbixForObject(String zabbixMethod, Object zabbixParams) {

        ZabbixRequestParam requestParam = new ZabbixRequestParam(zabbixMethod, zabbixParams);
        RestTemplate restTemplate = new RestTemplate();
        //   经测试，可以不用重写head ,默认的head 中ContentType 就是application/json;charset=UTF-8
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));//
//        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
//
//        HttpEntity<ZabbixRequestParam> formEntity = new HttpEntity<>(requestParam,headers);
        ZabbixResponse response = null;
        try {
            response = restTemplate.postForObject(zabbixUrl, requestParam, ZabbixResponse.class);
        } catch (Exception e) {
            //尝试登陆，以重连
            if (!login()) {
                logger.error("RestClientException,请求zabbix后台{}失败!", zabbixUrl);
                return response;
            }
            //登陆成功，重新请求一次
            try {
                requestParam.reloadAuthentication();
                response = restTemplate.postForObject(zabbixUrl, requestParam, ZabbixResponse.class);
            } catch (Exception e2) {
                logger.error("请求失败{} zabbixMethod={}", zabbixUrl, zabbixMethod);
            }
        }
        if (responseErrorAvailable(response)) {
            if (response.getError().getData().contains("Not authorised")
                    || response.getError().getData().contains("Session terminated")) {
                logger.warn("session验证失败!error:{},正在尝试重新登录!", response.getError().toString());
                //尝试登陆，以重连
                if (!login()) {
                    logger.error("尝试重新登录zabbix后台{}失败!", zabbixUrl);
                    return response;
                }
                //登陆成功，重新请求一次
                try {
                    requestParam.reloadAuthentication();
                    response = restTemplate.postForObject(zabbixUrl, requestParam, ZabbixResponse.class);
                } catch (Exception e3) {
                    logger.error("请求失败{} zabbixMethod={}", zabbixUrl, zabbixMethod);
                }
            }
        }

        return response;
    }

    @Override
    public List<String> createHost(List<String> hostIps, String groupId) {

        List<String> hostIds = new ArrayList<>();
        if (ObjectUtils.isEmpty(hostIps)) return hostIds;

        List params = new ArrayList();
        for (String ip : hostIps) {
            List groups = new ArrayList() {{
                add(new HashMap() {{
                    put("groupid", groupId);
                }});
            }};
            Map interfaces = new HashMap() {
                {
                    put("type", 1);
                    put("main", 1);
                    put("useip", 1);
                    put("ip", ip);
                    put("port", "10050");
                    put("dns", "");
                }
            };
            Map hostParam = new HashMap() {
                {
                    put("host", ip);
                    put("interfaces", interfaces);
                    put("groups", groups);
                }
            };
            params.add(hostParam);
        }

        ZabbixResponse response = requestZabbixForObject("host.create", params);
        if (responseResultAvailable(response)) {
            try {
                Map hostMap = (Map) response.getResult();
                hostIds = (List<String>) hostMap.get("hostids");
            } catch (Exception e) {
                logger.error("解析hostids失败!");
            }
        } else {
            logger.error("创建host失败!");
            if (responseErrorAvailable(response)) {
                logger.error("error:[{}]", response.getError().toString());
            }
        }
        return hostIds;
    }

    @Override
    public Map<String, String> getHostAll() {
        Map<String, String> IpMapId = new HashMap<>();
        Map params = new HashMap() {{
            put("output", new ArrayList() {{
                add("hostid");
                add("host");
            }});
        }};

        ZabbixResponse response = requestZabbixForObject("host.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                if (!ObjectUtils.isEmpty(result)) {
                    for (Map<String, String> host : result) {
                        IpMapId.put(host.get("host"), host.get("hostid"));
                    }
                }
            } catch (Exception e) {
                logger.error("getHostAll失败!解析result失败!param={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getHostAll失败!error:[{}],param={}", response.getError().toString(), params);
            } else {
                logger.error("getHostAll失败!返回为空!param={}", params);
            }
        }
        return IpMapId;
    }

    @Override
    public Boolean templateMassAdd(String templateId, List<String> hostIds) {
        if (ObjectUtils.isEmpty(hostIds)) return true;
        Map params = new HashMap() {{
            put("templates", new ArrayList() {{
                add(new HashMap() {{
                    put("templateid", templateId);
                }});
            }});
            put("hosts", new ArrayList() {{
                for (String hostId : hostIds) {
                    add(new HashMap() {{
                        put("hostid", hostId);
                    }});
                }
            }});
        }};
        ZabbixResponse response = requestZabbixForObject("template.massadd", params);
        if (responseResultAvailable(response)) {
            try {
                Map templateMap = (Map) response.getResult();
                List templateIds = (List<String>) templateMap.get("templateids");
                String templateIdReturn = (String) templateIds.get(0);
                if (templateIdReturn.equals(templateId)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("解析返回值templateids失败!");
            }
        } else {
            logger.error("模板关联机器失败!templateId={},hostIds={}", templateId, hostIds.toString());
            if (responseErrorAvailable(response)) {
                logger.error("error:[{}]", response.getError().toString());
            }
        }

        return false;

    }

    @Override
    public Boolean hostMassRemoveTemplate(List<String> hostIds, String templateId) {
        if (ObjectUtils.isEmpty(hostIds)) return true;
        Map params = new HashMap() {{
            put("hostids", new ArrayList() {{
                for (String hostId : hostIds) {
                    add(hostId);
                }
            }});
            put("templateids_clear", templateId);
        }};
        ZabbixResponse response = requestZabbixForObject("host.massremove", params);
        if (responseResultAvailable(response)) {
            try {
                Map hostIdsMap = (Map) response.getResult();
                List<String> hostIdsReturn = (List) hostIdsMap.get("hostids");
                if (hostIds.equals(hostIdsReturn))
                    return true;
            } catch (Exception e) {
                logger.error("解析返回值hostIds失败!");
            }
        } else {
            logger.error("机器解除模板关联失败!");
            if (responseErrorAvailable(response)) {
                logger.error("error:[{}]", response.getError().toString());
            }
        }
        return false;

    }

    @Override
    public List<ZabbixHost> getHosts(Map params) {
        List<ZabbixHost> servers = new ArrayList<>();
        if (CollectionUtils.isEmpty(params)) return servers;
        params.put("output", new ArrayList() {{
            add("hostid");
            add("host");
        }});

        ZabbixResponse response = requestZabbixForObject("host.get", params);
        if (responseResultAvailable(response)) {
            try {
                List serverList = (List) response.getResult();
                for (Object zabbixServer : serverList) {
                    ZabbixHost server = new ZabbixHost();
                    server.setHostId((String) ((Map) zabbixServer).get("hostid"));
                    server.setHostIp((String) ((Map) zabbixServer).get("host"));
                    servers.add(server);
                }
            } catch (Exception e) {
                logger.error("getHosts解析返回值失败!");
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getHosts获取模板关联的机器列表失败!error:[{}]", response.getError().toString());
            } else {
                logger.debug("getHosts返回值为空");
            }
        }
        return servers;
    }

    @Override
    public Boolean templateMassDelete(List<String> templateIds) {
        if (ObjectUtils.isEmpty(templateIds)) return true;
        List params = new ArrayList() {{
            for (String id : templateIds) {
                add(id);
            }
        }};

        ZabbixResponse response = requestZabbixForObject("template.delete", params);
        if (responseResultAvailable(response)) {
            try {
                Map resultMap = (Map) response.getResult();
                List<String> templateidsReturn = (List) resultMap.get("templateids");
                if (templateidsReturn.equals(templateIds)) {
                    logger.info("zabbix删除模板成功!templateids={}", templateidsReturn.toString());
                    return true;
                }
            } catch (Exception e) {
                logger.error("zabbix删除模板失败!解析返回值异常!templateids={},result={}", templateIds.toString(), response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                if (response.getError().getCode().equals(-32500)) {
                    logger.info("zabbix不存在模板!templateids={}", templateIds.toString());
                    return true;
                } else {
                    logger.error("zabbix删除模板失败!error:[{}]", response.getError().toString());
                }
            } else {
                logger.error("zabbix删除模板失败,返回为空!templateids={}", templateIds.toString());
            }
        }
        return false;
    }

    @Override
    public Boolean hostDelete(List<String> hostIds) {
        if (ObjectUtils.isEmpty(hostIds)) return true;

        List params = new ArrayList() {{
            for (String id : hostIds) {
                add(id);
            }
        }};

        ZabbixResponse response = requestZabbixForObject("host.delete", params);
        if (responseResultAvailable(response)) {
            try {
                Map resultMap = (Map) response.getResult();
                List<String> hostIdsReturn = (List) resultMap.get("hostids");
                if (hostIdsReturn.equals(hostIds)) {
                    logger.info("zabbix删除机器成功!ids={}", hostIds.toString());
                    return true;
                }
            } catch (Exception e) {
                logger.error("zabbix删除机器失败!解析返回值异常!hostIds={},result={}", hostIds.toString(), response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                if (response.getError().getCode().equals(-32500)) {
                    logger.info("zabbix已不存在机器!ids={}", hostIds.toString());
                    return true;
                } else {
                    logger.error("zabbix删除机器失败!error:[{}]", response.getError().toString());
                }
            } else {
                logger.error("zabbix删除机器失败,返回为空!ids={}", hostIds.toString());
            }
        }
        return false;

    }

    @Override
    public List<ZabbixItem> getItems(Map params) {
        List<ZabbixItem> zabbixItems = new ArrayList<>();
        params.put("output", new ArrayList() {{
            add("itemid");
            add("name");
            add("key_");
            add("delay");
            add("history");
            add("status");
            add("flags");
            add("units");
            add("lastvalue");
            add("value_type");
        }});
        params.put("selectApplications", new ArrayList() {{
            add("applicationid");
            add("name");
            add("hostid");
            add("templateids");
        }});
        params.put("selectTriggers", new ArrayList() {{
            add("triggerid");
            add("templateid");
            add("description");
            add("expression");
            add("comments");
            add("status");
            add("priority");
            add("value");
        }});
        params.put("selectHosts", new ArrayList() {{
            add("hostid");
            add("host");
        }});


        ZabbixResponse response = requestZabbixForObject("item.get", params);
        if (responseResultAvailable(response)) {
            try {
                List resultList = (List) response.getResult();
                if (!ObjectUtils.isEmpty(resultList)) {
                    for (Object itemObject : resultList) {
                        ObjectMap itemMap = new ObjectMap((Map) itemObject);
                        ZabbixItem item = new ZabbixItem();
                        item.setItemId(itemMap.getString("itemid"));
                        item.setName(itemMap.getString("name"));
                        item.setKey(itemMap.getString("key_"));
                        item.setDelay(getIntegerBefore(itemMap.getString("delay"), "s"));
                        item.setHistory(getIntegerBefore(itemMap.getString("history"), "d"));
                        item.setStatus(Integer.valueOf(itemMap.getString("status")));
                        item.setFlags(Integer.valueOf(itemMap.getString("flags")));
                        item.setUnits(itemMap.getString("units"));
                        item.setLastValue(itemMap.getString("lastvalue"));
                        item.setValueType(itemMap.getInteger("value_type"));

                        List applications = itemMap.getList("applications");
                        if (ObjectUtils.isEmpty(applications)) continue;
                        ObjectMap applicationMap = new ObjectMap((Map) applications.get(0));
                        ZabbixApplication application = new ZabbixApplication();
                        application.setApplicationId(applicationMap.getString("applicationid"));
                        application.setName(applicationMap.getString("name"));
                        application.setHostId(applicationMap.getString("hostid"));
                        List templateIds = applicationMap.getList("templateids");
                        if (!ObjectUtils.isEmpty(templateIds)) {
                            application.setParentId((String) templateIds.get(0));
                        } else {
                            application.setParentId(null);
                        }
                        item.setApplication(application);

                        List triggers = itemMap.getList("triggers");
                        if (!ObjectUtils.isEmpty(triggers)) {
                            for (Object trigger : triggers) {
                                Map<String, String> triggerMap = (Map) trigger;
                                ZabbixTrigger zabbixTrigger = new ZabbixTrigger();
                                zabbixTrigger.setTriggerId(triggerMap.get("triggerid"));
                                zabbixTrigger.setDescription(triggerMap.get("description"));
                                zabbixTrigger.setTemplateId(triggerMap.get("templateid"));
                                zabbixTrigger.setExpression(triggerMap.get("expression"));
                                zabbixTrigger.setStatus(Integer.valueOf(triggerMap.get("status")));
                                zabbixTrigger.setComments(triggerMap.get("comments"));
                                zabbixTrigger.setPriority(Integer.valueOf(triggerMap.get("priority")));
                                zabbixTrigger.setValue(Integer.valueOf(triggerMap.get("value")));
                                item.getTriggers().add(zabbixTrigger);
                            }

                        }
                        List<Map> hosts = itemMap.getList("hosts");
                        if (!ObjectUtils.isEmpty(hosts)) {
                            ObjectMap hostMap = new ObjectMap(hosts.get(0));
                            item.setHostId(hostMap.getString("hostid"));
                            item.setHostIp(hostMap.getString("host"));
                        }
                        zabbixItems.add(item);
                    }
                }

            } catch (Exception e) {
                logger.error("查询zabbixItems失败!解析返回值异常!params={},result={},exception={}", params, response.getResult().toString(), e);
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("查询zabbixItems失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("查询zabbixItems失败!,返回为空!params={}", params);
            }
        }
        return zabbixItems;
    }

    @Override
    public Map<String, ZabbixItem> getItemData(Map params) {
        Map<String, ZabbixItem> zabbixItems = new HashMap<>();
        params.put("output", new ArrayList() {{
            add("itemid");
            add("name");
            add("lastclock");
            add("lastvalue");
            add("prevvalue");
            add("status");
            add("units");
        }});
        params.put("selectApplications", new ArrayList() {{
            add("applicationid");
            add("name");
            add("hostid");
            add("templateids");
        }});
        params.put("selectHosts", new ArrayList() {{
            add("hostid");
            add("host");
        }});

        ZabbixResponse response = requestZabbixForObject("item.get", params);
        if (responseResultAvailable(response)) {
            try {
                List resultList = (List) response.getResult();
                if (!ObjectUtils.isEmpty(resultList)) {
                    for (Object itemObject : resultList) {
                        ObjectMap itemMap = new ObjectMap((Map) itemObject);
                        ZabbixItem item = new ZabbixItem();
                        item.setItemId(itemMap.getString("itemid"));
                        item.setName(itemMap.getString("name"));
                        item.setLastClock(itemMap.getString("lastclock"));
                        item.setLastValue(itemMap.getString("lastvalue"));
                        item.setPreValue(itemMap.getString("prevvalue"));
                        item.setStatus(Integer.valueOf(itemMap.getString("status")));
                        item.setUnits(itemMap.getString("units"));

                        List<Map> hosts = itemMap.getList("hosts");
                        if (!ObjectUtils.isEmpty(hosts)) {
                            ObjectMap hostMap = new ObjectMap(hosts.get(0));
                            item.setHostId(hostMap.getString("hostid"));
                            item.setHostIp(hostMap.getString("host"));
                        }

                        List applications = itemMap.getList("applications");
                        if (!ObjectUtils.isEmpty(applications)) {
                            ObjectMap applicationMap = new ObjectMap((Map) applications.get(0));
                            ZabbixApplication application = new ZabbixApplication();
                            application.setApplicationId(applicationMap.getString("applicationid"));
                            application.setName(applicationMap.getString("name"));
                            application.setHostId(applicationMap.getString("hostid"));
                            List templateIds = applicationMap.getList("templateids");
                            if (!ObjectUtils.isEmpty(templateIds)) {
                                application.setParentId((String) templateIds.get(0));
                            } else {
                                application.setParentId(null);
                            }
                            item.setApplication(application);
                        }
                        zabbixItems.put(item.getItemId(), item);
                    }
                }

            } catch (Exception e) {
                logger.error("getItemData失败!解析返回值异常!params={},result={},exception={}", params, response.getResult().toString(), e);
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getItemData失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getItemData失败!,返回为空!params={}", params);
            }
        }
        return zabbixItems;
    }

    @Override
    public ZabbixApplication getApplicationById(String Id) {
        ZabbixApplication application = null;

        Map params = new HashMap() {{
            put("output", "extend");
            put("applicationids", Id);
        }};

        ZabbixResponse response = requestZabbixForObject("application.get", params);
        if (responseResultAvailable(response)) {
            try {
                List resultList = (List) response.getResult();
                Map applicationMap = (Map) resultList.get(0);
                application = new ZabbixApplication();
                application.setApplicationId((String) applicationMap.get("applicationid"));
                application.setHostId((String) applicationMap.get("hostid"));
                application.setName((String) applicationMap.get("name"));
                List templateIds = (List) applicationMap.get("templateids");
                if (!ObjectUtils.isEmpty(templateIds)) {
                    application.setParentId((String) templateIds.get(0));
                } else {
                    application.setParentId(null);
                }
            } catch (Exception e) {
                logger.error("查询zabbixApplication失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("查询zabbixApplication失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("查询zabbixApplication失败!,返回为空!params={}", params);
            }
        }
        return application;

    }

    @Override
    public Map<String, ZabbixApplication> getApplicationsByIds(List<String> ids) {
        Map<String, ZabbixApplication> zabbixApplicationMap = new HashMap();
        if (ObjectUtils.isEmpty(ids)) return zabbixApplicationMap;

        Map params = new HashMap() {{
            put("output", "extend");
            put("applicationids", new ArrayList() {{
                for (String id : ids) {
                    add(id);
                }
            }});
        }};

        ZabbixResponse response = requestZabbixForObject("application.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> resultList = (List) response.getResult();
                for (Map applicationMap : resultList) {
                    ZabbixApplication application = new ZabbixApplication();
                    application.setApplicationId((String) applicationMap.get("applicationid"));
                    application.setHostId((String) applicationMap.get("hostid"));
                    application.setName((String) applicationMap.get("name"));
                    List templateIds = (List) applicationMap.get("templateids");
                    if (!ObjectUtils.isEmpty(templateIds)) {
                        application.setParentId((String) templateIds.get(0));
                    } else {
                        application.setParentId(null);
                    }
                    zabbixApplicationMap.put(application.getApplicationId(), application);
                }

            } catch (Exception e) {
                logger.error("查询zabbixApplications失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("查询zabbixApplications失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("查询zabbixApplications失败!,返回为空!params={}", params);
            }
        }
        return zabbixApplicationMap;
    }

    @Override
    public List<String> getApplicationNamesByTemplateIds(List<String> templateIds) {
        List<String> appNames = new ArrayList<>();
        if (ObjectUtils.isEmpty(templateIds)) return appNames;

        Map params = new HashMap() {{
            put("output", new ArrayList() {{
                add("applicationid");
                add("name");
            }});
            put("templateids", templateIds);
        }};

        ZabbixResponse response = requestZabbixForObject("application.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> resultList = (List) response.getResult();
                for (Map applicationMap : resultList) {
                    appNames.add((String) applicationMap.get("name"));
                }
            } catch (Exception e) {
                logger.error("getApplicationsByTemplateIds失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getApplicationsByTemplateIds失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getApplicationsByTemplateIds失败!,返回为空!params={}", params);
            }
        }
        return appNames;
    }

    @Override
    public Map<String, ZabbixTrigger> getTriggers(Map params) {
        Map<String, ZabbixTrigger> triggerMapId = new HashMap<>();
        if (params == null) return triggerMapId;

        params.put("output", new ArrayList() {{
            add("triggerid");
            add("description");
            add("priority");
            add("value");
            add("expression");
            add("status");
            add("comments");
        }});
        params.put("selectHosts", new ArrayList() {{
            add("host");
            add("hostid");
        }});
        params.put("selectItems", new ArrayList() {{
            add("itemid");
            add("lastvalue");
            add("units");
        }});

        ZabbixResponse response = requestZabbixForObject("trigger.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> resultList = (List) response.getResult();
                for (Map triggerObject : resultList) {
                    ObjectMap triggerMap = new ObjectMap(triggerObject);
                    ZabbixTrigger trigger = new ZabbixTrigger();
                    trigger.setTriggerId(triggerMap.getString("triggerid"));
                    trigger.setDescription(triggerMap.getString("description"));
                    trigger.setPriority(triggerMap.getInteger("priority"));
                    trigger.setValue(triggerMap.getInteger("value"));
                    trigger.setExpression(triggerMap.getString("expression"));
                    trigger.setStatus(triggerMap.getInteger("status"));
                    trigger.setComments(triggerMap.getString("comments"));
                    List<Map> tags = (List) triggerMap.getObject("tags");
                    if (!ObjectUtils.isEmpty(tags)) {
                        ObjectMap tagsMap = new ObjectMap(tags.get(0));
                        TriggerTags triggerTags = new TriggerTags();
                        triggerTags.setTag(tagsMap.getString("tag"));
                        triggerTags.setValue(tagsMap.getString("value"));
                        trigger.getTags().add(triggerTags);
                    }
                    List<Map> hosts = (List) triggerMap.getObject("hosts");
                    if (!CollectionUtils.isEmpty(hosts)) {
                        ObjectMap hostMap = new ObjectMap(hosts.get(0));
                        trigger.setHostId(hostMap.getString("hostid"));
                        trigger.setHostIp(hostMap.getString("host"));
                    }
                    List<Map> items = (List) triggerMap.getObject("items");
                    if (!CollectionUtils.isEmpty(items)) {
                        ObjectMap itemMap = new ObjectMap(items.get(0));
                        trigger.setItemId(itemMap.getString("itemid"));
                        trigger.setItemLastValue(itemMap.getString("lastvalue"));
                        trigger.setItemUnits(itemMap.getString("units"));
                    }
                    triggerMapId.put(trigger.getTriggerId(), trigger);
                }
            } catch (Exception e) {
                logger.error("getTriggers失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else if (responseErrorAvailable(response)) {
            logger.error("getTriggers失败!params={},error:[{}]", params, response.getError().toString());
        } else {
            logger.error("getTriggers失败!,返回为空!params={}", params);
        }

        return triggerMapId;
    }

    @Override
    public Boolean itemsUpdate(List<ZabbixItem> items) {
        if (ObjectUtils.isEmpty(items)) return true;
        List params = new ArrayList() {{
            for (ZabbixItem item : items) {
                add(new HashMap() {{
                    put("itemid", item.getItemId());
                    if (!ObjectUtils.isEmpty(item.getDelay())) put("delay", item.getDelay() + "s");
                    if (!ObjectUtils.isEmpty(item.getHistory())) put("history", item.getHistory() + "d");
                    if (!ObjectUtils.isEmpty(item.getStatus())) put("status", item.getStatus().toString());
                }});
            }
        }};

        ZabbixResponse response = requestZabbixForObject("item.update", params);
        if (responseResultAvailable(response)) {
            try {
                Map resultMap = (Map) response.getResult();
                List<String> itemIds = (List) resultMap.get("itemids");
                for (int i = 0; i < itemIds.size(); i++) {
                    if (!itemIds.get(i).equals(items.get(i).getItemId())) {
                        logger.error("更改items失败!返回值不匹配!param={},result={}", params, response.getResult().toString());
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error("更改items失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
                return false;
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("更改items失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("更改items失败!,返回为空!params={}", params);
            }
            return false;
        }
        return true;
    }

    @Override
    public Boolean triggersUpdate(List<ZabbixTrigger> triggers) {
        if (ObjectUtils.isEmpty(triggers)) return true;
        List params = new ArrayList() {{
            for (ZabbixTrigger trigger : triggers) {
                add(new HashMap() {{
                    put("triggerid", trigger.getTriggerId());
                    if (!ObjectUtils.isEmpty(trigger.getComments())) put("comments", trigger.getComments());
                    if (!ObjectUtils.isEmpty(trigger.getStatus())) put("status", trigger.getStatus().toString());
                    if (!ObjectUtils.isEmpty(trigger.getExpression())) put("expression", trigger.getExpression());
                }});
            }
        }};

        ZabbixResponse response = requestZabbixForObject("trigger.update", params);
        if (responseResultAvailable(response)) {
            try {
                Map resultMap = (Map) response.getResult();
                List<String> triggerIds = (List) resultMap.get("triggerids");
                for (int i = 0; i < triggerIds.size(); i++) {
                    if (!triggerIds.get(i).equals(triggers.get(i).getTriggerId())) {
                        logger.error("更改triggers失败!返回值不匹配!param={},result={}", params, response.getResult().toString());
                        return false;
                    }
                }
            } catch (Exception e) {
                logger.error("更改triggers失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
                return false;
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("更改triggers失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("更改triggers失败!,返回为空!params={}", params);
            }
            return false;
        }
        return true;
    }

    @Override
    public List<ZabbixApplication> getApplicationAndItemOrgByHostId(List<String> hostIds) {
        List<ZabbixApplication> zabbixApplicationList = new ArrayList<>();
        if (ObjectUtils.isEmpty(hostIds)) return zabbixApplicationList;

        Map params = new HashMap() {{
            put("hostids", new ArrayList() {{
                for (String hostId : hostIds) {
                    add(hostId);
                }
            }});

            put("selectItems", new ArrayList() {{
                add("itemid");
                add("name");
            }});
        }};

        ZabbixResponse response = requestZabbixForObject("application.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                for (Map application : result) {
                    ZabbixApplication zabbixApplication = new ZabbixApplication();
                    zabbixApplication.setApplicationId((String) application.get("applicationid"));
                    zabbixApplication.setHostId((String) application.get("hostid"));
                    zabbixApplication.setName((String) application.get("name"));
                    List templateIds = (List) application.get("templateids");
                    if (!ObjectUtils.isEmpty(templateIds)) {
                        zabbixApplication.setParentId((String) templateIds.get(0));
                    } else {
                        zabbixApplication.setParentId(null);
                    }
                    List<Map> items = (List) application.get("items");
                    if (!ObjectUtils.isEmpty(items)) {
                        for (Map item : items) {
                            ZabbixItem zabbixItem = new ZabbixItem();
                            zabbixItem.setItemId((String) item.get("itemid"));
                            zabbixItem.setName((String) item.get("name"));
                            zabbixApplication.getItems().add(zabbixItem);
                        }
                    }
                    zabbixApplicationList.add(zabbixApplication);
                }

            } catch (Exception e) {
                logger.error("请求application-item-org失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("请求application-item-org失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("请求application-item-org失败!,返回为空!params={}", params);
            }
        }
        return zabbixApplicationList;

    }

    @Override
    public List<ZabbixApplication> getLatestItemData(List<String> hostIds, List<String> appNames) {
        if (ObjectUtils.isEmpty(hostIds)) return new ArrayList<>();
        List<ZabbixApplication> zabbixApplicationList = new ArrayList<>();
        Map params = new HashMap() {{
            put("hostids", new ArrayList() {{
                for (String hostId : hostIds) {
                    add(hostId);
                }
            }});

            put("selectItems", new ArrayList() {{
                add("itemid");
                add("name");
                add("value_type");
                add("lastclock");
                add("lastvalue");
                add("prevvalue");
                add("status");
                add("units");
            }});

            if (!ObjectUtils.isEmpty(appNames)) {
                put("filter", new HashMap() {{
                    put("name", new ArrayList() {{
                        for (String name : appNames) {
                            add(name);
                        }
                    }});
                }});
            }
        }};

        ZabbixResponse response = requestZabbixForObject("application.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                for (Map appReturn : result) {
                    ObjectMap appMap = new ObjectMap(appReturn);
                    ZabbixApplication application = new ZabbixApplication();
                    application.setApplicationId(appMap.getString("applicationid"));
                    application.setHostId(appMap.getString("hostid"));
                    application.setName(appMap.getString("name"));
                    List templateIds = appMap.getList("templateids");
                    if (!ObjectUtils.isEmpty(templateIds)) {
                        application.setParentId((String) templateIds.get(0));
                    } else {
                        application.setParentId(null);
                    }
                    List<Map> items = (List) appReturn.get("items");
                    if (!ObjectUtils.isEmpty(items)) {
                        for (Map itemReturn : items) {
                            ObjectMap itemMap = new ObjectMap(itemReturn);
                            if (itemMap.getInteger("status").equals(1)) continue;//过滤掉 已被禁用的监控项数据
                            ZabbixItem zabbixItem = new ZabbixItem();
                            zabbixItem.setItemId(itemMap.getString("itemid"));
                            zabbixItem.setName(itemMap.getString("name"));
                            zabbixItem.setValueType(itemMap.getInteger("value_type"));
                            zabbixItem.setUnits(itemMap.getString("units"));
                            zabbixItem.setLastClock(itemMap.getString("lastclock"));
                            zabbixItem.setLastValue(itemMap.getString("lastvalue"));
                            zabbixItem.setPreValue(itemMap.getString("prevvalue"));
                            application.getItems().add(zabbixItem);
                        }
                    }
                    zabbixApplicationList.add(application);
                }
                if (!ObjectUtils.isEmpty(zabbixApplicationList)) {
                    zabbixApplicationList.sort(new ZabbixApplicationSortByHostId());
                }

            } catch (Exception e) {
                logger.error("请求application-item-data失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("请求application-item-data失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("请求application-item-data失败!,返回为空!params={}", params);
            }
        }
        return zabbixApplicationList;
    }

    @Override
    public List<ZabbixProblem> getProblems(Map params) {
        List<ZabbixProblem> zabbixProblemList = new ArrayList<>();
        if (params == null) return zabbixProblemList;

        params.put("output", "extend");
        params.put("recent", "true");
        params.put("selectAcknowledges", "extend");
        params.put("sortfield", new ArrayList() {{
            add("eventid");
        }});
        params.put("sortorder", "DESC");

        ZabbixResponse response = requestZabbixForObject("problem.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                for (Map object : result) {
                    ObjectMap problem = new ObjectMap(object);
                    ZabbixProblem zabbixProblem = new ZabbixProblem();
                    zabbixProblem.setEventId(problem.getString("eventid"));
                    zabbixProblem.setSource(problem.getInteger("source"));
                    zabbixProblem.setObject(problem.getInteger("object"));
                    zabbixProblem.setObjectId(problem.getString("objectid"));
                    zabbixProblem.setClock(problem.getString("clock"));
                    zabbixProblem.setNs(problem.getString("ns"));
                    zabbixProblem.setRecoveryEventId(problem.getString("r_eventid"));
                    zabbixProblem.setRecoveryEventClock(problem.getString("r_clock"));
                    zabbixProblem.setRecoveryEventNs(problem.getString("r_ns"));
                    zabbixProblem.setCorrelationId(problem.getString("correlationid"));
                    zabbixProblem.setUserId(problem.getString("userid"));

                    List<Map> acknowledgeList = problem.getList("acknowledges");
                    if (!ObjectUtils.isEmpty(acknowledgeList)) {
                        for (Map object2 : acknowledgeList) {
                            ObjectMap acknowledge = new ObjectMap(object2);
                            ProblemAcknowledge problemAcknowledge = new ProblemAcknowledge();
                            problemAcknowledge.setId(acknowledge.getString("acknowledgeid"));
                            problemAcknowledge.setUserId(acknowledge.getString("userid"));
                            problemAcknowledge.setEventId(acknowledge.getString("eventid"));
                            problemAcknowledge.setClock(acknowledge.getString("clock"));
                            problemAcknowledge.setMessage(acknowledge.getString("message"));
                            problemAcknowledge.setAction(acknowledge.getInteger("action"));
                            zabbixProblem.getAcknowledges().add(problemAcknowledge);
                        }
                    }
                    zabbixProblemList.add(zabbixProblem);
                }

            } catch (Exception e) {
                logger.error("getProblems失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getProblems失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getProblems失败!,返回为空!params={}", params);
            }
        }
        return zabbixProblemList;
    }

    @Override
    public List<ZabbixProblem> getEvents(Map params) {
        List<ZabbixProblem> zabbixProblemList = new ArrayList<>();
        if (params == null) return zabbixProblemList;

        params.put("output", "extend");
        params.put("sortfield", new ArrayList() {{
            add("clock");
            add("eventid");
        }});
        params.put("sortorder", "DESC");
        params.put("select_acknowledges", "extend");
        params.put("selectRelatedObject", new ArrayList() {{
            add("triggerid");
            add("description");
            add("priority");
            add("value");
        }});
        params.put("selectHosts", new ArrayList() {{
            add("hostid");
            add("host");
        }});

        ZabbixResponse response = requestZabbixForObject("event.get", params);
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                for (Map object : result) {
                    ObjectMap event = new ObjectMap(object);
                    ZabbixProblem zabbixProblem = new ZabbixProblem();
                    zabbixProblem.setEventId(event.getString("eventid"));
                    zabbixProblem.setSource(event.getInteger("source"));
                    zabbixProblem.setObject(event.getInteger("object"));
                    zabbixProblem.setObjectId(event.getString("objectid"));
                    zabbixProblem.setClock(event.getString("clock"));
                    zabbixProblem.setValue(event.getInteger("value"));
                    zabbixProblem.setNs(event.getString("ns"));
                    zabbixProblem.setRecoveryEventId(event.getString("r_eventid"));
                    zabbixProblem.setRecoveryEventClock(event.getString("r_clock"));
                    zabbixProblem.setRecoveryEventNs(event.getString("r_ns"));
                    zabbixProblem.setCorrelationId(event.getString("correlationid"));
                    zabbixProblem.setUserId(event.getString("userid"));

                    List<Map> acknowledgeList = event.getList("acknowledges");
                    if (!ObjectUtils.isEmpty(acknowledgeList)) {
                        for (Map object2 : acknowledgeList) {
                            ObjectMap acknowledge = new ObjectMap(object2);
                            ProblemAcknowledge problemAcknowledge = new ProblemAcknowledge();
                            problemAcknowledge.setId(acknowledge.getString("acknowledgeid"));
                            problemAcknowledge.setUserId(acknowledge.getString("userid"));
                            problemAcknowledge.setEventId(acknowledge.getString("eventid"));
                            problemAcknowledge.setClock(acknowledge.getString("clock"));
                            problemAcknowledge.setMessage(acknowledge.getString("message"));
                            problemAcknowledge.setAction(acknowledge.getInteger("action"));
                            zabbixProblem.getAcknowledges().add(problemAcknowledge);
                        }
                    }

                    if (!ObjectUtils.isEmpty(event.getObject("relatedObject"))) {
                        ObjectMap triggerMap = new ObjectMap(event.getMap("relatedObject"));
                        ZabbixTrigger trigger = new ZabbixTrigger();
                        trigger.setTriggerId(triggerMap.getString("triggerid"));
                        trigger.setDescription(triggerMap.getString("description"));
                        trigger.setPriority(triggerMap.getInteger("priority"));
                        trigger.setValue(triggerMap.getInteger("value"));
                        zabbixProblem.setRelatedObject(trigger);
                    }

                    List<Map> hostList = event.getList("hosts");
                    if (!ObjectUtils.isEmpty(hostList)) {
                        ObjectMap hostMap = new ObjectMap(hostList.get(0));
                        if (zabbixProblem.getRelatedObject() != null) {
                            zabbixProblem.getRelatedObject().setHostId(hostMap.getString("hostid"));
                            zabbixProblem.getRelatedObject().setHostIp(hostMap.getString("host"));
                        }
                    }

                    zabbixProblemList.add(zabbixProblem);
                }

            } catch (Exception e) {
                logger.error("getEvents失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getEvents失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getEvents失败!,返回为空!params={}", params);
            }
        }
        return zabbixProblemList;
    }

    public Boolean acknowledgeEvents(List<String> events) {
        if (CollectionUtils.isEmpty(events)) return true;
        Map params = new HashMap() {{
            put("eventids", new ArrayList() {{
                for (String id : events) {
                    add(id);
                }
            }});
            put("message", "忽略");
            put("action", 1);
        }};
        ZabbixResponse response = requestZabbixForObject("event.acknowledge", params);
        if (responseResultAvailable(response)) {
            try {
                Map result = (Map) response.getResult();
                List<String> eventsReturn = (List) result.get("eventids");
                if (!CollectionUtils.isEmpty(eventsReturn) && events.equals(eventsReturn)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("acknowledgeEvents失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("acknowledgeEvents失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("acknowledgeEvents失败!,返回为空!params={}", params);
            }
        }
        return false;

    }

    @Override
    public List<ZabbixHistory> getHistory(Map params) {

        params.put("sortfield", "clock");
        params.put("sortorder", "DESC");

        ZabbixResponse response = requestZabbixForObject("history.get", params);
        List<ZabbixHistory> zabbixHistoryList = new ArrayList<>();
        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                for (Map object : result) {
                    ObjectMap objectMap = new ObjectMap(object);
                    ZabbixHistory zabbixHistory = new ZabbixHistory();
                    zabbixHistory.setItemId(objectMap.getString("itemid"));
                    zabbixHistory.setClock(objectMap.getString("clock"));
                    zabbixHistory.setNanoseconds(objectMap.getString("ns"));
                    zabbixHistory.setValue(objectMap.getString("value"));
                    zabbixHistoryList.add(zabbixHistory);
                }

            } catch (Exception e) {
                logger.error("getHistory失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getHistory失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getHistory失败!,返回为空!params={}", params);
            }
        }
        return zabbixHistoryList;
    }

    @Override
    public List<GraphConfig> getGraphConfig(Map params) {
        List<GraphConfig> graphConfigList = new ArrayList<>();
        params.put("output", new ArrayList() {{
            add("graphid");
            add("name");
            add("graphtype");
        }});

        params.put("selectGraphItems", new ArrayList() {{
            add("gitemid");
            add("itemid");
            add("color");
        }});

        params.put("selectItems", new ArrayList() {{
            add("itemid");
            add("name");
        }});


        ZabbixResponse response = requestZabbixForObject("graph.get", params);

        if (responseResultAvailable(response)) {
            try {
                List<Map> result = (List) response.getResult();
                if (!ObjectUtils.isEmpty(result)) {
                    for (Map temp : result) {
                        ObjectMap objectMap = new ObjectMap(temp);
                        GraphConfig graphConfig = new GraphConfig();
                        graphConfig.setGraphId(objectMap.getString("graphid"));
                        graphConfig.setGraphName(objectMap.getString("name"));
                        graphConfig.setGraphType(objectMap.getInteger("graphtype"));

                        List<Map> gitemList = objectMap.getList("gitems");
                        if (!CollectionUtils.isEmpty(gitemList)) {
                            for (Map gitem : gitemList) {
                                ObjectMap gitemMap = new ObjectMap(gitem);
                                GraphItem graphItem = new GraphItem();
                                graphItem.setGraphItemId(gitemMap.getString("gitemid"));
                                graphItem.setItemId(gitemMap.getString("itemid"));
                                graphItem.setColor(gitemMap.getString("color"));
                                graphConfig.getItemList().add(graphItem);
                            }

                            List<Map> itemList = objectMap.getList("items");
                            if (!CollectionUtils.isEmpty(itemList)) {
                                Map<String, String> itemIdMapName = new HashMap<>();
                                for (Map item : itemList) {
                                    ObjectMap itemMap = new ObjectMap(item);
                                    itemIdMapName.put(itemMap.getString("itemid"), itemMap.getString("name"));
                                }

                                for (GraphItem gitem : graphConfig.getItemList()) {
                                    if (itemIdMapName.containsKey(gitem.getItemId())) {
                                        gitem.setItemName(itemIdMapName.get(gitem.getItemId()));
                                    }
                                }
                            }

                        }
                        graphConfigList.add(graphConfig);
                    }


                }

            } catch (Exception e) {
                logger.error("getGraphConfig失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("getGraphConfig失败!params={},error:[{}]", params, response.getError().toString());
            } else {
                logger.error("getGraphConfig失败!,返回为空!params={}", params);
            }
        }
        return graphConfigList;

    }

    @Override
    public GraphConfig createGraph(GraphConfig graphConfig) throws Exception {
        Map params = new HashMap() {{
            put("name", graphConfig.getGraphName());
            put("width", 900);
            put("height", 200);
            put("graphtype", graphConfig.getGraphType());
            put("gitems", new ArrayList() {{
                if (!CollectionUtils.isEmpty(graphConfig.getItemList())) {
                    for (GraphItem graphItem : graphConfig.getItemList()) {
                        add(new HashMap() {{
                            put("itemid", graphItem.getItemId());
                            put("color", graphItem.getColor());
                        }});
                    }
                }
            }});

        }};
        ZabbixResponse response = requestZabbixForObject("graph.create", params);
        if (responseResultAvailable(response)) {
            try {
                Map result = (Map) response.getResult();
                ObjectMap graphIdMap = new ObjectMap(result);
                List<String> graphIds = graphIdMap.getList("graphids");
                if (!CollectionUtils.isEmpty(graphIds)) {
                    graphConfig.setGraphId(graphIds.get(0));
                    return graphConfig;
                }

            } catch (Exception e) {
                logger.error("createGraph失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
                throw new Exception("createGraph失败!解析返回值异常!");
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("createGraph失败!params={},error:[{}]", params, response.getError().toString());
                throw new Exception(response.getError().toString());
            } else {
                logger.error("createGraph失败!,返回为空!params={}", params);
                throw new Exception("createGraph失败!,返回为空!");
            }
        }
        return graphConfig;

    }

    @Override
    public GraphConfig updateGraph(GraphConfig graphConfig) throws Exception {
        Map params = new HashMap() {{
            put("graphid", graphConfig.getGraphId());
            put("name", graphConfig.getGraphName());
            put("width", 900);
            put("height", 200);
            put("graphtype", graphConfig.getGraphType());
            put("gitems", new ArrayList() {{
                if (!CollectionUtils.isEmpty(graphConfig.getItemList())) {
                    for (GraphItem graphItem : graphConfig.getItemList()) {
                        add(new HashMap() {{
                            put("itemid", graphItem.getItemId());
                            put("color", graphItem.getColor());
                        }});
                    }
                }
            }});

        }};

        ZabbixResponse response = requestZabbixForObject("graph.update", params);
        if (responseResultAvailable(response)) {
            try {
                Map result = (Map) response.getResult();
                ObjectMap graphIdMap = new ObjectMap(result);
                List<String> graphIds = graphIdMap.getList("graphids");
                if (!CollectionUtils.isEmpty(graphIds) && graphIds.get(0).equals(graphConfig.getGraphId())) {
                    return graphConfig;
                }

            } catch (Exception e) {
                logger.error("updateGraph失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
                throw new Exception("updateGraph失败!解析返回值异常!");
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("updateGraph失败!params={},error:[{}]", params, response.getError().toString());
                throw new Exception(response.getError().toString());
            } else {
                logger.error("updateGraph失败!,返回为空!params={}", params);
                throw new Exception("updateGraph失败!,返回为空!");
            }
        }
        return graphConfig;

    }

    @Override
    public Boolean deleteGraph(List params) throws Exception {
        ZabbixResponse response = requestZabbixForObject("graph.delete", params);
        if (responseResultAvailable(response)) {
            try {
                Map result = (Map) response.getResult();
                ObjectMap graphIdMap = new ObjectMap(result);
                List<String> graphIds = graphIdMap.getList("graphids");
                if (!CollectionUtils.isEmpty(graphIds) && graphIds.equals(params)) return true;
            } catch (Exception e) {
                logger.error("deleteGraph失败!解析返回值异常!params={},result={}", params, response.getResult().toString());
                throw new Exception("deleteGraph失败!解析返回值异常!");
            }
        } else {
            if (responseErrorAvailable(response)) {
                logger.error("deleteGraph失败!params={},error:[{}]", params, response.getError().toString());
                throw new Exception(response.getError().toString());
            } else {
                logger.error("deleteGraph失败!,返回为空!params={}", params);
                throw new Exception("deleteGraph失败!,返回为空!");
            }
        }
        return false;
    }

    private Integer getIntegerBefore(String source, String flag) {
        return Integer.valueOf(source.substring(0, source.indexOf(flag)));
    }

    private Boolean responseResultAvailable(ZabbixResponse response) {
        if (response != null && response.getResult() != null) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean responseErrorAvailable(ZabbixResponse response) {
        if (response != null && response.getError() != null) {
            return true;
        } else {
            return false;
        }
    }
}
