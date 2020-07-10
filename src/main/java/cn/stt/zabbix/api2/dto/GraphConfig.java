package cn.stt.zabbix.api2.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangzhen3
 * @version 2018/2/9
 */
public class GraphConfig {
    private String graphId;

    private String graphName;

    private Integer graphType;

    private Integer categoryId;

    private List<GraphItem> itemList = new ArrayList<>();

    public String getGraphId() {
        return graphId;
    }

    public void setGraphId(String graphId) {
        this.graphId = graphId;
    }

    public String getGraphName() {
        return graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public Integer getGraphType() {
        return graphType;
    }

    public void setGraphType(Integer graphType) {
        this.graphType = graphType;
    }

    public List<GraphItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<GraphItem> itemList) {
        this.itemList = itemList;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
}
