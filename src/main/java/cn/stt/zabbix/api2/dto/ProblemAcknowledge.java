package cn.stt.zabbix.api2.dto;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ProblemAcknowledge {

    private String id;

    private String userId;

    private String eventId;

    private String clock;

    private String message;

    private Integer action;// 1 关闭问题 ； 2 保持问题开启

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getClock() {
        return clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }
}
