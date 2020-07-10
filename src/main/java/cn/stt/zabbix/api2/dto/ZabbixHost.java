package cn.stt.zabbix.api2.dto;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixHost {
    private String hostId;

    private String hostIp;

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
}
