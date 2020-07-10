package cn.stt.zabbix.api2.service;

import cn.stt.zabbix.api2.dto.GraphConfig;
import cn.stt.zabbix.api2.dto.ZabbixApplication;
import cn.stt.zabbix.api2.dto.ZabbixHistory;
import cn.stt.zabbix.api2.dto.ZabbixHost;
import cn.stt.zabbix.api2.dto.ZabbixItem;
import cn.stt.zabbix.api2.dto.ZabbixProblem;
import cn.stt.zabbix.api2.dto.ZabbixResponse;
import cn.stt.zabbix.api2.dto.ZabbixTrigger;
import org.dom4j.Document;

import java.util.List;
import java.util.Map;

/**
 * @author wangzhen3
 * @version 2017/12/01
 *          ctrl + 单击  打开链接 推荐chrome 浏览器
 * @see <a href="https://www.zabbix.com/documentation/3.4/manual/api">zabbixDoc</a>
 */
public interface ZabbixService {
    /**
     * @return 登陆zabbix后台 true/false
     */
    Boolean login();

    /**
     * 判断zabbix是否已 在线（可用，并已登陆），没有的话 尝试登陆
     *
     * @return 在线/重连成功，返回1；自系统启动首次登陆成功返回2； 不在线，且重连失败，返回0
     */
    Integer OnLine();

    /**
     * 封装了 RestTemplate 请求 与 zabbix 自动重连功能
     *
     * @param zabbixMethod 请求方法
     * @param zabbixParams 请求参数
     * @return result Object
     */
    ZabbixResponse requestZabbixForObject(String zabbixMethod, Object zabbixParams);

    /**
     * 导入xml模板
     *
     * @param sourceXml
     * @return
     */
    Boolean importConfiguration(Document sourceXml);

    /**
     * 根据模板host名称(全英文的那个名字 在zabbix后台唯一标识模板) 查询 模板在zabbix后台的id
     *
     * @param tempLateHostName
     * @return
     */
    String getTemplateIdByHostName(String tempLateHostName);

    /**
     * 根据group名称，查询group在zabbix后台的id
     *
     * @param groupName
     * @return
     */
    String getGroupIdByName(String groupName);

    /**
     * 创建主机
     *
     * @param hostIps 主机ip列表
     * @param groupId 所属分组id(zabbix)
     * @return 按hostIps 的顺序 返回 对应的hostId
     */
    List<String> createHost(List<String> hostIps, String groupId);

    /**
     * 查询全部主机
     *
     * @return key 主机host(Ip), value 主机hostId
     */
    Map<String, String> getHostAll();

    /**
     * 批量将机器hostIds（zabbix后台的hostId）（列表） 与 模板关联
     *
     * @param templateId
     * @param hostIds
     * @return
     */
    Boolean templateMassAdd(String templateId, List<String> hostIds);


    /**
     * 主机(批量) 取消 模板链接 并清理-（会删掉主机监控项）
     *
     * @param hostIds    需要取消模板关联的主机id列表
     * @param templateId 需要取消关联的 模板id
     * @return
     */
    Boolean hostMassRemoveTemplate(List<String> hostIds, String templateId);


    /**
     * 查询 模板关联的所有机器
     *
     * @param params 默认参数:
     *               {
     *               "output": ["hostid","host"]
     *               }
     * @return
     */
    List<ZabbixHost> getHosts(Map params);

    /**
     * 批量删除模板，会清理掉模板当前所关联主机的所有监控项（已与模板解除关联的主机，其监控项不会被清理)
     *
     * @param templateIds
     * @return
     */
    Boolean templateMassDelete(List<String> templateIds);

    /**
     * 批量删除主机
     *
     * @param hostIds 主机列表
     * @return
     */
    Boolean hostDelete(List<String> hostIds);

    /**
     * 查询主机关联的所有的监控项
     *
     * @param params {@link <a href = "https://www.zabbix.com/documentation/3.4/manual/api/reference/item/get">item.get</a>}
     *               选填参数：
     *               {
     *               "hostids":["10205"],
     *               "itemids":["27692","27707"],
     *               "triggerids":["12345"]
     *               }
     *               默认参数：
     *               {
     *               "output":["itemid","name","key_","delay","history","status","flags","units","lastvalue","value_type"],
     *               "selectApplications": ["applicationid","name","hostid","templateids"],
     *               "selectTriggers":["triggerid","templateid","description","expression","comments","status","priority","value"],
     *               "selectHosts":["hostid","host"]
     *               }
     * @return Item列表 或 空Array
     */
    List<ZabbixItem> getItems(Map params);

    /**
     * 查询监控项数据，和监控项关联的host，无用的字段不查
     *
     * @param params 默认参数
     *               {
     *               "output":["itemid","name","lastclock","lastvalue","lastvalue","prevvalue","status","units"],
     *               "selectApplications": ["applicationid","name","hostid","templateids"],
     *               "selectHosts":["hostid","host"]
     *               }
     * @return Item列表key-zabbixItemId 或 空Map
     */
    Map<String, ZabbixItem> getItemData(Map params);

    /**
     * 根据application Id 查询 application 详情
     *
     * @param Id
     * @return application 或 null
     */
    ZabbixApplication getApplicationById(String Id);

    /**
     * 批量查询，根据application Ids 查询 applications 详情
     *
     * @param ids
     * @return key 为applicationId
     */
    Map<String, ZabbixApplication> getApplicationsByIds(List<String> ids);


    /**
     * 查询template 关联的所有application
     *
     * @param templateIds
     * @return
     */
    List<String> getApplicationNamesByTemplateIds(List<String> templateIds);


    /**
     * 获取所有的触发器
     * doc:
     *
     * @param params 默认参数:
     *               {
     *               "output":["triggerid","description","priority","value","expression","status","comments"],
     *               "selectItems":["itemid","lastvalue","units"],
     *               "selectHosts":["host","hostid"]
     *               }
     * @return map  key = {@link ZabbixTrigger#triggerId} , value = {@link ZabbixTrigger}; 或空Map
     * @see <a href = "https://www.zabbix.com/documentation/3.4/manual/api/reference/trigger/get">trigger.get</a>
     */
    Map<String, ZabbixTrigger> getTriggers(Map params);

    /**
     * 根据itemId 修改 item
     *
     * @param items 字段itemId 必填，只支持修改delay,history,status
     * @return
     */
    Boolean itemsUpdate(List<ZabbixItem> items);

    /**
     * 根据triggerId 修改 trigger
     *
     * @param triggers 字段triggerId 必填，只支持修改 status,comments,expression,
     *                 注意:通过模板关联生成的trigger 只能修改status,comments,其他是改不了的，会失败
     * @return true/false
     */
    Boolean triggersUpdate(List<ZabbixTrigger> triggers);

    /**
     * 根据hostIds 查询关联的application-Item 组织（组织结点 主要包含Id 和 name）
     *
     * @param hostIds
     * @return
     */
    List<ZabbixApplication> getApplicationAndItemOrgByHostId(List<String> hostIds);


    /**
     * 查询 监控数据，以主机ID 和 applicationName 作为过滤条件
     * 会过滤掉 已被禁用的监控项数据
     *
     * @param hostIds
     * @param appNames 为空 时，查询全部数据
     * @return application-item
     */
    List<ZabbixApplication> getLatestItemData(List<String> hostIds, List<String> appNames);

    /**
     * 查询报警数据 --来自触发器
     *
     * @param params 查询参数
     *               默认参数：
     *               {
     *               "output": "extend",
     *               "sortfield": ["eventid"],
     *               "sortorder": "DESC",
     *               "recent":"true",
     *               "selectAcknowledges": "extend"
     *               }
     * @return {@link ZabbixProblem}
     * @see <a href="https://www.zabbix.com/documentation/3.4/manual/api/reference/problem/get">problem.get</a>
     * ctrl + 单击 查看url链接
     */
    List<ZabbixProblem> getProblems(Map params);

    /**
     * 查询报警事件 --来自触发器
     *
     * @param params 默认参数：
     *               {  "output": "extend",
     *               "sortfield": ["clock", "eventid"],
     *               "sortorder": "DESC",
     *               "select_acknowledges":"extend",
     *               "selectRelatedObject":["triggerid","description","priority","value"],
     *               "selectHosts":["hostid","host"]
     *               }
     * @return {@link ZabbixProblem}
     * @see <a href="https://www.zabbix.com/documentation/3.4/manual/api/reference/event/get">event.get</a>
     */
    List<ZabbixProblem> getEvents(Map params);

    /**
     * 关闭触发器
     *
     * @param events 需关闭的event 列表
     *               默认参数：
     *               {
     *               "message": "忽略",
     *               "action": 1
     *               }
     * @return
     */
    Boolean acknowledgeEvents(List<String> events);

    /**
     * 查监控项的历史数据
     *
     * @param params itemids  string/array 监控项id 必填  "history":3, 必填-不填或者类型不对将查不到数据
     * @return
     * @see <a href="https://www.zabbix.com/documentation/3.4/manual/api/reference/history/get">history.get</a>
     * 必填参数:
     * {
     * "itemids": ["27702"]
     * }
     * 默认参数：
     * {
     * "sortfield":"clock",
     * "sortorder":"DESC"
     * }
     */
    List<ZabbixHistory> getHistory(Map params);


    /**
     * 查询图表配置信息
     *
     * @param params 默认参数:
     *               params.put("output",new ArrayList(){{
     *               add("graphid");
     *               add("name");
     *               add("graphtype");
     *               }});
     *               <p>
     *               params.put("selectGraphItems",new ArrayList(){{
     *               add("gitemid");
     *               add("itemid");
     *               add("color");
     *               }});
     *               <p>
     *               params.put("selectItems",new ArrayList(){{
     *               add("itemid");
     *               add("name");
     *               }});
     * @return
     */
    List<GraphConfig> getGraphConfig(Map params);

    /**
     * 创建监控图表
     *
     * @param graphConfig
     * @return
     */
    GraphConfig createGraph(GraphConfig graphConfig) throws Exception;

    /**
     * 更新监控图表
     *
     * @param graphConfig
     * @return
     */
    GraphConfig updateGraph(GraphConfig graphConfig) throws Exception;

    /**
     * 删除图表
     *
     * @param params 图表id
     * @return
     * @throws Exception
     */
    Boolean deleteGraph(List params) throws Exception;

}
