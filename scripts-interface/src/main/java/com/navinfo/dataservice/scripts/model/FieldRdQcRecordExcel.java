package com.navinfo.dataservice.scripts.model;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.nirobot.common.utils.DateUtils;

import net.sf.json.JSONObject;

public class FieldRdQcRecordExcel implements IObj{

	private int num;
	private String area;//区域
	private String fieldGroup;// 队别,子任务所属基地名
	private String linkPid;//LinkPid
	private String province;// 省,子任务所属省
	private String city;// 市,子任务所属市
	private String qcSubtaskName;// 项目，导出时导出子任务名称,质检子任务ID
	private String routeNum;// 线路号,不维护
	private String estabLevel;// 设施类别,POI.level
	private String problemNum;// 问题编号,uuid（不带下划线_的uuid）
	private String photoNum;// 照片编号,不维护
	private String meshId;// 图幅号,Poi.meshid
	private String groupName;// 组名,不维护
	private String poiFid;// 设施ID,Poi.POI_NUM，对应导出报表fid
	private String kindCode;// 分类代码,Poi.kindcode
	private String classTop;// 大分类,前台输入
	private String classMedium;// 中分类,前台输入
	private String classBottom;// 小分类,前台输入
	private String problemType;// 问题类型,前台输入
	private String problemPhenomenon;// 问题现象,前台输入
	private String problemDescription;// 问题描述,前台输入
	private String initialCause;// 初步原因,前台输入
	private String rootCause;// 根本原因（RCA）,前台输入
	private String checkUserid;// 质检员,质检员ID
	private String checkTime;// 质检日期,当前记录问题时间
	private String collectorUserid;// 作业员
	private String collectorTime;// 采集日期,前台输入
	private String checkDepartment;// 质检部门,服务赋值默认外业采集部(默认:外业采集部)
	private String checkMode;// 质检方式,前台输入
	private String modifyDate;// 更改日期,同质检日期
	private String modifyUserid;// 更改人,同质检人
	private String confirmUserid;// 确认人,前台输入
	private String version;// 版本号,前台输入（前端提供前后四年的版本供选择）
	private String problemLevel;// 问题等级,前台输入
	private String photoExist;// 是否有照片,不维护
	private String kind;//种别
	private String fc;//功能等级
	private String memoUser;// 备注作业员,当采集员是”AAA“时,该字段读取采集子任务作业员userid
	private String classWeight;// 类别权重,不维护
	private String problemWeight;// 问题重要度权重,不维护
	private String totalWeight;// 总权重,不维护
	private String wordYear;// 工作年限,不维护

	public FieldRdQcRecordExcel() {
	}

	public FieldRdQcRecordExcel(int num, String area, String fieldGroup, String linkPid, String province, String city,
			String qcSubtaskName, String routeNum, String estabLevel, String problemNum, String photoNum, String meshId,
			String groupName, String poiFid, String kindCode, String classTop, String classMedium, String classBottom,
			String problemType, String problemPhenomenon, String problemDescription, String initialCause,
			String rootCause, String checkUserid, String checkTime, String collectorUserid, String collectorTime,
			String checkDepartment, String checkMode, String modifyDate, String modifyUserid, String confirmUserid,
			String version, String problemLevel, String photoExist, String kind, String fc, String memoUser,
			String classWeight, String problemWeight, String totalWeight, String wordYear) {
		super();
		this.num = num;
		this.area = area;
		this.fieldGroup = fieldGroup;
		this.linkPid = linkPid;
		this.province = province;
		this.city = city;
		this.qcSubtaskName = qcSubtaskName;
		this.routeNum = routeNum;
		this.estabLevel = estabLevel;
		this.problemNum = problemNum;
		this.photoNum = photoNum;
		this.meshId = meshId;
		this.groupName = groupName;
		this.poiFid = poiFid;
		this.kindCode = kindCode;
		this.classTop = classTop;
		this.classMedium = classMedium;
		this.classBottom = classBottom;
		this.problemType = problemType;
		this.problemPhenomenon = problemPhenomenon;
		this.problemDescription = problemDescription;
		this.initialCause = initialCause;
		this.rootCause = rootCause;
		this.checkUserid = checkUserid;
		this.checkTime = checkTime;
		this.collectorUserid = collectorUserid;
		this.collectorTime = collectorTime;
		this.checkDepartment = checkDepartment;
		this.checkMode = checkMode;
		this.modifyDate = modifyDate;
		this.modifyUserid = modifyUserid;
		this.confirmUserid = confirmUserid;
		this.version = version;
		this.problemLevel = problemLevel;
		this.photoExist = photoExist;
		this.kind = kind;
		this.fc = fc;
		this.memoUser = memoUser;
		this.classWeight = classWeight;
		this.problemWeight = problemWeight;
		this.totalWeight = totalWeight;
		this.wordYear = wordYear;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getFieldGroup() {
		return fieldGroup;
	}

	public void setFieldGroup(String fieldGroup) {
		if(fieldGroup == null || fieldGroup.isEmpty()){
			fieldGroup = "空";
		}
		this.fieldGroup = fieldGroup;
	}

	public String getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(String linkPid) {
		this.linkPid = linkPid;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getQcSubtaskName() {
		return qcSubtaskName;
	}

	public void setQcSubtaskName(String qcSubtaskName) {
		this.qcSubtaskName = qcSubtaskName;
	}

	public String getRouteNum() {
		return routeNum;
	}

	public void setRouteNum(String routeNum) {
		this.routeNum = routeNum;
	}

	public String getEstabLevel() {
		return estabLevel;
	}

	public void setEstabLevel(String estabLevel) {
		this.estabLevel = estabLevel;
	}

	public String getProblemNum() {
		return problemNum;
	}

	public void setProblemNum(String problemNum) {
		this.problemNum = problemNum;
	}

	public String getPhotoNum() {
		return photoNum;
	}

	public void setPhotoNum(String photoNum) {
		this.photoNum = photoNum;
	}

	public String getMeshId() {
		return meshId;
	}

	public void setMeshId(String meshId) {
		this.meshId = meshId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getPoiFid() {
		return poiFid;
	}

	public void setPoiFid(String poiFid) {
		this.poiFid = poiFid;
	}

	public String getKindCode() {
		return kindCode;
	}

	public void setKindCode(String kindCode) {
		this.kindCode = kindCode;
	}

	public String getClassTop() {
		return classTop;
	}

	public void setClassTop(String classTop) {
		this.classTop = classTop;
	}

	public String getClassMedium() {
		return classMedium;
	}

	public void setClassMedium(String classMedium) {
		this.classMedium = classMedium;
	}

	public String getClassBottom() {
		return classBottom;
	}

	public void setClassBottom(String classBottom) {
		this.classBottom = classBottom;
	}

	public String getProblemType() {
		return problemType;
	}

	public void setProblemType(String problemType) {
		this.problemType = problemType;
	}

	public String getProblemPhenomenon() {
		return problemPhenomenon;
	}

	public void setProblemPhenomenon(String problemPhenomenon) {
		this.problemPhenomenon = problemPhenomenon;
	}

	public String getProblemDescription() {
		return problemDescription;
	}

	public void setProblemDescription(String problemDescription) {
		this.problemDescription = problemDescription;
	}

	public String getInitialCause() {
		return initialCause;
	}

	public void setInitialCause(String initialCause) {
		this.initialCause = initialCause;
	}

	public String getRootCause() {
		return rootCause;
	}

	public void setRootCause(String rootCause) {
		this.rootCause = rootCause;
	}

	public String getCheckUserid() {
		return checkUserid;
	}

	public void setCheckUserid(String checkUserid) {
		this.checkUserid = checkUserid;
	}

	public String getCheckTime() {
		return checkTime;
	}

	public void setCheckTime(String checkTime) {
		this.checkTime = checkTime;
	}

	public String getCollectorUserid() {
		return collectorUserid;
	}

	public void setCollectorUserid(String collectorUserid) {
		this.collectorUserid = collectorUserid;
	}

	public String getCollectorTime() {
		return collectorTime;
	}

	public void setCollectorTime(String collectorTime) {
		this.collectorTime = collectorTime;
	}

	public String getCheckDepartment() {
		return checkDepartment;
	}

	public void setCheckDepartment(String checkDepartment) {
		this.checkDepartment = checkDepartment;
	}

	public String getCheckMode() {
		return checkMode;
	}

	public void setCheckMode(String checkMode) {
		this.checkMode = checkMode;
	}

	public String getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getModifyUserid() {
		return modifyUserid;
	}

	public void setModifyUserid(String modifyUserid) {
		this.modifyUserid = modifyUserid;
	}

	public String getConfirmUserid() {
		return confirmUserid;
	}

	public void setConfirmUserid(String confirmUserid) {
		this.confirmUserid = confirmUserid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProblemLevel() {
		return problemLevel;
	}

	public void setProblemLevel(String problemLevel) {
		this.problemLevel = problemLevel;
	}

	public String getPhotoExist() {
		return photoExist;
	}

	public void setPhotoExist(String photoExist) {
		this.photoExist = photoExist;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getFc() {
		return fc;
	}

	public void setFc(String fc) {
		this.fc = fc;
	}

	public String getMemoUser() {
		return memoUser;
	}

	public void setMemoUser(String memoUser) {
		this.memoUser = memoUser;
	}

	public String getClassWeight() {
		return classWeight;
	}

	public void setClassWeight(String classWeight) {
		this.classWeight = classWeight;
	}

	public String getProblemWeight() {
		return problemWeight;
	}

	public void setProblemWeight(String problemWeight) {
		this.problemWeight = problemWeight;
	}

	public String getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(String totalWeight) {
		this.totalWeight = totalWeight;
	}

	public String getWordYear() {
		return wordYear;
	}

	public void setWordYear(String wordYear) {
		this.wordYear = wordYear;
	}

	
	@Override
	public String rowId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowId(String rowId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjType objType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<IRow>> children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int mesh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "";
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

	
	

}
