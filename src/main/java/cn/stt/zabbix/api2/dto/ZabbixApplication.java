package cn.stt.zabbix.api2.dto;

import java.util.ArrayList;
import java.util.List;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixApplication {
    private String applicationId;

    private String hostId;

    private String name;

    private String parentId;//parentId 为null 时，hostId指向Template

    private List<ZabbixItem> items = new ArrayList<>();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public List<ZabbixItem> getItems() {
        return items;
    }

    public void setItems(List<ZabbixItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZabbixApplication{");
        sb.append("applicationId='").append(applicationId).append('\'');
        sb.append(", hostId='").append(hostId).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", parentId='").append(parentId).append('\'');
        sb.append(", items=").append(items);
        sb.append('}');
        return sb.toString();
    }
}
