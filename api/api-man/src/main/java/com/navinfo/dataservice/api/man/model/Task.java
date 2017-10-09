package com.navinfo.dataservice.api.man.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/** 
* @ClassName:  Task 
* @author code generator
* @date 2016-06-06 06:12:30 
* @Description: TODO
*/
public class Task implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int taskId ;
	private String name;
	private int blockId=0 ;
	private String blockName;
	private int regionId=0;
	private int programId=0;
	private String programName;
	private String version;
	private int createUserId ;
	private String createUserName;
	private Timestamp createDate ;
	private int status ;
	private String descp ;
	private Timestamp planStartDate ;
	private Timestamp planEndDate ;
	private int type;
	private int programType;
	private Timestamp producePlanStartDate ;
	private Timestamp producePlanEndDate ;
	private int lot ;
	private int groupId;
	private String groupName;
	private float roadPlanTotal ;
	private int poiPlanTotal ;
	private int roadPlanIn;
	private int roadPlanOut;
	private int poiPlanIn;
	private int poiPlanOut;
	private int latest ;
	private int groupLeader =0; 
	private String workProperty ;
	private int dataPlanStatus;
	
	private String workKind;
	private String overdueReason;
	private String overdueOtherReason;
	private int tips2MarkNum;
	//是否已经分配子任务
	private int isAssign;
	private String uploadMethod;
	
	public String getUploadMethod() {
		return uploadMethod;
	}

	public void setUploadMethod(String uploadMethod) {
		this.uploadMethod = uploadMethod;
	}

	public int getIsAssign() {
		return isAssign;
	}

	public void setIsAssign(int isAssign) {
		this.isAssign = isAssign;
	}

	public int getTips2MarkNum() {
		return tips2MarkNum;
	}

	public void setTips2MarkNum(int tips2MarkNum) {
		this.tips2MarkNum = tips2MarkNum;
	}

	private Map<String, Object> changeFields=new HashMap<String, Object>();
	
	public Map<String, Object> getChangeFields() {
		return changeFields;
	}

	public String getWorkKind() {
		return workKind;
	}
	
	public List<Integer> getWorkKindList(){
		String[] kindList = this.workKind.split("\\|");
		List<Integer> result=new ArrayList<Integer>();
		for(int i=0;i<kindList.length;i++){
			if(kindList[i].equals("1")){
				result.add(i+1);
			}
		}
		return result;
	}


	public void setWorkKind(String workKind) {
		changeFields.put("WORK_KIND", workKind);
		this.workKind = workKind;
	}
	/**
	 * 将JSONArray转成workKind，例如workKindArray=[1，2，3]，workKind=1|1|1|0
	 * @param workKindArray
	 */
	public void setWorkKind(JSONArray workKindArray){
		if(workKindArray==null||workKindArray.size()==0){
			this.workKind="0|0|0|0";
			return;
		}
		String result = "0|0|0|0";
		for(Object kind:workKindArray){
			int t=Integer.valueOf(kind.toString());
			result=result.substring(0,(t-1)*2)+"1"+result.substring((t-1)*2+1,result.length());
		}
		changeFields.put("WORK_KIND", result);
		this.workKind= result;
	}
	
	/**
	 * workKind=0|1|0|0,num=2,则返回1，num=1，则返回0
	 * 1外业采集，2众包，3情报矢量，4多源
	 * @param num
	 * @return
	 */
	public int getSubWorkKind(int num){
		if(this.workKind==null||this.workKind.equals("")){return 0;}
		return Integer.valueOf(this.workKind.substring((num-1)*2, (num-1)*2+1));
	}

	private JSONObject geometry;
	private Map<Integer,Integer> gridIds;
	
	private String method;
	private String adminName;
	private int inforStage;
	
	public Task (){
	}

	
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		changeFields.put("TASK_ID", taskId);
		this.taskId = taskId;
	}
	public int getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(int createUserId) {
		changeFields.put("CREATE_USER_ID", createUserId);
		this.createUserId = createUserId;
	}
	public Timestamp getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Timestamp createDate) {
		changeFields.put("CREATE_DATE", createDate);
		this.createDate = createDate;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		changeFields.put("STATUS", status);
		this.status = status;
	}
	public String getDescp() {
		if(null==descp){return "";}
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
		changeFields.put("DESCP", descp);
	}
	public Timestamp getPlanEndDate() {
		return planEndDate;
	}
	public void setPlanEndDate(Timestamp planEndDate) {
		changeFields.put("PLAN_END_DATE", planEndDate);
		this.planEndDate = planEndDate;
	}
	public int getLatest() {
		return latest;
	}
	public void setLatest(int latest) {
		changeFields.put("LATEST", latest);
		this.latest = latest;
	}
	
	public String getName() {
		if(null==name){return "";}
		return name;
	}
	public void setName(String name) {
		this.name = name;
		changeFields.put("NAME", name);
	}
	public Timestamp getPlanStartDate() {
		return planStartDate;
	}
	public void setPlanStartDate(Timestamp planStartDate) {
		this.planStartDate = planStartDate;
		changeFields.put("PLAN_START_DATE", planStartDate);
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getCreateUserName() {
		return createUserName;
	}
	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}


	/**
	 * @return the blockId
	 */
	public int getBlockId() {
		return blockId;
	}


	/**
	 * @param blockId the blockId to set
	 */
	public void setBlockId(int blockId) {
		this.blockId = blockId;
		changeFields.put("BLOCK_ID", blockId);
	}


	/**
	 * @return the regionId
	 */
	public int getRegionId() {
		return regionId;
	}


	/**
	 * @param regionId the regionId to set
	 */
	public void setRegionId(int regionId) {
		this.regionId = regionId;
		changeFields.put("REGION_ID", regionId);
	}


	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
		changeFields.put("TYPE", type);
	}


	/**
	 * @return the producePlanStartDate
	 */
	public Timestamp getProducePlanStartDate() {
		return producePlanStartDate;
	}


	/**
	 * @param producePlanStartDate the producePlanStartDate to set
	 */
	public void setProducePlanStartDate(Timestamp producePlanStartDate) {
		this.producePlanStartDate = producePlanStartDate;
		changeFields.put("PRODUCE_PLAN_START_DATE", producePlanStartDate);
	}


	/**
	 * @return the producePlanEndDate
	 */
	public Timestamp getProducePlanEndDate() {
		return producePlanEndDate;
	}


	/**
	 * @param producePlanEndDate the producePlanEndDate to set
	 */
	public void setProducePlanEndDate(Timestamp producePlanEndDate) {
		this.producePlanEndDate = producePlanEndDate;
		changeFields.put("PRODUCE_PLAN_END_DATE", producePlanEndDate);
	}


	/**
	 * @return the lot
	 */
	public int getLot() {
		return lot;
	}


	/**
	 * @param lot the lot to set
	 */
	public void setLot(int lot) {
		this.lot = lot;
		changeFields.put("LOT", lot);
	}


	/**
	 * @return the groupId
	 */
	public int getGroupId() {
		return groupId;
	}


	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
		changeFields.put("GROUP_ID", groupId);
	}


	/**
	 * @return the roadPlanTotal
	 */
	public float getRoadPlanTotal() {
		return roadPlanTotal;
	}


	/**
	 * @param roadPlanTotal the roadPlanTotal to set
	 */
	public void setRoadPlanTotal(float roadPlanTotal) {
		this.roadPlanTotal = roadPlanTotal;
		changeFields.put("ROAD_PLAN_TOTAL", roadPlanTotal);
	}


	/**
	 * @return the poiPlanTotal
	 */
	public int getPoiPlanTotal() {
		return poiPlanTotal;
	}


	/**
	 * @param poiPlanTotal the poiPlanTotal to set
	 */
	public void setPoiPlanTotal(int poiPlanTotal) {
		this.poiPlanTotal = poiPlanTotal;
		changeFields.put("POI_PLAN_TOTAL", poiPlanTotal);
	}


	/**
	 * @return the programId
	 */
	public int getProgramId() {
		return programId;
	}


	/**
	 * @param programId the programId to set
	 */
	public void setProgramId(int programId) {
		this.programId = programId;
		changeFields.put("PROGRAM_ID", programId);
	}


	/**
	 * @return the groupLeader
	 */
	public int getGroupLeader() {
		return groupLeader;
	}


	/**
	 * @param groupLeader the groupLeader to set
	 */
	public void setGroupLeader(int groupLeader) {
		this.groupLeader = groupLeader;
	}


	/**
	 * @return the blockName
	 */
	public String getBlockName() {
		if(blockName==null){
			return "";
		}
		return blockName;
	}


	/**
	 * @param blockName the blockName to set
	 */
	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}


	/**
	 * @return the programName
	 */
	public String getProgramName() {
		if(programName==null){
			return "";
		}
		return programName;
	}


	/**
	 * @param programName the programName to set
	 */
	public void setProgramName(String programName) {
		this.programName = programName;
	}


	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}


	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	/**
	 * @return the workProperty
	 */
	public String getWorkProperty() {
		return workProperty;
	}


	/**
	 * @param workProperty the workProperty to set
	 */
	public void setWorkProperty(String workProperty) {
		this.workProperty = workProperty;
		changeFields.put("WORK_PROPERTY",workProperty);
	}


	/**
	 * @return the programType
	 */
	public int getProgramType() {
		return programType;
	}


	/**
	 * @param programType the programType to set
	 */
	public void setProgramType(int programType) {
		this.programType = programType;
	}


	/**
	 * @return the geometry
	 */
	public JSONObject getGeometry() {
		return geometry;
	}


	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(JSONObject geometry) {
		this.geometry = geometry;
		changeFields.put("GEOMETRY", geometry);
	}


	/**
	 * @return the gridIds
	 */
	public Map<Integer,Integer> getGridIds() {
		return gridIds;
	}


	/**
	 * @param gridIds the gridIds to set
	 */
	public void setGridIds(Map<Integer,Integer> gridIds) {
		this.gridIds = gridIds;
	}


	public String getMethod() {
		return method;
	}


	public void setMethod(String method) {
		this.method = method;
		changeFields.put("METHOD", method);
	}


	public String getAdminName() {
		return adminName;
	}


	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getOverdueReason() {
		return overdueReason;
	}

	public void setOverdueReason(String overdueReason) {
		this.overdueReason = overdueReason;
		changeFields.put("OVERDUE_REASON", overdueReason);
	}

	public String getOverdueOtherReason() {
		return overdueOtherReason;
	}

	public void setOverdueOtherReason(String overdueOtherReason) {
		this.overdueOtherReason = overdueOtherReason;
		changeFields.put("OVERDUE_OTHER_REASON", overdueOtherReason);
	}

	public int getInforStage() {
		return inforStage;
	}

	public void setInforStage(int inforStage) {
		this.inforStage = inforStage;
		changeFields.put("INFOR_STAGE", inforStage);
	}

	public int getDataPlanStatus() {
		return dataPlanStatus;
	}

	public void setDataPlanStatus(int dataPlanStatus) {
		changeFields.put("DATA_PLAN_STATUS", dataPlanStatus);
		this.dataPlanStatus = dataPlanStatus;
	}
	
	public int getRoadPlanIn() {
		return roadPlanIn;
	}

	public void setRoadPlanIn(int roadPlanIn) {
		changeFields.put("ROAD_PLAN_IN", roadPlanIn);
		this.roadPlanIn = roadPlanIn;
	}

	public int getRoadPlanOut() {
		return roadPlanOut;
	}

	public void setRoadPlanOut(int roadPlanOut) {
		changeFields.put("ROAD_PLAN_OUT", roadPlanOut);
		this.roadPlanOut = roadPlanOut;
	}

	public int getPoiPlanIn() {
		return poiPlanIn;
	}

	public void setPoiPlanIn(int poiPlanIn) {
		changeFields.put("POI_PLAN_IN", poiPlanIn);
		this.poiPlanIn = poiPlanIn;
	}

	public int getPoiPlanOut() {
		return poiPlanOut;
	}

	public void setPoiPlanOut(int poiPlanOut) {
		changeFields.put("POI_PLAN_OUT", poiPlanOut);
		this.poiPlanOut = poiPlanOut;
	}

}
