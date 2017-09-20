package com.navinfo.dataservice.engine.limit.glm.model.limit;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.engine.limit.glm.iface.IObj;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

/**
 * Created by ly on 2017/9/18.
 */
public class ScPlateresInfo implements IObj{
	
	//情报主键
	private String infoIntelId;
	
	public String getInfoIntelId(){
		return this.infoIntelId;
	}
	
	public void setInfoIntelId(String value){
		this.infoIntelId = value;
	}
	
	//情报编码
	private String infoCode;
	
	public String getInfoCode(){
		return this.infoCode;
	}
	
	public void setInfoCode(String value){
		this.infoCode = value;
	}
	
	//行政区划代码
	private String adminCode;
	
	public String getAdminCode(){
		return this.adminCode;
	}
	
	public void setAdminCode(String value){
		this.adminCode = value;
	}
	
	//来源网址
	private String url;
	
	public String getUrl(){
		return this.url;
	}
	
	public void setUrl(String value){
		this.url = url;
	}
	
	//新闻发布日期
	private String newsTime;
	
	public String getNewsTime(){
		return this.newsTime;
	}
	
	public void setNewsTime(String value){
		this.newsTime = value;
	}
	
	//新闻内容
	private String infoContent;
	
	public String getInfoContent(){
		return this.infoContent;
	}
	
	public void setInfoContent(String value){
		this.infoContent = value;
	}
	
	//完成状态
	private int complete = 1;
	
	public int getComplete(){
		return this.complete;
	}
	
	public void setComplete(int value){
		this.complete = value;
	}
	
	//限行长短期说明
	private String condition;
	
	public String getCondition(){
		return this.condition;
	}
	
	public void setCondition(String value){
		this.condition = value;
	}
	
	//备注
	private String memo;
	
	public String getMemo(){
		return this.memo;
	}
	
	public void setMemo(String value){
		this.memo = value;
	}
	
	@Override
	public JSONObject Serialize(ObjLevel objLevel) {
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

	@Override
	public String tableName() {
		return "SC_PLATERES_INFO";
	}

	protected ObjStatus status;
	
	@Override
	public ObjStatus status() {
		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status = os;
	}

	@Override
	public ObjType objType() {
		return ObjType.SCPLATERESINFO;
	}

	private Map<String, Object> changedFields = new HashMap<>();
	 
	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "link_pid";
	}

	@Override
	public int parentPKValue() {
		return 0;
	}
	
    @Override
    public String primaryKeyValue() {
        return this.infoIntelId;
    }

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {
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

					String oldValue = null;

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

		if (changedFields.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String primaryKey() {
		return "INFO_INTEL_ID";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void loadResultSet(ResultSet rs) throws SQLException{
		
		this.setInfoIntelId(rs.getString("INFO_INTEL_ID"));
		
		this.setInfoCode(rs.getString("INFO_CODE"));
		
		this.setAdminCode(rs.getString("ADMIN_CODE"));
		
		this.setUrl(rs.getString("URL"));
		
		this.setNewsTime(rs.getString("NEWS_TIME"));
		
		this.setInfoContent(rs.getString("INFO_CONTENT"));
		
		this.setComplete(rs.getInt("COMPLETE"));
		
		this.setCondition(rs.getString("CONDITION"));
		
		this.setMemo(rs.getString("MEMO"));
	}
}
