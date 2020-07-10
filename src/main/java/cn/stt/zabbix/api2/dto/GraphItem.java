package cn.stt.zabbix.api2.dto;

/**
 * @author wangzhen3
 * @version 2018/2/9
 */
public class GraphItem {
    private String itemId;

    private String itemName;

    private String color;

    private String graphItemId;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getGraphItemId() {
        return graphItemId;
    }

    public void setGraphItemId(String graphItemId) {
        this.graphItemId = graphItemId;
    }
}
