
package com.navinfo.dataservice.engine.limit.glm.model.limit;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IObj;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScPlateresInfo implements IObj {

    private String infoIntelId = ""; //INFO_INTEL_ID	情报主键
    private String infoCode;//    INFO_CODE	情报编号
    private String adminCode;//    ADMIN_CODE	行政区划代码
    private String url;//    URL	来源网址
    private String newsTime;//    NEWS_TIME	新闻发布日期
    private String infoContent;//    INFO_CONTENT	新闻内容
    private int complete = 1;//    COMPLETE	完成状态
    private String condition;//    CONDITION	限行长短期说明
    private String memo;//    MEMO	备注

    public String getInfoIntelId() {
        return infoIntelId;
    }

    public void setInfoIntelId(String infoIntelId) {
        this.infoIntelId = infoIntelId;
    }

    public String getInfoCode() {
        return infoCode;
    }

    public void setInfoCode(String infoCode) {
        this.infoCode = infoCode;
    }

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNewsTime() {
        return newsTime;
    }

    public void setNewsTime(String newsTime) {
        this.newsTime = newsTime;
    }

    public String getInfoContent() {
        return infoContent;
    }

    public void setInfoContent(String infoContent) {
        this.infoContent = infoContent;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    protected ObjStatus status;

    private Map<String, Object> changedFields = new HashMap<>();

    @Override
    public List<IRow> relatedRows() {
        return null;
    }

    @Override
    public String primaryKeyValue() {
        return infoIntelId;
    }

    @Override
    public String primaryKey() {
        return "INFO_INTEL_ID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        return null;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        return null;
    }

    @Override
    public String tableName() {
        return "SC_PLATERES_INFO";
    }

    @Override
    public ObjStatus status() {
        return status;
    }

    @Override
    public void setStatus(ObjStatus os) {

        status = os;
    }

    @Override
    public LimitObjType objType() {
        return LimitObjType.SCPLATERESINFO;
    }

    @Override
    public Map<String, Object> changedFields() {
        return changedFields;
    }

    @Override
    public String parentPKName() {
        return "INFO_INTEL_ID";
    }

    @Override
    public int parentPKValue() {
        return 0;
    }

    @Override
    public String parentTableName() {
        return "SC_PLATERES_INFO";
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {
        Iterator keys = json.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if (json.get(key) instanceof JSONArray) {
                continue;
            } else {
                if (!"objStatus".equals(key)) {

                    Field field = this.getClass().getDeclaredField(key);

                    field.setAccessible(true);

                    Object objValue = field.get(this);

                    String oldValue;

                    if (objValue == null) {
                        oldValue = "null";
                    } else {
                        oldValue = String.valueOf(objValue);
                    }

                    String newValue = json.getString(key);

                    if (!newValue.equals(oldValue)) {
                        Object value = json.get(key);

                        if (value instanceof String) {
                            changedFields.put(key, newValue.replace("'", "''"));
                        } else {
                            changedFields.put(key, value);
                        }

                    }
                }
            }
        }

        return changedFields.size() > 0;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
        if (objLevel == ObjLevel.HISTORY) {
            json.remove("status");
        }
        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        Iterator keys = json.keys();

        while (keys.hasNext()) {

            String key = (String) keys.next();

            if (!"objStatus".equals(key)) {

                Field f = this.getClass().getDeclaredField(key);

                f.setAccessible(true);

                f.set(this, json.get(key));
            }

        }
        return true;
    }
}
