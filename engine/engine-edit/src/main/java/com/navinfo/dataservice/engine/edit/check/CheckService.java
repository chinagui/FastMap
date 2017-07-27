package com.navinfo.dataservice.engine.edit.check;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.selector.CkRuleSelector;
import com.navinfo.dataservice.dao.check.selector.CkSuiteSelector;
import com.navinfo.dataservice.dao.log.LogGridStat;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckService {

	public CheckService() {
		// TODO Auto-generated constructor stub
	}
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private static class SingletonHolder{
		private static final CheckService INSTANCE =new CheckService();
	}
	public static CheckService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 
	 * @param dbId
	 * @param subTaskId
	 * @param userId
	 * @param checkType 检查类型(1 poi粗编 ;2 poi精编 ; 3 道路粗编 ; 4道路精编 ; 5道路名 ; 6 其他;)
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public long checkRun(int subtaskId,long userId,int checkType,JSONObject jsonReq) throws Exception{
		long jobId=0;
		
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		if (subtaskObj == null) {
			throw new Exception("subtaskid未找到数据");
		}	
		int dbId=subtaskObj.getDbId();
		
		String ckRules = "";		
		if (jsonReq.containsKey("ckRules")) {
			ckRules = jsonReq.getString("ckRules");
		}
		
		List<String> ruleList=new ArrayList<String>();
		if (!ckRules.isEmpty()) {
			String[] rules = ckRules.split(",");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		}
		
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		if(checkType == 5){  //道路名检查 ,直接调元数据库 子版本检查		
			log.info(" begion 道路名检查 ");
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			Integer metaDbid = metaDb.getDbId();
			if(metaDbid != null && metaDbid >0){
				//根据subtask_id 获取rd_name 的nameIds
                FccApi apiFcc = (FccApi) ApplicationContextUtil.getBean("fccApi");
                JSONArray tips = apiFcc.searchDataBySpatial(subtaskObj.getGeometry(), subtaskId, 1901, new JSONArray());
                
                log.info("tips: "+tips);
                //获取当前子任务下所有的道路名id
                List<Integer> nameIds = getNameIds(subtaskId, tips);
                log.info(" nameIds.size():  "+nameIds.size());
                log.info(" begin 子任务范围内 道路名子版本检查 ");
				
				String jobName = "元数据库检查";
				/*if(jsonReq.containsKey("jobName") && jsonReq.getString("jobName") != null 
						&& StringUtils.isNotEmpty(jsonReq.getString("jobName")) && !jsonReq.getString("jobName").equals("null")){
					jobName = "webrdName:"+jsonReq.getString("jobName");
				}else{
					jobName = "webrdName:"+"元数据库检查";
				}
			*/
				JSONObject validationRequestJSON=new JSONObject();
				validationRequestJSON.put("name", "");
				validationRequestJSON.put("nameGroupid", "");
				validationRequestJSON.put("adminId", "");
				validationRequestJSON.put("roadTypes", "");
				validationRequestJSON.put("rules", ruleList);
				validationRequestJSON.put("nameIds", nameIds);
				jobId=apiService.createJob("metaValidation", validationRequestJSON, userId, subtaskId, jobName);
				
			}
		}else if(checkType==3 ||checkType ==4 || checkType ==1){//道路 + poi粗编
//			List<Integer> grids= new ArrayList<Integer>();
//			if(subtaskObj.getGridIds() != null && subtaskObj.getGridIds().size() >0){
//				 grids= subtaskObj.getGridIds();
//			}
			List<Integer> grids = null;
			Connection conn = null;
			try{
				conn = DBConnector.getInstance().getConnectionById(dbId);
				LogGridStat stat = new LogGridStat(conn);
				grids = stat.statGridsBySubtaskId(subtaskObj.getSubtaskId());
				if(grids==null||grids.size()==0){
					throw new Exception("子任务（"+subtaskObj.getSubtaskId()+"）中作业范围为空，不需要执行检查。");
				}
			}catch(Exception e){
				log.error("获取作业范围grids出错："+e.getMessage(),e);
				throw e;
			}finally{
				DbUtils.closeQuietly(conn);
			}
//			if(subtaskObj.getGridIds().keySet() != null && subtaskObj.getGridIds().keySet().size() >0){
//				 grids= (List<Integer>) subtaskObj.getGridIds().keySet();
//			}
			
			JSONObject validationRequestJSON=new JSONObject();
			validationRequestJSON.put("grids", grids);
			validationRequestJSON.put("rules", ruleList);
			validationRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("gdbValidation", validationRequestJSON, userId,subtaskId, "检查");
		}//20161214 by zxy,目前行编按需检查规则未实现，不能使用新框架进行检查，暂时行编自定义检查还是通过cop检查执行，
		//但是相关job接口已ok，检查规则实现后，开放即可
		else if(checkType ==9){//checkType ==1poi行编
			JSONObject poiRequestJSON=new JSONObject();
//			*checkType ==0poi行编 
//			 * 必传参数：subtaskId,ckRules
//			 * 测试用参数：pids
			JSONArray pids = null;
			if (jsonReq.containsKey("pids")) {
				pids = jsonReq.getJSONArray("pids");
			}
			poiRequestJSON.put("pids", pids);
			poiRequestJSON.put("rules", ruleList);
			poiRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("poiRowValidation", poiRequestJSON, userId,subtaskId, "检查");
		}
		else if (checkType ==2){//checkType ==2poi精编
			JSONObject poiRequestJSON=new JSONObject();
			/*checkType ==1poi精编 
			 * 必传参数：subtaskId，firstWorkItem，secondWorkItem
			 * 测试用参数：pids,ckRules
			 */
			JSONArray pids = null;
			if (jsonReq.containsKey("pids")) {
				pids = jsonReq.getJSONArray("pids");
			}
			poiRequestJSON.put("pids", pids);
			poiRequestJSON.put("rules", ruleList);
			poiRequestJSON.put("targetDbId", dbId);
			
			String firstWorkItem = "";
			if (jsonReq.containsKey("firstWorkItem")) {
				firstWorkItem = jsonReq.getString("firstWorkItem");
			}
			poiRequestJSON.put("firstWorkItem", firstWorkItem);
			String secondWorkItem = "";
			if (jsonReq.containsKey("secondWorkItem")) {
				secondWorkItem = jsonReq.getString("secondWorkItem");
			}
			poiRequestJSON.put("secondWorkItem", secondWorkItem);
			int status=1;
			if (jsonReq.containsKey("status")) {
				status = jsonReq.getInt("status");
			}
			poiRequestJSON.put("status", status);
			jobId=apiService.createJob("poiColumnValidation", poiRequestJSON, userId,subtaskId, "检查");
		}
		
		return jobId;
	}
	
	/**
	 * @Title: metaCheckRun
	 * @Description: 元数据编辑平台检查
	 * @param userId
	 * @param checkType
	 * @param jsonReq
	 * @return
	 * @throws Exception  long
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月20日 下午3:25:35 
	 */
	public long metaCheckRun(long userId,int checkType,JSONObject jsonReq) throws Exception{
		long jobId=0;
		String ckRules = "";		
		if (jsonReq.containsKey("ckRules")) {
			ckRules = jsonReq.getString("ckRules");
		}
		
		List<String> ruleList=new ArrayList<String>();
		if (!ckRules.isEmpty()) {
			String[] rules = ckRules.split(",");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		}
		
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		
		if(checkType == 5){//道路名子版本检查+全库检查
			
			log.info(" begin 道路名子版本检查+全库检查 ");
			JSONObject paramsObj = new JSONObject();
			if(jsonReq.containsKey("params") && jsonReq.getJSONObject("params") != null ){
				paramsObj = jsonReq.getJSONObject("params");
			}
			
			String name = "" ;
			if(paramsObj.containsKey("name") && paramsObj.getString("name") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("name")) && !paramsObj.getString("name").equals("null")){
				name = paramsObj.getString("name");
			}
			String nameGroupid = "";
			if(paramsObj.containsKey("nameGroupid") && paramsObj.getString("nameGroupid") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("nameGroupid")) && !paramsObj.getString("nameGroupid").equals("null")){
				nameGroupid = paramsObj.getString("nameGroupid");
			}	
			String adminId = "";
			if(paramsObj.containsKey("adminId") && paramsObj.getString("adminId") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("adminId")) && !paramsObj.getString("adminId").equals("null")){
				adminId = paramsObj.getString("adminId");
			}
			String roadTypes = "";
			if(paramsObj.containsKey("roadTypes") && paramsObj.getJSONArray("roadTypes") != null && paramsObj.getJSONArray("roadTypes").size() > 0 ){
				JSONArray arr = paramsObj.getJSONArray("roadTypes");
				roadTypes = arr.join(",");
			}
			List<Integer> nameIds = new ArrayList<Integer>();
			if(jsonReq.containsKey("nameIds") && jsonReq.getJSONArray("nameIds") != null && jsonReq.getJSONArray("nameIds").size() > 0 ){
				log.info("meta check selectdata : nameIds:"+jsonReq.getJSONArray("nameIds"));
				JSONArray arr = jsonReq.getJSONArray("nameIds");
				nameIds = (List<Integer>) JSONArray.toCollection(arr);
				log.info("nameIds list:"+nameIds.size());
//				nameIds = arr.join(",");
			}
			
			
			
			
			String jobName = "";
			if(jsonReq.containsKey("jobName") && jsonReq.getString("jobName") != null 
					&& StringUtils.isNotEmpty(jsonReq.getString("jobName")) && !jsonReq.getString("jobName").equals("null")){
				jobName = "rdName:"+jsonReq.getString("jobName");
			}
			
			log.info("name :"+name+" nameGroupid: "+nameGroupid+" adminId:"+adminId+" roadTypes:"+roadTypes+" nameIds: "+nameIds+" jobName: "+jobName);
			JSONObject validationRequestJSON=new JSONObject();
			validationRequestJSON.put("name", name);
			validationRequestJSON.put("nameGroupid", nameGroupid);
			validationRequestJSON.put("adminId", adminId);
			validationRequestJSON.put("roadTypes", roadTypes);
			validationRequestJSON.put("rules", ruleList);
			validationRequestJSON.put("nameIds", nameIds);
			jobId=apiService.createJob("metaValidation", validationRequestJSON, userId, 0, jobName);
		}
		return jobId;
	}
	
	/**
	 * 检查规则查询
	 * @param request
	 * @return
	 * @author wangdongbin
	 */
	public JSONArray getCkRules(int pageSize,int pageNum, int type) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkSuiteSelector suiteSelector = new CkSuiteSelector(conn);
			
			JSONArray suiteArray = suiteSelector.getSuite(pageSize, pageNum, type);
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRules(suiteArray);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkRuleCodes
	 * @Description: 根据检查类型获取规则号
	 * @param type
	 * @return
	 * @throws Exception  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月24日 下午2:33:42 
	 */
	public JSONArray getCkRuleCodes( Integer type) throws Exception {
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRulesByType(type);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkRuleCodes
	 * @Description: 根据规则号获取规则名称
	 * @param ruleCode
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月17日 下午3:12:14 
	 */
	public String getRuleNameById( String ruleCode) throws Exception {
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRuleNameById(ruleCode);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkSuites
	 * @Description: 获取某类检查的所有 suite
	 * @param type
	 * @param flag 
	 * @return
	 * @throws Exception  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月19日 下午2:01:13 
	 */
	public JSONArray getCkSuites(int type, int flag) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			CkSuiteSelector suiteSelector = new CkSuiteSelector(conn);
			
			JSONArray suiteArray = suiteSelector.getSuite(type,flag);
			
			return suiteArray;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public JSONArray getCkRulesBySuiteId(String suiteId, String ruleCode) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getCkRulesBySuiteId(suiteId,ruleCode);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	public List<Integer> getNameIds(int subtaskId,JSONArray tips) throws Exception {
		PreparedStatement pstmt = null;
		Connection conn = null;
		ResultSet resultSet = null;
		List<Integer> nameIds = null;
		
		try {
			nameIds = new ArrayList<Integer>();
			conn = DBConnector.getInstance().getMetaConnection();
			
			StringBuilder sql = new StringBuilder();
			String ids = "";
			String tmep = "";
			Clob pidClod = null;
			//添加根据子任务id直接查询的sql 
			sql.append("SELECT  * ");
			sql.append(" FROM ( ");
			
			sql.append("SELECT name_id  from rd_name r  where r.src_resume = '\"task\":"+ subtaskId +"' ");
			
			if (tips.size()>0) {
				sql.append(" union all  ");
				sql.append(" SELECT tt.name_id  FROM ( ");
				sql.append(" select substr(replace(t.src_resume,'\"',''),instr(replace(t.src_resume,'\"',''), ':') + 1,length(replace(src_resume,'\"',''))) as tipid,t.name_id ");
				sql.append(" from rd_name t  where t.src_resume like '%tips%' ) tt ");
				sql.append(" where 1=1 ");
				
				for (int i=0;i<tips.size();i++) {
					JSONObject tipsObj = tips.getJSONObject(i);
					ids += tmep;
					tmep = ",";
					ids +=tipsObj.getString("id");
				}
				pidClod = ConnectionUtil.createClob(conn);
				pidClod.setString(1, ids);
				sql.append(" and tt.tipid in (select column_value from table(clob_to_table(?)))");
			}
			sql.append(" )  ");
			log.info(" getNameIds :"+sql.toString());
			pstmt = conn.prepareStatement(sql.toString());
			if (tips.size()>0) {
				pstmt.setClob(1, pidClod);
			}
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				nameIds.add(resultSet.getInt("name_id"));
			}
			
			return nameIds;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
