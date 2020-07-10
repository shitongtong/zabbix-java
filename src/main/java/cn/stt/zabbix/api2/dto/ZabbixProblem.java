package cn.stt.zabbix.api2.dto;

import java.util.ArrayList;
import java.util.List;
/**
 * @author wangzhen3
 * @version 2017/12/01
 */
public class ZabbixProblem {

    private String eventId;

    /**
     * Type of the problem event.
     Possible values:
     0 - event created by a trigger;
     3 - internal event.
     */
    private Integer source;

    /**
     *Type of object that is related to the problem event.

     Possible values for trigger events:
     0 - trigger.

     Possible values for internal events:
     0 - trigger;
     4 - item;
     5 - LLD rule.
     */
    private Integer object;

    /**
     * ID of the related object.
     */
    private String objectId;

    /**
     * Time when the problem event was created.
     */
    private String clock;

    /**
     * Nanoseconds when the problem event was created.
     */
    private String ns;

    private String recoveryEventId;

    /**
     * Time when the recovery event was created.
     */
    private String recoveryEventClock;

    /**
     * Nanoseconds when the recovery event was created.
     */
    private String recoveryEventNs;

    /**
     * Correlation rule ID if this event was recovered by global correlation rule.
     */
    private String correlationId;

    /**
     * User ID if the problem was manually closed.
     */
    private String userId;

    /**
     * State of the related object.

     Possible values for trigger events:
     0 - OK;
     1 - problem.
     */
    private Integer value;

    private ZabbixTrigger relatedObject;//关联的触发器

    private List<ProblemAcknowledge> acknowledges = new ArrayList<>();

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getObject() {
        return object;
    }

    public void setObject(Integer object) {
        this.object = object;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getClock() {
        return clock;
    }

    public void setClock(String clock) {
        this.clock = clock;
    }

    public String getNs() {
        return ns;
    }

    public void setNs(String ns) {
        this.ns = ns;
    }

    public String getRecoveryEventId() {
        return recoveryEventId;
    }

    public void setRecoveryEventId(String recoveryEventId) {
        this.recoveryEventId = recoveryEventId;
    }

    public String getRecoveryEventClock() {
        return recoveryEventClock;
    }

    public void setRecoveryEventClock(String recoveryEventClock) {
        this.recoveryEventClock = recoveryEventClock;
    }

    public String getRecoveryEventNs() {
        return recoveryEventNs;
    }

    public void setRecoveryEventNs(String recoveryEventNs) {
        this.recoveryEventNs = recoveryEventNs;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<ProblemAcknowledge> getAcknowledges() {
        return acknowledges;
    }

    public void setAcknowledges(List<ProblemAcknowledge> acknowledges) {
        this.acknowledges = acknowledges;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public ZabbixTrigger getRelatedObject() {
        return relatedObject;
    }

    public void setRelatedObject(ZabbixTrigger relatedObject) {
        this.relatedObject = relatedObject;
    }
}
