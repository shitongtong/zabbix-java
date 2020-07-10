package cn.stt.zabbix.api2.dto;

import cn.stt.zabbix.api2.service.imp.ZabbixServiceImp;

/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixRequestParam {

    private String jsonrpc = "2.0";

    private String method;

    private Object params;

    private String auth = null;

    private Long id;

    //结构化时，自动装入token 和 id
    public ZabbixRequestParam(){
        this.id = ZabbixServiceImp.requestId.getAndIncrement();
        this.auth = ZabbixServiceImp.authenticationToken;
    }

    //结构化时，自动装入token 和 id
    public ZabbixRequestParam(String method,Object params){
        this.method = method;
        this.params = params;
        this.id = ZabbixServiceImp.requestId.getAndIncrement();
        this.auth = ZabbixServiceImp.authenticationToken;
    }

    public void reloadAuthentication(){
        this.id = ZabbixServiceImp.requestId.getAndIncrement();
        this.auth = ZabbixServiceImp.authenticationToken;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
