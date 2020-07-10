package cn.stt.zabbix.api2.dto;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class TriggerTags {
    private String tag;

    private String value;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TriggerTags{");
        sb.append("tag='").append(tag).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
