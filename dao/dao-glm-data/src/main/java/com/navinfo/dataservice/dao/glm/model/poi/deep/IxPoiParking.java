package com.navinfo.dataservice.dao.glm.model.poi.deep;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 索引:POI 深度信息(停车场类)  
 * @author zhaokk
 *
 */
public class IxPoiParking implements IObj {

	private Logger logger = Logger.getLogger(IxPoiParking.class);
	
	private int pid;
	private int poiPid =0;
	private String  parkingType;//停车场类型  
	private String  tollStd ;//收费标准
	private String   tollDes;//收费描述
	private String   tollWay;//收费方式
	private String  payment;//支付方式 
	private String  remark;//收费备注 
	private String  source;//信息获取源 
	private String openTiime;//开放时间 
	private int totalNum = 0 ;//车位数量 
	private String workTime;//制作时间
	private double resHigh = 0;//限高
	private double resWidth = 0;//限宽
	private double resWeigh = 0;//限重
	private long vehicle = 0;//停放车辆类型 
	private String photoName;//全景照片
	private int certificate = 0; //入口凭证
	private int uRecord=0;
	private String uDate;
	
	public String getParkingType() {
		return parkingType;
	}

	public void setParkingType(String parkingType) {
		this.parkingType = parkingType;
	}

	public String getTollStd() {
		return tollStd;
	}

	public void setTollStd(String tollStd) {
		this.tollStd = tollStd;
	}

	public String getTollDes() {
		return tollDes;
	}

	public void setTollDes(String tollDes) {
		this.tollDes = tollDes;
	}

	public String getTollWay() {
		return tollWay;
	}

	public void setTollWay(String tollWay) {
		this.tollWay = tollWay;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}



	public String getOpenTiime() {
		return openTiime;
	}

	public void setOpenTiime(String openTiime) {
		this.openTiime = openTiime;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public String getWorkTime() {
		return workTime;
	}

	public void setWorkTime(String workTime) {
		this.workTime = workTime;
	}

	public double getResHigh() {
		return resHigh;
	}

	public void setResHigh(double resHigh) {
		this.resHigh = resHigh;
	}

	public double getResWidth() {
		return resWidth;
	}

	public void setResWidth(double resWidth) {
		this.resWidth = resWidth;
	}

	public double getResWeigh() {
		return resWeigh;
	}

	public void setResWeigh(double resWeigh) {
		this.resWeigh = resWeigh;
	}

	public long getVehicle() {
		return vehicle;
	}

	public void setVehicle(long vehicle) {
		this.vehicle = vehicle;
	}

	public int getWomenNum() {
		return womenNum;
	}

	public void setWomenNum(int womenNum) {
		this.womenNum = womenNum;
	}

	public int getHandicapNum() {
		return handicapNum;
	}

	public void setHandicapNum(int handicapNum) {
		this.handicapNum = handicapNum;
	}

	public int getMiniNum() {
		return miniNum;
	}

	public void setMiniNum(int miniNum) {
		this.miniNum = miniNum;
	}

	public int getVipNum() {
		return vipNum;
	}

	public void setVipNum(int vipNum) {
		this.vipNum = vipNum;
	}

	public int getCertificate() {
		return certificate;
	}

	public void setCertificate(int certificate) {
		this.certificate = certificate;
	}

	public String getHaveSpecialplace() {
		return haveSpecialplace;
	}

	public void setHaveSpecialplace(String haveSpecialplace) {
		this.haveSpecialplace = haveSpecialplace;
	}

	private String haveSpecialplace ;//是否存在特殊类型停车位
	private int womenNum = 0;//女士停车位数量
	private int handicapNum = 0;//残障停车位数量
	private int miniNum = 0;//迷你停车位数量
	private int vipNum = 0;//专用停车位数量
	private String rowId;
	

	public int getPoiPid() {
		return poiPid;
	}

	public void setPoiPid(int poiPid) {
		this.poiPid = poiPid;
	}
	public String getRowId() {
		return rowId;
	}
 
    private Map<String, Object> changedFields = new HashMap<String, Object>();   
	@Override
	public String rowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
		
	}

	@Override
	public String tableName() {
		return "ix_poi_parking";
	}

	@Override
	public ObjStatus status() {
		return null;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}
	
	@Override
	public void setStatus(ObjStatus os) {
	}

	@Override
	public ObjType objType() {
		return ObjType.IXPOIPARKING;
	}

	@Override
	public void copy(IRow row) {
		
	}

	@Override
	public Map<String, Object> changedFields() {
		return this.changedFields;
	}

	@Override
	public String parentPKName() {
		return "POI_PID";
	}

	@Override
	public int parentPKValue() {
		return this.getPoiPid();
	}

	@Override
	public String parentTableName() {
		return "ix_poi";
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}



	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public int getuRecord() {
		return uRecord;
	}

	public void setuRecord(int uRecord) {
		this.uRecord = uRecord;
	}

	public String getuDate() {
		return uDate;
	}

	public void setuDate(String uDate) {
		this.uDate = uDate;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (json.get(key) instanceof JSONArray) {
				continue;
			} else {
				if ( !"objStatus".equals(key)) {
					
					Field field = this.getClass().getDeclaredField(key);
					
					field.setAccessible(true);
					
					Object objValue = field.get(this);
					String newValue = json.getString(key);
					if("null".equalsIgnoreCase(newValue))newValue=null;
					logger.info("objValue:"+objValue);
					logger.info("newValue:"+newValue);
					if (!isEqualsString(objValue,newValue)) {
						logger.info("isEqualsString:false");
						Object value = json.get(key);
						
						if(value instanceof String){
							changedFields.put(key, newValue.replace("'", "''"));
						}
						else{
							changedFields.put(key, value);
						}

					}

					
				}
			}
		}
		
		if (changedFields.size() >0){
			return true;
		}else{
			return false;
		}

	}
	
	private static boolean isEqualsString(Object oldValue,Object newValue){
		if(null==oldValue&&null==newValue)
			return true;
		if(StringUtils.isEmpty(oldValue)&&StringUtils.isEmpty(newValue)){
			return true;
		}
		if(oldValue==null&&newValue!=null){
			return false;
		}
		if(oldValue!=null&&newValue==null){
			return false;
		}
		return oldValue.toString().equals(newValue.toString());
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		return JSONObject.fromObject(this, JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
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
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
		return this.getPid();
	}

	@Override
	public String primaryKey() {
		return "parking_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}


}
