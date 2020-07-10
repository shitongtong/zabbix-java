package cn.stt.zabbix.api2.dto;

import java.util.ArrayList;
import java.util.List;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixItem {
    private String itemId;

    private String name;

    private String hostId;

    private String hostIp;

    private String key;

    private Integer delay; //单位 秒 s

    private Integer history;// 单位 天 d

    private Integer status;// 0启用 1禁用

    private ZabbixApplication application;

    private Integer flags;//0 - a plain item; 4 - a discovered item.

    private String units;//监控数据 度量单位

    private String lastClock;//上次数据更新时间  2017-12-18 14:23:04

    private String lastValue;//最近一条数据值

    private String preValue;//之前一条数据值

    private Integer valueType;//Possible values: 0 - numeric float; 1 - character; 2 - log; 3 - numeric unsigned; 4 - text.

    private List<ZabbixTrigger> triggers = new ArrayList<>();

    public ZabbixItem(){

    }
    //for test
    public ZabbixItem(String name,String lastValue,String applicationName){
        this.name = name;
        this.lastValue = lastValue;
        ZabbixApplication application = new ZabbixApplication();
        application.setName(applicationName);
        this.application = application;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getHistory() {
        return history;
    }

    public void setHistory(Integer history) {
        this.history = history;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ZabbixApplication getApplication() {
        return application;
    }

    public void setApplication(ZabbixApplication application) {
        this.application = application;
    }

    public List<ZabbixTrigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<ZabbixTrigger> triggers) {
        this.triggers = triggers;
    }

    public Integer getFlags() {
        return flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getLastClock() {
        return lastClock;
    }

    public void setLastClock(String lastClock) {
        this.lastClock = lastClock;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String getPreValue() {
        return preValue;
    }

    public void setPreValue(String preValue) {
        this.preValue = preValue;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public Integer getValueType() {
        return valueType;
    }
    public void setValueType(Integer valueType) {
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZabbixItem{");
        sb.append("itemId='").append(itemId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", hostId='").append(hostId).append('\'');
        sb.append(", hostIp='").append(hostIp).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append(", delay=").append(delay);
        sb.append(", history=").append(history);
        sb.append(", status=").append(status);
        sb.append(", application=").append(application);
        sb.append(", flags=").append(flags);
        sb.append(", units='").append(units).append('\'');
        sb.append(", lastClock='").append(lastClock).append('\'');
        sb.append(", lastValue='").append(lastValue).append('\'');
        sb.append(", preValue='").append(preValue).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append(", triggers=").append(triggers);
        sb.append('}');
        return sb.toString();
    }


}
