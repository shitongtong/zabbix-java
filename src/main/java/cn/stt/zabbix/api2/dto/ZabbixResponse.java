package cn.stt.zabbix.api2.dto;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixResponse {

    private String jsonrpc;

    private Long id;

    private Object result;

    private ZabbixError error;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ZabbixError getError() {
        return error;
    }

    public void setError(ZabbixError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZabbixResponse{");
        sb.append("jsonrpc='").append(jsonrpc).append('\'');
        sb.append(", id=").append(id);
        sb.append(", result=").append(result);
        sb.append(", error=").append(error);
        sb.append('}');
        return sb.toString();
    }
}
