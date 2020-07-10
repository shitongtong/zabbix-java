package cn.stt.zabbix.api2.dto;

import lombok.Data;

/**
 * @Description TODO
 * @Author shitt7
 * @Date 2020/3/10 14:45
 */
@Data
public class PandoraConfig {
    private String zabbixIp = "localhost";
    private String zabbixUser = "user";
    private String zabbixPassword = "password";
}
