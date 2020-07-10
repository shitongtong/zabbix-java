package cn.stt.zabbix.api2.dto;

/**
 * @author wangzhen3
 * @version 2018/1/10
 */
public class ZabbixHistory {
    private String clock;

    private String itemId;

    private String nanoseconds;

    private String value;

    public String getClock() {
        return clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getNanoseconds() {
        return nanoseconds;
    }

    public void setNanoseconds(String nanoseconds) {
        this.nanoseconds = nanoseconds;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
