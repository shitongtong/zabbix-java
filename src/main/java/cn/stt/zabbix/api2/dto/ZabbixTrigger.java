package cn.stt.zabbix.api2.dto;

import java.util.ArrayList;
import java.util.List;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixTrigger {
    private String triggerId; //触发器ID 唯一

    private String description;//触发器名称

    private Integer priority;//触发器的严重等级 0 - (default) not classified; 1 - information; 2 - warning; 3 - average; 4 - high; 5 - disaster.

    private String expression;//触发器表达式

    private Integer status; //0启用  1禁用

    private String comments;//自定义信息（对应模板导入时的trigger-description字段） 以$S 为 分隔符，依次为 expression  description-name  description-value

    private Integer value;//触发器状态 0 正常， 1异常

    private String templateId;// 若为"0" ，表示该触发器为自定义的触发器，而不是通过关联监控模板生成的

    private List<TriggerTags> tags = new ArrayList<>();//自定义标签

    private String hostIp;//所属机器 ip

    private String hostId;//所属机器 id

    private String itemId;//所属监控项 id

    private String itemLastValue;//监控项数据

    private String itemUnits;//监控项数据 单位

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<TriggerTags> getTags() {
        return tags;
    }

    public void setTags(List<TriggerTags> tags) {
        this.tags = tags;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemLastValue() {
        return itemLastValue;
    }

    public void setItemLastValue(String itemLastValue) {
        this.itemLastValue = itemLastValue;
    }

    public String getItemUnits() {
        return itemUnits;
    }

    public void setItemUnits(String itemUnits) {
        this.itemUnits = itemUnits;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZabbixTrigger{");
        sb.append("triggerId='").append(triggerId).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", priority=").append(priority);
        sb.append(", expression='").append(expression).append('\'');
        sb.append(", status=").append(status);
        sb.append(", comments='").append(comments).append('\'');
        sb.append(", value=").append(value);
        sb.append(", templateId='").append(templateId).append('\'');
        sb.append(", tags=").append(tags);
        sb.append(", hostIp='").append(hostIp).append('\'');
        sb.append(", hostId='").append(hostId).append('\'');
        sb.append(", itemId='").append(itemId).append('\'');
        sb.append(", itemLastValue='").append(itemLastValue).append('\'');
        sb.append(", itemUnits='").append(itemUnits).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
