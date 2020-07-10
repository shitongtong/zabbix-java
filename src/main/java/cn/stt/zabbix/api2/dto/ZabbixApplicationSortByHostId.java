package cn.stt.zabbix.api2.dto;

import java.text.Collator;
import java.util.Comparator;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixApplicationSortByHostId implements Comparator<ZabbixApplication>{

    @Override
    public int compare(ZabbixApplication o1, ZabbixApplication o2){
        return Collator.getInstance().compare(o1.getHostId(),o2.getHostId());
    }

}
