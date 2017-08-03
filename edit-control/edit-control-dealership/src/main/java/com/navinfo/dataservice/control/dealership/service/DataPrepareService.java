package com.navinfo.dataservice.control.dealership.service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.dealership.diff.DiffService;
import com.navinfo.dataservice.control.dealership.service.excelModel.DiffTableExcel;
import com.navinfo.dataservice.control.dealership.service.excelModel.exportWorkResultEntity;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpDbDiffResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 代理店数据准备类
 * 
 * @author jicaihua
 *
 */
public class DataPrepareService {
	private static Logger log = LoggerRepos.getLogger(DataPrepareService.class);

	private DataPrepareService() {
	}

	private static class SingletonHolder {
		private static final DataPrepareService INSTANCE = new DataPrepareService();
	}

	public static DataPrepareService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 查询代理店品牌
	 * @param chainStatus品牌状态
	 * @param begainSize 开始的数据条数
	 * @param endSize 结束的数据条数
	 * @return 分页后的某种品牌状态的List
	 * @author songhe
	 * 
	 * */
	public List<Map<String, Object>> queryDealerBrand(int chainStatus,int pageSize, int pageNum) throws SQLException{
		
		//处理分页数据
		int begainSize = (pageSize-1) * pageNum+1;
		int endSize = pageSize * pageNum;
		Connection con = null;
		try{
			con = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = null;
			if(chainStatus != -1){
				selectSql = "SELECT * FROM "
						+ "(SELECT A.*, ROWNUM RN FROM "
						+ "(SELECT t.* FROM IX_DEALERSHIP_CHAIN t where t.chain_status  = " + chainStatus + ") "
								+ "A WHERE ROWNUM <= " + endSize + ")WHERE RN >= " + begainSize;
			}else{
				selectSql = "SELECT t.* FROM IX_DEALERSHIP_CHAIN t";
			}
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> dealerShipList = new ArrayList();
					while (rs.next()) {
						Map<String, Object> result = new HashMap<>();
						result.put("chainCode", rs.getString("CHAIN_CODE"));
						result.put("chainName", rs.getString("CHAIN_NAME"));
						result.put("chainWeight", rs.getInt("CHAIN_WEIGHT"));
						result.put("chainStatus", rs.getInt("CHAIN_STATUS"));
						result.put("workType", rs.getInt("WORK_TYPE"));
						result.put("workStauts", rs.getInt("WORK_STATUS"));
						dealerShipList.add(result);
					}
					return dealerShipList;
				}
			};
			
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
		return null;
	}
	
	/**
	 * 判断品牌状态
	 * @throws Exception 
	 * 
	 * 
	 * */
	public Map<String, Integer> getPrepareData(String chainCode, Connection con) throws Exception{
		QueryRunner run = new QueryRunner();
		try{
			String sql = "select t.work_status from IX_DEALERSHIP_CHAIN t where t.chain_code = '"+chainCode+"'";
			ResultSetHandler<Map<String, Integer>> rs = new ResultSetHandler<Map<String, Integer>>() {
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> result = new HashMap<>();
					if(rs.next()){
						result.put("workStatus", rs.getInt("WORK_STATUS"));
//						result.put("dealSrcDiff", rs.getInt("deal_src_diff"));
						return result;
					}
					return null;
				}
			};
			return run.query(con, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 差分结果列表
	 * @param chainCode品牌代码
	 * @return 
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public List<Map<String, Object>> loadDiffList(String chainCode) throws Exception{
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			
			Map<String, Integer> prepareResult = getPrepareData(chainCode, con);
			if(prepareResult == null){
				throw new Exception("chainCode不存在");
			}
			int workStatus = prepareResult.get("workStatus");
			//这个为3的时候，没有sorce表的数据
//			int dealSrcDiff = prepareResult.get("dealSrcDiff");
			String sql = "";
			if(workStatus == 0 || workStatus == 1){
				//表表查分查询sql
//				sql = "select r.deal_src_diff, r.province,r.poi_num_1,r.poi_num_2,r.poi_num_3,r.poi_num_4,r.poi_num_5,r.result_id,s.source_id,r.city,r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status "
//						+ "from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_CHAIN c "
//						+ "where r.source_id = s.source_id and c.chain_code = r.chain and r.chain =  '"+chainCode+"'" +"and r.workflow_status = 0";
				sql = "select r.deal_src_diff, r.province,r.poi_num_1,r.poi_num_2,r.poi_num_3,r.poi_num_4,r.poi_num_5,r.result_id,s.source_id,r.city,"
						+ "r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status from "
						+ "(IX_DEALERSHIP_RESULT r left join IX_DEALERSHIP_SOURCE s on r.source_id = s.source_id) left join IX_DEALERSHIP_CHAIN c on "
						+ "r.chain = c.chain_code where c.chain_code = '"+chainCode+"'" +"and r.workflow_status = 0";
			}else if(workStatus == 2 || workStatus == 3){
				//表库查分查询sql
//				sql = "select r.deal_src_diff, r.province,r.poi_num_1,r.poi_num_2,r.poi_num_3,r.poi_num_4,r.poi_num_5,r.result_id,s.source_id,r.city,r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status "
//						+ "from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_CHAIN c "
//						+ "where r.source_id = s.source_id and c.chain_code = r.chain and r.chain =  '"+chainCode+"'";
				sql = "select r.deal_src_diff, r.province,r.poi_num_1,r.poi_num_2,r.poi_num_3,r.poi_num_4,r.poi_num_5,r.result_id,s.source_id,r.city,"
						+ "r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status from "
						+ "(IX_DEALERSHIP_RESULT r left join IX_DEALERSHIP_SOURCE s on r.source_id = s.source_id) left join IX_DEALERSHIP_CHAIN c on "
						+ "r.chain = c.chain_code where c.chain_code = '"+chainCode+"'";
			}else{
				throw new Exception("品牌对应状态码异常为："+workStatus);
			}
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> diffList = new ArrayList();
					while (rs.next()) {
						Map<String, Object> result = new HashMap<>();
						result.put("resultId", rs.getInt("result_id"));
						result.put("sourceId", rs.getInt("source_id"));
						result.put("province", rs.getString("province"));
						result.put("city", rs.getString("city"));
						result.put("kindCode", rs.getString("kind_code"));
						result.put("resultName", rs.getString("result_name"));
						result.put("sourceName", rs.getString("source_name"));
						result.put("workType", rs.getInt("work_type"));
						result.put("dealSrcDiff", rs.getInt("deal_src_diff"));
						result.put("workflowStatus", rs.getInt("workflow_status"));
						int poiNum = calculatePoiNum(rs);
						result.put("poiNum", poiNum);
						diffList.add(result);
					}
					return diffList;
				}
			};
			log.info("loadDiffList-->sql:"+sql);
			return run.query(con, sql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(con);
			throw e;
		}finally{
			DbUtils.commitAndClose(con);
		}
	}

	/**
	 * 1.功能描述：表差分结果人工整理完毕后，上传入库
	 * 2.实现逻辑：
	 * 详见需求：一体化代理店业务需求-》表表差分结果导入
	 * 3.使用场景：
	 * 	1）代理店编辑平台-数据准备-表表差分
	 * @param chainCode
	 * @param upFile
	 * @throws Exception
	 */
	public void impTableDiff(HttpServletRequest request,Long userId)throws Exception {
		log.info("start 文件表表差分导入");
		
//		JSONObject dataJson = InputStreamUtils.request2Parameter(request);
//		if (dataJson == null) {
//			throw new IllegalArgumentException("parameter参数不能为空。");
//		}
//		String chainCode = dataJson.getString("chainCode");
		
		//excel文件上传到服务器		
		//保存文件
		String filePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath)+"/dealership/fullChainExcel";  //服务器部署路径 /data/resources/upload
		//String filePath ="D:/temp/dealership/fullChainExcel";
		log.info("文件由本地上传到服务器指定位置"+filePath);
		JSONObject returnParam = InputStreamUtils.request2File(request, filePath);
		String localFile=returnParam.getString("filePath");
		String chainCode = returnParam.getString("chainCode");
//		String localFile="E:/temp/20170720162631.xls";
//		String chainCode = "400F";
		log.info("文件已上传至"+localFile);
		//导入表表差分结果excel
		List<Map<String, Object>> sourceMaps=impDiffExcel(localFile);
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getDealershipConnection();
			//导入到oracle库中
			//excel的listmap转成list<bean>
			Set<Integer> resultIdSet=new HashSet<Integer>();
//			Set<Integer> sourceIdSet=new HashSet<Integer>();
			Set<String> chainSet=new HashSet<String>();
			List<DiffTableExcel> excelSet=new ArrayList<DiffTableExcel>();
			List<Integer> excelOldSourceIdList=new ArrayList<Integer>();
			for(Map<String,Object> source:sourceMaps){
				JSONObject json = JSONObject.fromObject(source);
				DiffTableExcel diffSub=(DiffTableExcel) JSONObject.toBean(json, DiffTableExcel.class);
				excelSet.add(diffSub);
				excelOldSourceIdList.add(diffSub.getOldSourceId());
				resultIdSet.add(diffSub.getResultId());
//				sourceIdSet.add(diffSub.getOldSourceId());
				chainSet.add(diffSub.getChain());
				//导入时，判断导入文件中“代理店品牌”是否跟作业品牌一致，如果一致，则可以导入，否则不可以导入
				if(!chainCode.equals(diffSub.getChain())){
					log.info("导入文件中“代理店品牌”是否跟作业品牌不一致");
					throw new Exception("导入文件中“代理店品牌”是否跟作业品牌不一致");
			}
				if(diffSub.getDealSrcDiff()!=1&&diffSub.getDealSrcDiff()!=2
						&&diffSub.getDealSrcDiff()!=3&&diffSub.getDealSrcDiff()!=4){
					log.info("表表差分结果中“新旧一览表差分结果”，值域必须在｛1，2，3，4｝范围内，uuid="+diffSub.getResultId());
					throw new Exception("表表差分结果中“新旧一览表差分结果”，值域必须在｛1，2，3，4｝范围内，uuid="+diffSub.getResultId());
				}
			}
			//加载IX_DEALERSHIP_RESULT中的数据
			Map<Integer, IxDealershipResult> resultObjSet = IxDealershipResultSelector.getByResultIds(conn, resultIdSet);
			//Map<Integer, IxDealershipResult> sourceObjSet = IxDealershipResultSelector.getBySourceIds(conn, sourceIdSet);
//			Map<Integer, IxDealershipSource> sourceObjSet = IxDealershipSourceSelector.getBySourceIds(conn, sourceIdSet);
			//===========================================================================================================
			//bug:8015(预处理平台_代理店_数据准备_表表差分结果导入：RESULT. SOURCE_ID在表表差分结果中“旧一览表ID”不存在，不应该导入)
			Map<Integer, IxDealershipSource> sourceObjSet = IxDealershipSourceSelector.getSourceIdsByChain(conn, chainSet);
			//===========================================================================================================
			//根据导入原则，获取需要修改的数据
			Map<String,Set<IxDealershipResult>> changeMap=importMain(excelSet,resultObjSet,sourceObjSet,excelOldSourceIdList);
			//IX_DEALERSHIP_RESULT.RESULT_ID在上传的表表差分结果中“UUID”中不存在，则将该IX_DEALERSHIP_RESULT记录物理删除；
			deleteResult(conn,chainCode,resultIdSet);
			//数据持久化到数据库
			persistChange(conn,changeMap,userId);
			//修改IX_DEALERSHIP_CHAIN状态
			IxDealershipChainOperator.changeChainStatus(conn,chainCode,1);
			log.info("end 文件表表差分导入");
		}catch(Exception e){
			log.error("", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new ServiceException(e.getMessage(), e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
		
	/**
	 * IX_DEALERSHIP_RESULT.RESULT_ID在上传的表表差分结果中“UUID”中不存在，则将该IX_DEALERSHIP_RESULT记录物理删除；
	 * @param resultIdSet
	 * @throws SQLException 
	 */
	private void deleteResult(Connection conn,String chainCode,Set<Integer> resultIdSet) throws SQLException {
		log.info("start 表表差分物理删除无效记录");
		String sql="DELETE FROM IX_DEALERSHIP_RESULT"
				+ " WHERE CHAIN = '"+chainCode+"'"
				+ "   AND RESULT_ID NOT IN ";
		QueryRunner run=new QueryRunner();
		if(resultIdSet.size()>1000){
			sql= sql+"(SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";
			Clob clob = ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(resultIdSet, ","));
			run.update(conn, sql,clob);
		}else{
			sql= sql+"('"+StringUtils.join(resultIdSet, "','")+"')";
			run.update(conn, sql);
	}

		log.info("end 表表差分物理删除无效记录");		
		
	}

	/**
	 * IxDealershipResult数据持久化到数据库
	 * @param conn
	 * @param changeMap
	 * @throws ServiceException 
	 */
	private void persistChange(Connection conn, Map<String, Set<IxDealershipResult>> changeMap,Long userId) throws ServiceException {
		if(changeMap.containsKey("ADD")){
			Set<IxDealershipResult> resultSet = changeMap.get("ADD");
			for(IxDealershipResult tmp:resultSet){
				IxDealershipResultOperator.createIxDealershipResult(conn,tmp);
			}
		}
		if(changeMap.containsKey("UPDATE")){
			Set<IxDealershipResult> resultSet = changeMap.get("UPDATE");
			for(IxDealershipResult tmp:resultSet){
				IxDealershipResultOperator.updateIxDealershipResult(conn,tmp,userId);
			}
		}
	}
	/**
	 * 根据导入原则，获取需要修改的数据
	 * @param excelSet
	 * @param resultObjSet
	 * @param sourceObjSet 
	 * @param sourceObjSet
	 * @return
	 * @throws Exception 
	 */
	private Map<String, Set<IxDealershipResult>> importMain(
			List<DiffTableExcel> excelSet, Map<Integer, IxDealershipResult> resultObjSet, Map<Integer, IxDealershipSource> sourceObjSet,List<Integer> excelOldSourceIdList) throws Exception {
		log.info("start 表表差分修改result表记录");
		Map<String, Set<IxDealershipResult>> resultMap=new HashMap<String, Set<IxDealershipResult>>();
		resultMap.put("ADD", new HashSet<IxDealershipResult>());
		resultMap.put("UPDATE", new HashSet<IxDealershipResult>());
		
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
		List<CpRegionProvince> cpRegionList = api.listCpRegionProvince();
		Map<String, Integer> cpRegionMap=new HashMap<String, Integer>();
		Map<Integer,Integer> oldSourceIdMap=new HashMap<Integer,Integer>();
		for(CpRegionProvince region:cpRegionList){
			cpRegionMap.put(region.getProvince(), region.getRegionId());
		}
		
		for(Entry<Integer, IxDealershipSource> entry : sourceObjSet.entrySet()) {  
			if (!excelOldSourceIdList.contains(entry.getKey())){
				log.info("全国一览表IX_DEALERSHIP_RESULT.SOURCE_ID在表表差分结果中“旧一览表ID”不存在：旧一览表ID="+entry.getKey());
				throw new Exception("全国一览表IX_DEALERSHIP_RESULT.SOURCE_ID在表表差分结果中“旧一览表ID”不存在：旧一览表ID="+entry.getKey());
			}		  
		}  
		for (DiffTableExcel diffSub:excelSet){
			int resultId=diffSub.getResultId();
			IxDealershipResult resultObj = null;
			int oldSourceId = diffSub.getOldSourceId();
			if(resultId==0&&oldSourceId==0){
				log.info("表表差分结果中存在“UUID”和“旧一览表ID”都为0或字符串，数据错误");
				throw new Exception("表表差分结果中存在“UUID”和“旧一览表ID”都为0或字符串，数据错误");
			}
			if(oldSourceId!=0){
				if(oldSourceIdMap.containsKey(oldSourceId)){
					log.info("表表差分结果中“旧一览表ID”不唯一,旧一览表ID="+oldSourceId);
					throw new Exception("表表差分结果中“旧一览表ID”不唯一,旧一览表ID="+oldSourceId);
				}else{
					oldSourceIdMap.put(oldSourceId, oldSourceId);
				}
			}
			if(resultId!=0){
				if(!resultObjSet.containsKey(resultId)){
					log.info("表表差分结果中“UUID”在IX_DEALERSHIP_RESULT.RESULT_ID中不存在:uuid="+resultId);
					throw new Exception("表表差分结果中“UUID”在IX_DEALERSHIP_RESULT.RESULT_ID中不存在:uuid="+resultId);
				}
				resultObj = resultObjSet.get(resultId);
				if(resultObj.getName()!=null&&!(resultObj.getName().equals(diffSub.getName()))){
					log.info("resultObj.name:"+resultObj.getName()+",diffSub.name:"+diffSub.getName());
					log.info("表表差分结果中“厂商提供名称”和库中不一致:uuid="+resultId);
					throw new Exception("表表差分结果中“厂商提供名称”和库中不一致:uuid="+resultId);
				}
				if(resultObj.getAddress()!=null&&!(resultObj.getAddress().equals(diffSub.getAddress()))){
					log.info("resultObj.address:"+resultObj.getAddress()+",diffSub.address:"+diffSub.getAddress());
					log.info("表表差分结果中“厂商提供地址”和库中不一致:uuid="+resultId);
					throw new Exception("表表差分结果中“厂商提供地址”和库中不一致:uuid="+resultId);
				}
				resultMap.get("UPDATE").add(resultObj);
			}else{
				resultObj = new IxDealershipResult();
				//resultObj.setSourceId(oldSourceId);
				resultMap.get("ADD").add(resultObj);
			}
			
			if (diffSub.getName()!=null&&!"".equals(diffSub.getName())){
				if(diffSub.getOldSourceId()!=0){
					if(diffSub.getDealSrcDiff()!=1&&diffSub.getDealSrcDiff()!=4){
						log.info("表表差分结果中“新旧一览表差分结果”的值域必须是{1,4}:uuid="+resultId);
						throw new Exception("表表差分结果中“新旧一览表差分结果”的值域必须是{1,4}:uuid="+resultId);
					}
				}else{
					if(diffSub.getDealSrcDiff()!=3){
						log.info("表表差分结果中“新旧一览表差分结果”的值域必须是{3}:uuid="+resultId);
						throw new Exception("表表差分结果中“新旧一览表差分结果”的值域必须是{3}:uuid="+resultId);
					}
				}
			}else{
				if(diffSub.getOldSourceId()!=0){
					if(diffSub.getDealSrcDiff()!=2){
						log.info("表表差分结果中“新旧一览表差分结果”的值域必须是{2}:uuid="+resultId);
						throw new Exception("表表差分结果中“新旧一览表差分结果”的值域必须是{2}:uuid="+resultId);
					}
				}
			}
			
			resultObj.setDealSrcDiff(diffSub.getDealSrcDiff());
			
			if(oldSourceId!=resultObj.getSourceId()){
				resultObj.setSourceId(diffSub.getOldSourceId());
				IxDealershipSource sourceObj = null;
				if(oldSourceId!=0){
					if(!sourceObjSet.containsKey(oldSourceId)){
						log.info("表表差分结果中“旧一览表ID”在IX_DEALERSHIP_RESULT.SOURCE_ID中不存在:SOURCE_ID="+oldSourceId);
						throw new Exception("表表差分结果中“旧一览表ID”在IX_DEALERSHIP_RESULT.SOURCE_ID中不存在:SOURCE_ID="+oldSourceId);
					}
					sourceObj=sourceObjSet.get(diffSub.getOldSourceId());
					changeResultObj(resultObj,sourceObj);
				}
				else{sourceObj=new IxDealershipSource();}
				
				if(StringUtils.isEmpty(resultObj.getProvince())){
					if(cpRegionMap.containsKey(resultObj.getProvince())){
						resultObj.setRegionId(cpRegionMap.get(resultObj.getProvince()));
					}
				}else{
					if(cpRegionMap.containsKey(sourceObj.getProvince())){
						resultObj.setRegionId(cpRegionMap.get(sourceObj.getProvince()));
					}
				}
			}
		}
		log.info("end 表表差分修改result表记录");
		return resultMap;
	}
	/**
	 * IX_DEALERSHIP_RESULT表记录，根据source获取的IX_DEALERSHIP_SOURCE，更新其相应字段。
	 * @param resultObj
	 * @param sourceObj
	 */
	private void changeResultObj(IxDealershipResult resultObj,IxDealershipSource sourceObj){
		resultObj.setCfmPoiNum(sourceObj.getCfmPoiNum());
		if(!StringUtils.isEmpty(resultObj.getCfmPoiNum())){
			resultObj.setCfmIsAdopted(1);
		}
		resultObj.setPoiKindCode(sourceObj.getPoiKindCode());
		resultObj.setPoiChain(sourceObj.getPoiChain());
		resultObj.setPoiName(sourceObj.getPoiName());
		resultObj.setPoiNameShort(sourceObj.getPoiNameShort());
		resultObj.setPoiAddress(sourceObj.getPoiAddress());
		resultObj.setPoiTel(sourceObj.getPoiTel());
		resultObj.setPoiPostCode(sourceObj.getPoiPostCode());
		resultObj.setPoiXDisplay(sourceObj.getPoiXDisplay());
		resultObj.setPoiYDisplay(sourceObj.getPoiYDisplay());
		resultObj.setPoiXGuide(sourceObj.getPoiXGuide());
		resultObj.setPoiYGuide(sourceObj.getPoiYGuide());
		resultObj.setGeometry(sourceObj.getGeometry());
		if(StringUtils.isEmpty(resultObj.getChain())){
			resultObj.setChain(sourceObj.getChain());
		}
		
	}

	private void importDiff2Oracle(List<Map<String, Object>> sourceMaps){
		
	}
	/**
	 * 表表查分结果excel读取
	 * @param upFile
	 * @return List<Map<String, Object>> excel记录值
	 * @throws Exception
	 */
	private List<Map<String, Object>> impDiffExcel(String upFile) throws Exception{
		log.info("start 导入表表差分结果excel："+upFile);
		ExcelReader excleReader = new ExcelReader(upFile);
		Map<String,String> excelHeader = new HashMap<String,String>();
		
		excelHeader.put("UUID", "resultId");
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供简称", "nameShort");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话（销售）", "telSale");
		excelHeader.put("厂商提供电话（服务）", "telService");
		excelHeader.put("厂商提供电话（其他）", "telOther");		
		excelHeader.put("厂商提供邮编", "postCode");
		excelHeader.put("厂商提供英文名称", "nameEng");
		excelHeader.put("厂商提供英文地址", "addressEng");
		
		/*		新旧一览表差分结果*/
		excelHeader.put("旧一览表ID", "oldSourceId");
		excelHeader.put("旧一览表省份", "oldProvince");
		excelHeader.put("旧一览表城市", "oldCity");
		excelHeader.put("旧一览表项目", "oldProject");
		excelHeader.put("旧一览表分类", "oldKindCode");
		excelHeader.put("旧一览表品牌", "oldChain");
		excelHeader.put("旧一览表名称", "oldName");
		excelHeader.put("旧一览表简称", "oldNameShort");
		excelHeader.put("旧一览表地址", "oldAddress");
		excelHeader.put("旧一览表电话（销售）", "oldTelSale");
		excelHeader.put("旧一览表电话（服务）", "oldTelService");
		excelHeader.put("旧一览表电话（其他）", "oldTelOther");		
		excelHeader.put("旧一览表邮编", "oldPostCode");
		excelHeader.put("旧一览表英文名称", "oldNameEng");
		excelHeader.put("旧一览表英文地址", "oldAddressEng");
		
		excelHeader.put("新旧一览表差分结果", "dealSrcDiff");
		
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		log.info("end 导入表表差分结果excel："+upFile);
		return sources;
	}
	
	/**
	 * @Title: searchTableDiff
	 * @Description: TODO
	 * @param chainCode
	 * @return
	 * @throws SQLException  List<Map<String,Object>>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年6月1日 下午2:21:32 
	 */
	public List<ExpIxDealershipResult> searchTableDiff(String chainCode) throws SQLException{
		
		Connection conn = null;
		try{
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			
			QueryRunner run = new QueryRunner();
			String selectSql = "select r.result_id,r.province , r.city, r.project , r.kind_code, r.chain , r.name ,"
					+ " r.name_short, r.address,r.tel_sale ,"
					+ " r.tel_service ,r.tel_other,r.post_code,r.name_eng,r.address_eng,s.source_id old_source_id,"
					+ " s.province old_province,s.city old_city,s.project old_project,s.kind_code old_kind_code,s.chain old_chain, "
					+ " s.name old_name,s.name_short old_name_short,s.address old_address,s.tel_sale old_tel_sale,"
					+ " s.tel_service old_tel_service,s.tel_other old_tel_other,s.post_code old_post_code,"
					+ " s.name_eng old_name_eng,s.address_eng old_address_eng,r.deal_src_diff "
					+ " from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s "
					+ " where r.source_id = s.source_id(+) "
					+ " and r.chain = '"+chainCode+"' "
					+ " and r.workflow_status = 0 ";
			log.info("selectSql: "+selectSql);
			ResultSetHandler<List<ExpIxDealershipResult>> rs = new ResultSetHandler<List<ExpIxDealershipResult>>() {
				@Override
				public List<ExpIxDealershipResult> handle(ResultSet rs) throws SQLException {
					
					List<ExpIxDealershipResult> diffList = new ArrayList<ExpIxDealershipResult>();
					while (rs.next()) {
						ExpIxDealershipResult result = new ExpIxDealershipResult();
							result.setResultId(rs.getString("result_id"));
							result.setProvince( rs.getString("province"));
							result.setCity( rs.getString("city"));
							result.setProject( rs.getString("project"));
							result.setKindCode( rs.getString("kind_code"));
							result.setChain( rs.getString("chain"));
							result.setName( rs.getString("name"));
							result.setNameShort( rs.getString("name_short"));
							result.setAddress( rs.getString("address"));
							result.setTelSale( rs.getString("tel_sale"));
							result.setTelService( rs.getString("tel_service"));
							result.setTelOther( rs.getString("tel_other"));
							result.setPostCode( rs.getString("post_code"));
							result.setNameEng( rs.getString("name_eng"));
							result.setAddressEng( rs.getString("address_eng"));
							result.setOldSourceId( rs.getString("old_source_id"));
							result.setOldProvince( rs.getString("old_province"));
							result.setOldCity( rs.getString("old_city"));
							result.setOldProject( rs.getString("old_project"));
							result.setOldKindCode( rs.getString("old_kind_code"));
							result.setOldChain( rs.getString("old_chain"));
							result.setOldName( rs.getString("old_name"));
							result.setOldNameShort( rs.getString("old_name_short"));
							result.setOldAddress( rs.getString("old_address"));
							result.setOldTelSale( rs.getString("old_tel_sale"));
							result.setOldTelService( rs.getString("old_tel_service"));
							result.setOldTelOther( rs.getString("old_tel_other"));
							result.setOldPostCode( rs.getString("old_post_code"));
							result.setOldNameEng( rs.getString("old_name_eng"));
							result.setOldAddressEng( rs.getString("old_address_eng"));
							result.setDealSrcDiff( rs.getString("deal_src_diff"));
						
						diffList.add(result);
					}
					return diffList;
				}
			};
			
			return run.query(conn, selectSql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}


	/**
	 * @param request
	 * @throws Exception 
	 */
	public void uploadChainExcel(HttpServletRequest request) throws Exception {
		Connection conn = null;
		try{

			AccessToken tokenObj=(AccessToken) request.getAttribute("token");

			long userId = tokenObj.getUserId();
			//获取代理店数据库连接
			conn = DBConnector.getInstance().getDealershipConnection();
			
			//保存文件
			String filePath = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.uploadPath)+"/dealership/fullChainExcel"; 
//			String filePath = "D:\\data\\resources\\upload\\dealership\\fullChainExcel";
			JSONObject  returnParam = InputStreamUtils.request2File(request, filePath);
			String localZipFile = returnParam.getString("filePath");
			log.info("load file");
			log.info("localZipFile :" + localZipFile);

			//解压
			String localUnzipDir = localZipFile.substring(0,localZipFile.indexOf("."));
			ZipUtils.unzipFile(localZipFile,localUnzipDir);
			log.info("unzip file");
			log.info("localUnzipDir : " + localUnzipDir);

			//获取个品牌状态
			Map<String,Integer> chainStatus = getChainStatus(conn);
			
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHMMss");
		    String date = df.format(new Date());
			
			File file = new File(localUnzipDir);
			if (file.exists()) {
				List<String> pathList = new ArrayList<String>();
				getDirectory(file,pathList);
				log.info("pathList : " + pathList.toString());

				//获取IxDealershipSource
				Map<String, List<IxDealershipSource>> dealershipSourceMap = IxDealershipSourceSelector.getAllIxDealershipSourceByChain(conn);

				for(String fileStr:pathList){
					File file2 = new File(fileStr);
					if (file2.isDirectory()) {
						continue;
					} else {
						log.info("文件:" + file2.getAbsolutePath());
						//解析excel,读取IxDealershipResult
						String chain = null;
						String fileName = file2.getAbsolutePath();
						List<Map<String, Object>> sourceMaps = impIxDealershipResultExcel(fileName);

						//Excel文件的校验
						uploadChainExcelCheck(sourceMaps, fileStr);
						
						List<IxDealershipResult> dealershipResult = new ArrayList<IxDealershipResult>();
						for(Map<String, Object> map:sourceMaps){
							IxDealershipResult ixDealershipResult = new IxDealershipResult();
							InputStreamUtils.transMap2Bean(map, ixDealershipResult);
							dealershipResult.add(ixDealershipResult);
							chain = ixDealershipResult.getChain();
						}
						if(chainStatus.containsKey(chain)&&chainStatus.get(chain)==0){
							List<IxDealershipSource> dealershipSources =  dealershipSourceMap.get(chain);
							//执行差分
							Map<Integer,List<IxDealershipResult>> resultMap = DiffService.diff(dealershipSources, dealershipResult, chain,date);
							//写库
							List<IxDealershipResult> insert = resultMap.get(1);
							log.info("insert object");
							if(insert!=null&&insert.size()>0){
								for(IxDealershipResult bean:insert){
									IxDealershipResultOperator.createIxDealershipResult(conn,bean);
								}
							}
							
							int workType = 2;
							int workStatus = 0;
							int chain_status = 1;
							updateIxDealershipChain(conn,chain,workStatus,workType,chain_status);
							log.info("import chian:" + chain);
						}
					}
				}

			}
		}catch(IllegalArgumentException e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			if(e.getMessage().equals("MALFORMED")){
				throw new ServiceException("更新失败，原因为:上传文件名有中文");
			}
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}
		catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	//Excel文件的校验（一栏表上传接口：6877）
	private void uploadChainExcelCheck(List<Map<String, Object>> list, String fileFullName) throws Exception {
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, List<String>> dataMap = metadataApi.scPointAdminareaDataMap();
		Map<String, Integer> chainStatusMap = null;
		List<String> districtList = null;
		List<String> kindCodeList = null;
		Connection metaConn = null;
		Connection dealershipConn = null;
		String fileName = fileFullName.substring(fileFullName.lastIndexOf("fullChainExcel/") + 15);
		try{
			metaConn = DBConnector.getInstance().getMetaConnection();
			dealershipConn = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			String districtSql = "SELECT DISTRICT FROM SC_POINT_ADMINAREA WHERE REMARK = '1'";
			String kindCodeSql = "SELECT KIND_CODE FROM SC_POINT_POICODE_NEW";
			String chainStatusSql = "SELECT CHAIN_CODE, CHAIN_STATUS FROM IX_DEALERSHIP_CHAIN";
			districtList = run.query(metaConn, districtSql, new ResultSetHandler<List<String>>(){
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> list = new ArrayList<>();
					while(rs.next()){
						list.add(rs.getString("DISTRICT"));
					}
					return list;
				}
			});
			kindCodeList = run.query(metaConn, kindCodeSql, new ResultSetHandler<List<String>>(){
				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> list = new ArrayList<>();
					while(rs.next()){
						list.add(rs.getString("KIND_CODE"));
					}
					return list;
				}
			});
			chainStatusMap = run.query(dealershipConn, chainStatusSql, new ResultSetHandler<Map<String, Integer>>(){
				@Override
				public Map<String, Integer> handle(ResultSet rs) throws SQLException {
					Map<String, Integer> map = new HashMap<>();
					while(rs.next()){
						map.put(rs.getString("CHAIN_CODE"), rs.getInt("CHAIN_STATUS"));
					}
					return map;
				}
			});
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(metaConn);
			DbUtils.rollbackAndCloseQuietly(dealershipConn);
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(metaConn);
			DbUtils.closeQuietly(dealershipConn);
		}
		
		for (int i = 0; i < list.size(); i++) {
			String province = list.get(i).get("province").toString();
			String city = list.get(i).get("city").toString();
			String project = list.get(i).get("project").toString();
			String kindCode = list.get(i).get("kindCode").toString();
			String chain = list.get(i).get("chain").toString();
			String name = list.get(i).get("name").toString();
			String address = list.get(i).get("address").toString();
			if(StringUtils.isEmpty(province) || !dataMap.get("province").contains(province)){
				throw new ServiceException(fileName + "文件中：第" + (i+2) + "行中：省份为空或在SC_POINT_ADMINAREA表PROVINCE中不存在");
			}
			if(!(!StringUtils.isEmpty(city) && (dataMap.get("city").contains(city) || districtList.contains(city)))){
				throw new ServiceException(fileName + "文件中：第" + (i+2) + "行中：城市为空或在SC_POINT_ADMINAREA表PROVINCE和字段REMARK为1的DISTRICT中不存在");
			}
			if(StringUtils.isEmpty(project)){
				throw new ServiceException(fileName + "文件中：第" + (i+2) + "行中：项目为空");
			}
			if(StringUtils.isEmpty(kindCode) || !kindCodeList.contains(kindCode)){
				throw new ServiceException(fileName + "文件中：第" + (i+2) + "行中：代理店分类为空或不在表SC_POINT_POICODE_NEW中对应的KIND_CODE的值域内");
			}
			if(StringUtils.isEmpty(chain) || chainStatusMap.get(chain) != 0){
				throw new ServiceException(fileName + "文件中：代理店品牌为空或代理店品牌表中状态不是未开启");
			}
			if(StringUtils.isEmpty(name) || !com.navinfo.dataservice.commons.util.ExcelReader.h2f(name).equals(name)){
				throw new ServiceException(fileName + "文件中：第" + (i+2) +"行中：厂商提供名称为空或不是全角");
			}
			if(StringUtils.isEmpty(address) || !com.navinfo.dataservice.commons.util.ExcelReader.h2f(address).equals(address)){
				throw new ServiceException(fileName + "文件中：第" + (i+2) + "行中：厂商提供地址为空或不是全角");
			}
		}
	}
	
	/**
	 * @param conn
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Integer> getChainStatus(Connection conn) throws ServiceException {
		try{
			QueryRunner run = new QueryRunner();
			String sql= "select * from IX_DEALERSHIP_CHAIN";

			ResultSetHandler<Map<String,Integer>> rsHandler = new ResultSetHandler<Map<String,Integer>>() {
				public Map<String,Integer> handle(ResultSet rs) throws SQLException {
					Map<String,Integer> result = new HashMap<String,Integer>();
					while (rs.next()) {
						result.put(rs.getString("CHAIN_CODE"), rs.getInt("CHAIN_STATUS"));
					}
					return result;
				}	
			};
			log.info("getChainStatus sql:" + sql);
			return run.query(conn, sql,rsHandler);			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		}
	}

//	public static void getDirectory(File file, List<String> list){
//		File flist[] = file.listFiles();
//		if (flist == null || flist.length == 0) {
//		    return;
//		}
//		for (File f : flist) {
//		    if (f.isDirectory()) {
//		        for(File fileInner:f.listFiles()){
//		        	if(fileInner.getAbsoluteFile().toString().contains(".xlsx")||fileInner.getAbsoluteFile().toString().contains(".xls")){
//		        		list.add(fileInner.getAbsolutePath());
//		        		break;
//		        	}
//		        }
//		        getDirectory(f,list);
//		    } else {
//		    	if(f.getAbsoluteFile().toString().contains(".xlsx")||f.getAbsoluteFile().toString().contains(".xls")){
//	        		list.add(f.getAbsolutePath());
//	        	}
//		    }
//		}
//	}
	
	public static void getDirectory(File file, List<String> list){
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
			return;
		}
		for (File f : flist) {
			if (f.isDirectory()) {
				getDirectory(f,list);
			} else {
				String suffix = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1);
				if("xlsx".equals(suffix) || "xls".equals(suffix)){
					list.add(f.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * @param conn 
	 * @param chainStatus 
	 * @param ixDealershipChain
	 * @throws ServiceException 
	 */
	private void updateIxDealershipChain(Connection conn, String chainCode,Integer workStatus,Integer workType, int chainStatus) throws ServiceException {
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String updateSql = "update IX_DEALERSHIP_CHAIN C SET C.WORK_STATUS = " + workStatus + ",C.WORK_TYPE = " + workType + ",C.CHAIN_STATUS = " + chainStatus + " WHERE C.CHAIN_CODE = '" + chainCode + "'";			
			log.info("updateIxDealershipChain sql:" + updateSql);
			run.update(conn, updateSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}
	}

	/**
	 * 一览表上传excel导入
	 * @param upFile
	 * @return List<Map<String, Object>>
	 * @throws Exception 
	 */
	private List<Map<String, Object>> impIxDealershipResultExcel(String upFile) throws Exception {
		log.info("start 导入一览表上传excel："+upFile);
		ExcelReader excleReader = new ExcelReader(upFile);
		Map<String,String> excelHeader = new HashMap<String,String>();
		
		excelHeader.put("省份", "province");
		excelHeader.put("城市", "city");
		excelHeader.put("项目", "project");
		excelHeader.put("代理店分类", "kindCode");
		excelHeader.put("代理店品牌", "chain");
		excelHeader.put("厂商提供名称", "name");
		excelHeader.put("厂商提供简称", "nameShort");
		excelHeader.put("厂商提供地址", "address");
		excelHeader.put("厂商提供电话（销售）", "telSale");
		excelHeader.put("厂商提供电话（维修）", "telService");
		excelHeader.put("厂商提供电话（其他）", "telOther");		
		excelHeader.put("厂商提供邮编", "postCode");
		excelHeader.put("厂商提供英文名称", "nameEng");
		excelHeader.put("厂商提供英文地址", "addressEng");
		
		excelHeader.put("一览表提供时间", "provideDate");
		
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		log.info("end 导入一览表结果excel："+upFile);
		return sources;

	}
	
	
	/**
	 * 客户/外业确认列表
	 * @param dataJson
	 * @return 分页后的结果List
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public List<Map<String, Object>> cofirmDataList(JSONObject dataJson) throws Exception{
		//处理数据
		Map<String, Object> cofirmData = convertCofirmData(dataJson);
		Connection con = null;
		try{
			con = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			//数据状态
			String cfmStatus = String.valueOf(cofirmData.get("cfmStatus"));
			//列表类型
			int workflowStatus = 0;
			if("1".equals(cofirmData.get("type").toString())){
				workflowStatus = 5;
			}
			if("2".equals(cofirmData.get("type").toString())){
				workflowStatus = 4;
			}
			
			//分页信息
			int begainSize = Integer.parseInt(String.valueOf(cofirmData.get("begainSize")));
			int endSize = Integer.parseInt(String.valueOf(cofirmData.get("endSize")));
			
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT * FROM (SELECT A.*, ROWNUM RN FROM (");
			sb.append("select r.poi_num_1, r.poi_num_2, r.poi_num_3, r.poi_num_4, r.poi_num_5,r.result_id, r.name,r.address,r.kind_code,r.province,r.city,r.to_info_date,r.cfm_memo,r.fb_date,r.fb_content,r.fb_audit_remark,r.to_client_date from IX_DEALERSHIP_RESULT r where ");
			
			if("3".equals(cfmStatus)){
				if(workflowStatus == 4){
					sb.append("r.fb_source = 1 and r.cfm_status = 3");
				}else if(workflowStatus == 5){
					sb.append("r.fb_source = 2 and r.cfm_status = 3");
				}else{
					throw new Exception("已反馈类型的数据type请求参数错误：type应该为1或2");
				}
			}else{
				sb.append("r.workflow_status = "+workflowStatus+" and r.cfm_status = "+cfmStatus);
			}
			
			if(cofirmData.containsKey("chainCode") && cofirmData.get("chainCode") != null){
				sb.append(" and r.chain = '" + String.valueOf(cofirmData.get("chainCode")) + "'");
			}
			if(cofirmData.containsKey("name") && cofirmData.get("name") != null){
				sb.append(" and r.name like '%" + String.valueOf(cofirmData.get("name")) + "%'");
			}
			if(cofirmData.containsKey("address") && cofirmData.get("address") != null){
				sb.append(" and r.address like '%" + String.valueOf(cofirmData.get("address")) + "%'");
			}
			sb.append(")A WHERE ROWNUM <= "+endSize+")WHERE RN >= "+begainSize);
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> cofirmDataList = new ArrayList();
					while (rs.next()) {
						Map<String, Object> resultMap = new HashMap<>();
						resultMap.put("resultId", rs.getInt("result_id"));
						resultMap.put("name", rs.getString("name"));
						resultMap.put("address", rs.getString("address"));
						resultMap.put("kindCode", rs.getString("kind_code"));
						resultMap.put("province", rs.getString("province"));
						resultMap.put("city", rs.getString("city"));
						resultMap.put("toInfoDate ", rs.getString("to_info_date"));
						resultMap.put("cfmMemo", rs.getString("cfm_memo"));
						resultMap.put("fbDate", rs.getString("fb_date"));
						resultMap.put("fbContent", rs.getString("fb_content"));
						resultMap.put("fbAuditRemark", rs.getString("fb_audit_remark"));
						int poiNum = calculatePoiNum(rs);
						resultMap.put("matchPoiNum", poiNum);
						resultMap.put("toClientDate", rs.getString("to_client_date"));
						cofirmDataList.add(resultMap);
					}
					return cofirmDataList;
				}
			};
			log.info("客户/外业确认列表查询sql:"+sb.toString());
			return run.query(con, sb.toString(), rs);
		}catch(Exception e){
			log.error(e);
			throw e;
		}finally{
			DbUtils.commitAndClose(con);
		}
	}
	
	/**
	 * 处理客户/外业确认列表上传的数据
	 * @param dataJson
	 * @return 处理后的数据map
	 * @author songhe
	 * 
	 * */
	public synchronized Map<String, Object> convertCofirmData(JSONObject dataJson){
		
		Map<String, Object> cofirmDataMap =  new HashMap<>();
		//默认的页码和每页数据设置为1，20
		int pageSize = 1;
		if(dataJson.containsKey("pageSize")){
			pageSize = dataJson.getInt("pageSize");
		}
		int pageNum = 20;
		if(dataJson.containsKey("pageNum")){
			pageNum = dataJson.getInt("pageNum");
		}
		//处理分页查询的结束和开始位
		int begainSize = (pageSize-1) * pageNum+1;
		int endSize = pageSize * pageNum;
		cofirmDataMap.put("begainSize", begainSize);
		cofirmDataMap.put("endSize", endSize);
		
		int type = dataJson.getInt("type");
		int cfmStatus = dataJson.getInt("cfmStatus");
		cofirmDataMap.put("type", type);
		cofirmDataMap.put("cfmStatus", cfmStatus);
		
		if(dataJson.containsKey("chainCode") && dataJson.get("chainCode") != null && StringUtils.isNotBlank(dataJson.get("chainCode").toString())){
			cofirmDataMap.put("chainCode", dataJson.getString("chainCode"));
		}
		if(dataJson.containsKey("name") && dataJson.get("name") != null && StringUtils.isNotBlank(dataJson.get("name").toString())){
			cofirmDataMap.put("name", dataJson.getString("name"));
		}
		if(dataJson.containsKey("address") && dataJson.get("address") != null && StringUtils.isNotBlank(dataJson.get("address").toString())){
			cofirmDataMap.put("address", dataJson.getString("address"));
		}
		log.info("要查询确认列表属性为："+type+",确认状态为："+cfmStatus+"的数据");
		return cofirmDataMap;
	}
	
	
	/**
	 * 计算推荐POI总数方法
	 * @param ResultHandler
	 * @return 计算的推荐POI总数
	 * */
	public synchronized int calculatePoiNum(ResultSet rs){
		int poiNum = 0;
		try {
			if(rs.getString("poi_num_1") != null && "" != rs.getString("poi_num_1")){
				poiNum = poiNum + 1;
			}
			if(rs.getString("poi_num_2") != null && "" != rs.getString("poi_num_2")){
				poiNum = poiNum + 1;
			}
			if(rs.getString("poi_num_3") != null && "" != rs.getString("poi_num_3")){
				poiNum = poiNum + 1;
			}
			if(rs.getString("poi_num_4") != null && "" != rs.getString("poi_num_4")){
				poiNum = poiNum + 1;
			}
			if(rs.getString("poi_num_5") != null && "" != rs.getString("poi_num_5")){
				poiNum = poiNum + 1;
			}
		} catch (Exception e) {
			log.error("计算推荐POI数量出错，原因为：",e);
			return 0;
		}
		return poiNum;
	}
	
	/**
	 * 重启品牌
	 * 品牌状态改为“未开启”即chain_status=0，
	 * 品牌作业类型CHAIN.work_type改为“无”即0，
	 * 品牌作业状态CHAIN.work_status改为“无”即0。
	 * @param chainCode
	 * @throws Exception 
	 * 
	 * */
	public void openChain(String chainCode) throws Exception{
		Connection con = null;
		try{
			
			con = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			
			String updateSql = "update IX_DEALERSHIP_CHAIN C SET C.WORK_STATUS = 0, C.WORK_TYPE = 0, C.CHAIN_STATUS = 0 WHERE C.CHAIN_CODE = '" + chainCode + "'";			
			log.info("openChain sql:" + updateSql);
			run.execute(con, updateSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			DbUtils.rollback(con);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndClose(con);
		}
	}
	
	
	/**
	 * 得到客户确认-待发布中品牌数据
	 * @param chainCode
	 * @return
	 * @throws SQLException
	 */
	public List<ExpClientConfirmResult> expClientConfirmResultList(String chainCode) throws Exception{
		
		Connection conn = null;
	
		//获取代理店数据库连接
		conn=DBConnector.getInstance().getDealershipConnection();
		try{
			List<ExpClientConfirmResult> ClientConfirmResultList = null;
			if(StringUtils.isNotBlank(chainCode)){
				ClientConfirmResultList = IxDealershipResultSelector.getClientConfirmResultList(chainCode,conn);
			}
			if(ClientConfirmResultList!=null&&!ClientConfirmResultList.isEmpty()){
				for (ExpClientConfirmResult result : ClientConfirmResultList) {
					Connection regionConn = null;
					Connection mancon = null;
					try {
						int regionId = getRegionId(result.getResultId(), conn);
						mancon = DBConnector.getInstance().getManConnection();
						int dbId = getDailyDbId(regionId, mancon);
						regionConn = DBConnector.getInstance().getConnectionById(dbId);
						int pid = IxDealershipResultSelector.setRegionFiledByPoiNum(result,regionConn);//根据poiNum赋值日库中对应POI相关的字段
						if(pid!=0){
							IxDealershipResultSelector.setPoiStandrandNameByPid(result,regionConn);
							IxDealershipResultSelector.setPoiAliasNameByPid(result,regionConn);
							IxDealershipResultSelector.setPoiAddressByPid(result,regionConn);
							IxDealershipResultSelector.setPoiContactByPid(result,regionConn);
						}
						IxDealershipResultSelector.updateResultCfmStatus(result.getResultId(),2,conn);//将导出对应的记录的RESULT.cfm_status状态改为“待确认”即2
						IxDealershipResultSelector.updateResultToClientDate(result.getResultId(),conn);//更新TO_CLIENT_DATE为当前时间
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					} finally{
						DbUtils.commitAndCloseQuietly(mancon);
						DbUtils.commitAndCloseQuietly(regionConn);
					}
				}
				
			}
			
			
			return ClientConfirmResultList;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
			
	}
	
	/**
	 * 获取reginID
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getRegionId(int resultId, Connection conn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.region_id from IX_DEALERSHIP_RESULT t where t.RESULT_ID ="+resultId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
				
					if (rs.next()) {
						int regionId = rs.getInt("region_id");
							return regionId;
					}
					return -1;
				}
			};
			
			return run.query(conn, sql, rs);
		}catch(Exception e){
			throw e;

		}
	}

	
	/**
	 * 作业成果导出导出
	 * @param chainCode
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Object> exportWorkResulttList(JSONArray chains) throws Exception{
		Connection conn = null;
		try{
			//获取代理店库连接
			conn = DBConnector.getInstance().getDealershipConnection();
			Map<String, Object> resultMap = new HashMap(550);
			//处理excel表格内的第一行表头和excel文件名
			editExcelforWorkResult(resultMap);
			List<exportWorkResultEntity> exportWorkResultList = new ArrayList(500);
			String chainCode = "";
			for(int i = 0; i < chains.size(); i++){
				chainCode += "'"+chains.get(i).toString()+"',";
			}
			chainCode = chainCode.substring(0, chainCode.length()-1);
			if(StringUtils.isNotBlank(chainCode)){
				exportWorkResultList = exportWorkResultList(chainCode,conn);
			}
			resultMap.put("exportWorkResultList", exportWorkResultList);
			
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	
	}
	/**
	 * @param chainCode
	 * @return
	 * @throws Exception 
	 */
	public List<ExpDbDiffResult> searchDbDiff(String chainCode) throws Exception {
		Connection conn = null;
		try{
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			
			final Map<Integer,Set<String>> regionIdPidSetMap = new HashMap<Integer,Set<String>>();
			QueryRunner run = new QueryRunner();
			StringBuilder sb = new StringBuilder();
			sb.append(" SELECT R.RESULT_ID,                                     ");
			sb.append("        R.PROVINCE,                                      ");
			sb.append("        R.CITY,                                          ");
			sb.append("        R.PROJECT,                                       ");
			sb.append("        R.KIND_CODE,                                     ");
			sb.append("        R.CHAIN,                                         ");
			sb.append("        R.NAME,                                          ");
			sb.append("        R.NAME_SHORT,                                    ");
			sb.append("        R.ADDRESS,                                       ");
			sb.append("        R.TEL_SALE,                                      ");
			sb.append("        R.TEL_SERVICE,                                   ");
			sb.append("        R.TEL_OTHER,                                     ");
			sb.append("        R.POST_CODE,                                     ");
			sb.append("        R.NAME_ENG,                                      ");
			sb.append("        R.ADDRESS_ENG,                                   ");
			sb.append("        R.DEAL_SRC_DIFF,                                 ");
			sb.append("        R.WORKFLOW_STATUS,                               ");
			sb.append("        R.MATCH_METHOD,                                  ");
			sb.append("        R.POI_NUM_1,                                     ");
			sb.append("        R.POI_NUM_2,                                     ");
			sb.append("        R.POI_NUM_3,                                     ");
			sb.append("        R.POI_NUM_4,                                     ");
			sb.append("        R.POI_NUM_5,                                     ");
			//=====================================================================
			//代理店 - 表库差分结果导出原则变更(6882)
			sb.append("        R.CFM_POI_NUM,                                   ");
			sb.append("        R.CFM_IS_ADOPTED,                                ");
			//=====================================================================
			sb.append("        R.SIMILARITY,                                    ");
			sb.append("        R.DEAL_CFM_DATE,                                 ");
			sb.append("        R.REGION_ID,                                     ");
			sb.append("        S.SOURCE_ID OLD_SOURCE_ID,                       ");
			sb.append("        S.PROVINCE OLD_PROVINCE,                         ");
			sb.append("        S.CITY OLD_CITY,                                 ");
			sb.append("        S.PROJECT OLD_PROJECT,                           ");
			sb.append("        S.KIND_CODE OLD_KIND_CODE,                       ");
			sb.append("        S.CHAIN OLD_CHAIN,                               ");
			sb.append("        S.NAME OLD_NAME,                                 ");
			sb.append("        S.NAME_SHORT OLD_NAME_SHORT,                     ");
			sb.append("        S.ADDRESS OLD_ADDRESS,                           ");
			sb.append("        S.TEL_SALE OLD_TEL_SALE,                         ");
			sb.append("        S.TEL_SERVICE OLD_TEL_SERVICE,                   ");
			sb.append("        S.TEL_OTHER OLD_TEL_OTHER,                       ");
			sb.append("        S.POST_CODE OLD_POST_CODE,                       ");
			sb.append("        S.NAME_ENG OLD_NAME_ENG,                         ");
			sb.append("        S.ADDRESS_ENG OLD_ADDRESS_ENG                    ");
//			sb.append("   FROM IX_DEALERSHIP_RESULT R, IX_DEALERSHIP_SOURCE S   ");
//			sb.append("  WHERE R.SOURCE_ID = S.SOURCE_ID                        ");
//			sb.append("    AND R.CHAIN = '" + chainCode + "'");
			//代码修改：数据导出，导出数据量同界面显示数据量不一致（7102）
			sb.append("   FROM IX_DEALERSHIP_RESULT R LEFT JOIN IX_DEALERSHIP_SOURCE S   ");
			sb.append("  ON R.SOURCE_ID = S.SOURCE_ID                        ");
			sb.append("    WHERE R.CHAIN = '" + chainCode + "'");
			
			log.info("searchDbDiff sql: "+sb.toString());
			ResultSetHandler<List<ExpDbDiffResult>> rs = new ResultSetHandler<List<ExpDbDiffResult>>() {
				@Override
				public List<ExpDbDiffResult> handle(ResultSet rs) throws SQLException {
					
					List<ExpDbDiffResult> diffList = new ArrayList<ExpDbDiffResult>();
					while (rs.next()) {
						ExpDbDiffResult result = new ExpDbDiffResult();
							result.setResultId(rs.getString("result_id"));
							result.setProvince( rs.getString("province"));
							result.setCity( rs.getString("city"));
							result.setProject( rs.getString("project"));
							result.setKindCode( rs.getString("kind_code"));
							result.setChain( rs.getString("chain"));
							result.setName( rs.getString("name"));
							result.setNameShort( rs.getString("name_short"));
							result.setAddress( rs.getString("address"));
							result.setTelSale( rs.getString("tel_sale"));
							result.setTelService( rs.getString("tel_service"));
							result.setTelOther( rs.getString("tel_other"));
							result.setPostCode( rs.getString("post_code"));
							result.setNameEng( rs.getString("name_eng"));
							result.setAddressEng( rs.getString("address_eng"));
							result.setOldSourceId( rs.getString("old_source_id"));
							result.setOldProvince( rs.getString("old_province"));
							result.setOldCity( rs.getString("old_city"));
							result.setOldProject( rs.getString("old_project"));
							result.setOldKindCode( rs.getString("old_kind_code"));
							result.setOldChain( rs.getString("old_chain"));
							result.setOldName( rs.getString("old_name"));
							result.setOldNameShort( rs.getString("old_name_short"));
							result.setOldAddress( rs.getString("old_address"));
							result.setOldTelSale( rs.getString("old_tel_sale"));
							result.setOldTelService( rs.getString("old_tel_service"));
							result.setOldTelOther( rs.getString("old_tel_other"));
							result.setOldPostCode( rs.getString("old_post_code"));
							result.setOldNameEng( rs.getString("old_name_eng"));
							result.setOldAddressEng( rs.getString("old_address_eng"));

							int dealSrcDiff = rs.getInt("deal_src_diff");
							
							switch(dealSrcDiff){
							case 1:
								result.setDealSrcDiff("一致");
								break;
							case 2:
								result.setDealSrcDiff("删除");
								break;
							case 3:
								result.setDealSrcDiff("新增");
								break;
							case 4:
								result.setDealSrcDiff("更新");
								break;
							}

							int workflowStatus = rs.getInt("WORKFLOW_STATUS");
							switch(workflowStatus){
							case 0:
								result.setDbDiff("无");
								break;
							case 1:
								result.setDbDiff("差分一致无需处理");
								break;
							case 2:
								result.setDbDiff("需删除");
								break;
							case 3:
								result.setDbDiff("内业录入作业");
								break;
							case 4:
								result.setDbDiff("转外业确认");
								break;
							case 5:
								result.setDbDiff("转客户确认");
								break;
							case 6:
								result.setDbDiff("不代理");
								break;
							case 9:
								result.setDbDiff("外业处理完成，出品");
								break;
							}
							int matchMethod = rs.getInt("MATCH_METHOD");
							switch(matchMethod){
							case 0:
								result.setMatchMethod("不应用");
								break;
							case 1:
								result.setMatchMethod("ID匹配");
								break;
							case 2:
								result.setMatchMethod("推荐匹配");
								break;
							}

							Set<String> poiNum = new HashSet<String>();
							
							result.setPoi1Num( rs.getString("POI_NUM_1"));
							if(rs.getString("POI_NUM_1")!=null&&!rs.getString("POI_NUM_1").equals("")){
								poiNum.add(rs.getString("POI_NUM_1"));
							}
							
							result.setPoi2Num( rs.getString("POI_NUM_2"));
							if(rs.getString("POI_NUM_2")!=null&&!rs.getString("POI_NUM_2").equals("")){
								poiNum.add(rs.getString("POI_NUM_2"));
							}
							
							result.setPoi3Num( rs.getString("POI_NUM_3"));
							if(rs.getString("POI_NUM_3")!=null&&!rs.getString("POI_NUM_3").equals("")){
								poiNum.add(rs.getString("POI_NUM_3"));
							}

							result.setPoi4Num( rs.getString("POI_NUM_4"));
							if(rs.getString("POI_NUM_4")!=null&&!rs.getString("POI_NUM_4").equals("")){
								poiNum.add(rs.getString("POI_NUM_4"));
							}

							result.setPoi5Num( rs.getString("POI_NUM_5"));
							if(rs.getString("POI_NUM_5")!=null&&!rs.getString("POI_NUM_5").equals("")){
								poiNum.add(rs.getString("POI_NUM_5"));
							}
							
							//=============================================================================
							//代理店 - 表库差分结果导出原则变更(6882)
							result.setCfmPoiNum( rs.getString("CFM_POI_NUM"));
							if(rs.getString("CFM_POI_NUM")!=null&&!rs.getString("CFM_POI_NUM").equals("")){
								poiNum.add(rs.getString("CFM_POI_NUM"));
							}

							int cfmIsAdpoted = rs.getInt("CFM_IS_ADOPTED");
							switch(cfmIsAdpoted){
							case 0:
								result.setCfmIsAdopted("未处理");
								break;
							case 1:
								result.setCfmIsAdopted("未采纳");
								break;
							case 2:
								result.setCfmIsAdopted("已采纳");
								break;
							}
							//=============================================================================
							result.setSimilarity( rs.getString("SIMILARITY"));
							result.setDealCfmDate( rs.getString("DEAL_CFM_DATE"));
							result.setRegionId( rs.getInt("region_id"));
							if(regionIdPidSetMap.containsKey(rs.getInt("region_id"))){
								regionIdPidSetMap.get(rs.getInt("region_id")).addAll(poiNum);
							}else{
								regionIdPidSetMap.put(rs.getInt("region_id"), poiNum);
							}

						diffList.add(result);
					}
					return diffList;
				}
			};
			
			List<ExpDbDiffResult> result =  run.query(conn, sb.toString(), rs);
			//处理POI1_NUM,POI2_NUM,POI3_NUM,POI4_NUM,POI5_NUM相关值
			handleMatchedPois(result,regionIdPidSetMap);

			return result;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		return null;
	}

	
	/**
	 * @param result
	 * @param regionIdPidSetMap
	 * @throws SQLException 
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	private void handleMatchedPois(List<ExpDbDiffResult> result, Map<Integer, Set<String>> regionIdPidSetMap) throws Exception {
		Map<Integer,Map<String,IxPoiObj>> poi = new HashMap<Integer,Map<String,IxPoiObj>>();
		List<String> colValues = new ArrayList<String>();
		Map<Integer, Connection> dbConMap = queryAllRegionConn();
		Set<String> tabNames = new HashSet<String>();
		tabNames.add("IX_POI_NAME");
		tabNames.add("IX_POI_ADDRESS");
		tabNames.add("IX_POI_CONTACT");

		for(Map.Entry<Integer, Set<String>> entry:regionIdPidSetMap.entrySet()){
			if(!dbConMap.containsKey(entry.getKey())) {
				continue;
			}
			Connection regionConn = (Connection) dbConMap.get(entry.getKey());
			colValues.addAll(entry.getValue());
			Map<Long,BasicObj> objs = ObjBatchSelector.selectBySpecColumn(regionConn, "IX_POI",tabNames, false, "POI_NUM", colValues, false, false);
			Map<String,IxPoiObj> pois = new HashMap<String,IxPoiObj>();
			for(Map.Entry<Long,BasicObj> entryInner:objs.entrySet()){
				IxPoiObj ixPoiObj = (IxPoiObj)entryInner.getValue();
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				pois.put(ixPoi.getPoiNum(), ixPoiObj);
			}
			
			poi.put(entry.getKey(), pois);
		}
		
		for(ExpDbDiffResult expDbDiffResult:result){
			List<String> telList = new ArrayList<String>();
			if(expDbDiffResult.getTelOther()!=null){
				telList.addAll(Arrays.asList(StringUtils.split(expDbDiffResult.getTelOther(), "|")));
			}
			if(expDbDiffResult.getTelSale()!=null){
				telList.addAll(Arrays.asList(StringUtils.split(expDbDiffResult.getTelSale(), "|")));
			}
			if(expDbDiffResult.getTelService()!=null){
				telList.addAll(Arrays.asList(StringUtils.split(expDbDiffResult.getTelService(), "|")));
			}
			Collections.sort(telList);
			
			if(expDbDiffResult.getPoi1Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi1Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi1Num());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);

				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setPoi1Name(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setPoi1AliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setPoi1Address(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setPoi1Tel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setPoi1Diff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setPoi1PostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setPoi1KindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setPoi1Chain(poiInfo.get("poiChain"));
				}
			}
			if(expDbDiffResult.getPoi2Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi2Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi2Num());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);

				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setPoi2Name(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setPoi2AliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setPoi2Address(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setPoi2Tel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setPoi2Diff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setPoi2PostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setPoi2KindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setPoi2Chain(poiInfo.get("poiChain"));
				}

			}
			if(expDbDiffResult.getPoi3Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi3Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi3Num());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);

				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setPoi3Name(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setPoi3AliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setPoi3Address(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setPoi3Tel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setPoi3Diff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setPoi3PostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setPoi3KindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setPoi3Chain(poiInfo.get("poiChain"));
				}
			}
			if(expDbDiffResult.getPoi4Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi4Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi4Num());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);

				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setPoi4Name(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setPoi4AliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setPoi4Address(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setPoi4Tel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setPoi4Diff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setPoi4PostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setPoi4KindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setPoi4Chain(poiInfo.get("poiChain"));
				}
			}
			if(expDbDiffResult.getPoi5Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi5Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi5Num());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);

				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setPoi5Name(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setPoi5AliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setPoi5Address(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setPoi5Tel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setPoi5Diff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setPoi5PostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setPoi5KindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setPoi5Chain(poiInfo.get("poiChain"));
				}
			}
			//=================================================================================================================================
			//代理店 - 表库差分结果导出原则变更(6882)
			if(expDbDiffResult.getCfmPoiNum()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getCfmPoiNum())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getCfmPoiNum());
				Map<String,String> poiInfo = getPoiInfo(expDbDiffResult,ixPoiObj,telList);
				
				if(poiInfo.containsKey("poiName")){
					expDbDiffResult.setCfmPoiNumName(poiInfo.get("poiName"));
				}
				if(poiInfo.containsKey("poiAliasName")){
					expDbDiffResult.setCfmPoiNumAliasName(poiInfo.get("poiAliasName"));
				}
				if(poiInfo.containsKey("poiAddress")){
					expDbDiffResult.setCfmPoiNumAddress(poiInfo.get("poiAddress"));
				}
				if(poiInfo.containsKey("poiTel")){
					expDbDiffResult.setCfmPoiNumTel(poiInfo.get("poiTel"));
				}
				if(poiInfo.containsKey("poiDiff")){
					expDbDiffResult.setCfmPoiNumDiff(poiInfo.get("poiDiff"));
				}
				if(poiInfo.containsKey("poiPostCode")){
					expDbDiffResult.setCfmPoiNumPostCode(poiInfo.get("poiPostCode"));
				}
				if(poiInfo.containsKey("poiKindCode")){
					expDbDiffResult.setCfmPoiNumKindCode(poiInfo.get("poiKindCode"));
				}
				if(poiInfo.containsKey("poiChain")){
					expDbDiffResult.setCfmPoiNumChain(poiInfo.get("poiChain"));
				}
			}
			//=================================================================================================================================
		}
	}

	/**
	 * @param expDbDiffResult
	 * @param ixPoiObj
	 * @param telList 
	 * @return
	 */
	private Map<String, String> getPoiInfo(ExpDbDiffResult expDbDiffResult, IxPoiObj ixPoiObj, List<String> telList) {
		IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
		Map<String, String> result = new HashMap<String,String>();
		String poiName = null;
		String poiAddress = null;
		String poiTel = null;
		String poiTelSort = null;
		String poiPostCode = ixPoi.getPostCode();
		String poiKindCode = ixPoi.getKindCode();
		String poiChain = ixPoi.getChain();
		
		result.put("poiPostCode", poiPostCode);
		result.put("poiKindCode", poiKindCode);
		result.put("poiChain", poiChain);
		
		List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
		for(IxPoiName ixPoiName:ixPoiNameList){
			if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
				poiName = ixPoiName.getName();
				result.put("poiName", poiName);
			}
			if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
				result.put("poiAliasName", ixPoiName.getName());
			}
		}
		List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
		for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
			if(ixPoiAddress.getLangCode().equals("CHI")){
				poiAddress = ixPoiAddress.getFullname();
				result.put("poiAddress", poiAddress);
			}
		}
		List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
		List<String> contacts = new ArrayList<String>();
		for(IxPoiContact ixPoiContact:ixPoiContactList){
			contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
		}
		Collections.sort(contacts);
		poiTel = StringUtils.join(contacts.toArray(),"|");
//		result.put("poiTel", poiTel);
		
//==========================================================================================		
		//增加导出poi电话多条时，考虑电话优先级问题(7170)
		Map<Integer, String> map = new TreeMap<>();
		for(IxPoiContact ixPoiContact:ixPoiContactList){
			map.put(ixPoiContact.getPriority(), ixPoiContact.getContact());
		}
		List<String> list = new ArrayList<String>();
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			list.add(entry.getValue());
		}
		poiTelSort = StringUtils.join(list.toArray(),"|");
		result.put("poiTel", poiTelSort);
//==========================================================================================		
				
		List<String> diffs = new ArrayList<String>();
		if(expDbDiffResult.getName()==null){
			if(poiName!=null){
				diffs.add("名称不同");
			}
		}else{
			if(poiName==null
					||
					(poiName!=null&&!expDbDiffResult.getName().equals(poiName))){
				diffs.add("名称不同");
			}
		}

		if(expDbDiffResult.getAddress()==null){
			if(poiAddress!=null){
				diffs.add("地址不同");
			}
		}else{
			if(poiAddress==null
					||
					(poiAddress!=null&&!expDbDiffResult.getAddress().equals(poiAddress))){
				diffs.add("地址不同");
			}
		}
		
		if(poiTel==null
				||
				(poiTel!=null&&!poiTel.equals(StringUtils.join(telList.toArray(),"|")))){
			diffs.add("电话不同");
		}
		
		if(expDbDiffResult.getPostCode()==null){
			if(poiPostCode!=null){
				diffs.add("邮编不同");
			}
		}else{
			if(poiPostCode==null
					||
					(poiPostCode!=null&&!expDbDiffResult.getPostCode().equals(poiPostCode))){
				diffs.add("邮编不同");
			}
		}

		if(expDbDiffResult.getKindCode()==null){
			if(poiKindCode!=null){
				diffs.add("项目不同");
			}
		}else{
			if(poiKindCode==null
					||
					(poiKindCode!=null&&!expDbDiffResult.getKindCode().equals(poiKindCode))){
				diffs.add("项目不同");
			}
		}
		
		
		if(!diffs.contains("项目不同")){
			if(expDbDiffResult.getChain()==null){
				if(poiChain!=null){
					diffs.add("项目不同");
				}
			}else{
				if(poiChain==null
						||
						(poiChain!=null&&!expDbDiffResult.getChain().equals(poiChain))){
					diffs.add("项目不同");
				}
			}
		}
		
		result.put("poiDiff", StringUtils.join(diffs,"|"));
		return result;
	}

	public Map<Integer, Connection> queryAllRegionConn() throws Exception {
		Map MapConn = new HashMap();
		try {
			String sql = "select t.daily_db_id,region_id from region t";

			PreparedStatement pstmt = null;
			ResultSet rs = null;
			Connection conn = null;
			try {
				conn = DBConnector.getInstance().getManConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Connection regionConn = DBConnector.getInstance().getConnectionById(rs.getInt("daily_db_id"));
					MapConn.put(rs.getInt("region_id"), regionConn);
				}
				return MapConn;
			} finally {
				DbUtils.closeQuietly(conn, pstmt, rs);
			}
		} catch (Exception e) {
			throw new Exception("加载region失败：" + e.getMessage(), e);
		}
	}
	
	

	
	/**
	 * 作业成果导出结果的excel内容
	 * @param 
	 * @return
	 */
	public void editExcelforWorkResult(Map<String, Object> resultMap){
		String[] excelHead =  
	        { "序号","一览表ID", "IDCODE", "品牌", "分类", "CHAIN", "省/直辖市", "市", "县/区", "厂商提供名称" ,
	        		"四维录入名称", "厂商提供英文名称", "四维录入英文全称", "四维录入英文简称" , "厂商提供地址",
	        		"四维录入地址", "厂商提供英文地址","四维录入英文地址","厂商提供电话","四维录入电话","厂商提供邮编","四维录入CHAIN","四维录入邮编",
	        		"厂商提供别名","四维录入别名","四维录入别名原始英文","四维录入别名标准化英文","是否删除记录","项目","反馈人ID","负责人反馈结果","审核意见",
	        		"反馈时间","一览表确认时间","备注"};  
		
		resultMap.put("title", excelHead);
		resultMap.put("excelName", "作业成果导出列表"+DateUtils.dateToString(new Date(), "yyyyMMddHHmmss"));
	}
	
	/**
	 * 根据chainCode获取对应实体内容
	 * @param 
	 * @return
	 * @throws Exception 
	 */
	public List<exportWorkResultEntity> exportWorkResultList(String chainCode, Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			String sql = "select distinct s.source_id,s.cfm_poi_num,c.chain_name,s.kind_code,s.chain,"
					+ "s.province,s.city,s.name,s.poi_name,s.name_eng,s.address,s.poi_address,"
					+ "s.address_eng,s.tel_sale,s.tel_service,s.tel_other,s.poi_tel,s.post_code,"
					+ "s.poi_chain,s.poi_post_code,s.poi_name_short,s.name_short,s.is_deleted,s.project,"
					+ "s.fb_content,s.fb_audit_remark,s.fb_date,s.deal_cfm_date,s.cfm_memo "
					+ "from IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_RESULT r,IX_DEALERSHIP_CHAIN c where r.chain in("+chainCode+") and s.source_id = r.source_id and r.chain = c.chain_code "
					+ " order by s.source_id";
			ResultSetHandler<List<exportWorkResultEntity>> rs = new ResultSetHandler<List<exportWorkResultEntity>>() {
				@Override
				public List<exportWorkResultEntity> handle(ResultSet rs) throws SQLException {
					List<exportWorkResultEntity> excelBodyList = new ArrayList();
					int id = 1;
					while(rs.next()){
						exportWorkResultEntity entity = new exportWorkResultEntity();
						entity.setId(id);
						entity.setSourceId(rs.getInt("source_id"));
						entity.setCfmPoiNum(rs.getString("cfm_poi_num"));
						entity.setChainName(rs.getString("chain_name"));
						entity.setKindCode(rs.getString("kind_code"));
						entity.setChain(rs.getString("chain"));
						entity.setProvince(rs.getString("province"));
						entity.setCity(rs.getString("city"));
						entity.setBlock("");
						entity.setName(rs.getString("name"));
						entity.setPoiName(rs.getString("poi_name"));
						entity.setEnglishName(rs.getString("name_eng"));
						entity.setNavEnglishName("");
						entity.setNavEnglishShortName("");
						entity.setAddress(rs.getString("address"));
						entity.setPoiAddress(rs.getString("poi_address"));
						entity.setAddressEnglish(rs.getString("address_eng"));
						entity.setNavAddressEnglish("");
						entity.setVenderTel(rs.getString("tel_sale"), rs.getString("tel_service"), rs.getString("tel_other"));
						entity.setPoiTel(rs.getString("tel_other"));
						entity.setPostCode(rs.getString("post_code"));
						entity.setPoiChain(rs.getString("poi_chain"));
						entity.setPoiPostCode(rs.getString("poi_post_code"));
						entity.setPoiNameShort(rs.getString("poi_name_short"));
						entity.setNavStantardEngName("");
						entity.setNavOtherEngName("");
						entity.setShortName(rs.getString("name_short"));
						entity.setIsDeleted(rs.getInt("is_deleted"));
						entity.setProject(rs.getString("project"));
						entity.setPersonId("");
						entity.setFbContnt(rs.getString("fb_content"));
						entity.setAuditRemark(rs.getString("fb_audit_remark"));
						entity.setFbDate(rs.getString("fb_date"));
						entity.setDealCfmDate(rs.getString("deal_cfm_date"));
						entity.setCfmMemo(rs.getString("cfm_memo"));
						id++;
						excelBodyList.add(entity);
					}
					return excelBodyList;
				}
			};
			return run.query(conn, sql, rs);
		}catch(Exception e){
			log.error(e);
			throw e;
		}
	}
	
	/**
	 * 获取dailyDbId
	 * @throws Exception 
	 * @author songhe
	 * 
	 * */
	public int getDailyDbId(int regionId, Connection mancon) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.daily_db_id from REGION t where t.region_id =" + regionId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int dailyDbId = rs.getInt("daily_db_id");
						return dailyDbId;
					}
					return -1;
				}
			};
			
			return run.query(mancon, sql, rs);
		}catch(Exception e){
			throw e;
		}
	}

	/**
	 * @param userId
	 * @return
	 * @throws ServiceException 
	 */
	public Map<String, Object> chainUpdate(long userId) throws ServiceException {
		Connection conn = null;
		try{
			conn=DBConnector.getInstance().getDealershipConnection();
		    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHMMss");
		    String date = df.format(new Date());
			//获取一览表品牌
		    Map<String,String> chainMap = getChainListByStatus(conn,0);
			if(chainMap.size()==0){
				throw new Exception("不存在未开启的品牌，不能做品牌更新！");
			}
			
			//获取source数据
			Map<String, List<IxDealershipSource>> dealershipSourceMap = IxDealershipSourceSelector.getAllIxDealershipSourceByChain(conn,chainMap.keySet());
			//获取省份大区信息
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			Map<String,Integer> provinceRegionIdMap = manApi.getProvinceRegionIdMap();
			//转result,更新result表
			List<String> chainList2 = new ArrayList<String>();
			for(Map.Entry<String, List<IxDealershipSource>> entry:dealershipSourceMap.entrySet()){
				List<IxDealershipSource> ixDealershipSourceList = entry.getValue();
				List<IxDealershipResult> ixDealershipResultList = createIxDealershipResultByIxDealershipSource(ixDealershipSourceList,provinceRegionIdMap,date);
				if(ixDealershipResultList!=null&&ixDealershipResultList.size()>0){
					for(IxDealershipResult bean:ixDealershipResultList){
						IxDealershipResultOperator.createIxDealershipResult(conn,bean);
					}
				}
				chainList2.add(entry.getKey());
				chainMap.remove(entry.getKey());
			}
			//更新chain表
			int workType = 1;
			int workStatus = 1;
			int chain_status = 1;
			updateIxDealershipChain(conn,chainList2,workStatus,workType,chain_status);
			//启动表库差分
			long jobId = 0;
			Map<String,Object> result = new HashMap<String,Object>();
			String message = "";
			if(chainList2.size()!=0){
				JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
				JSONObject dataJson = new JSONObject();
				dataJson.put("chainCodeList", chainList2);
				dataJson.put("sourceType", 5);
				jobId=jobApi.createJob("DealershipTableAndDbDiffJob", dataJson, userId,0, "代理店库差分");
			}
			if(chainMap.size()>0){		
				message = "部分代理店品牌数据在全国一览表中不存在，无法执行品牌更新！";
			}

			if(jobId==0){
				throw new Exception("未开启的品牌数据在全国一览表中不存在，无法执行品牌更新！");
			}
			result.put("jobId", jobId);
			result.put("message", message);
			return result;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * @param conn
	 * @param chainList
	 * @param workStatus
	 * @param workType
	 * @param chainStatus
	 * @throws ServiceException 
	 */
	private void updateIxDealershipChain(Connection conn, List<String> chainList, int workStatus, int workType,
			int chainStatus) throws ServiceException {
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			
			String updateSql = "update IX_DEALERSHIP_CHAIN C SET C.WORK_STATUS = " + workStatus 
					+ ",C.WORK_TYPE = " + workType + ",C.CHAIN_STATUS = " + chainStatus 
					+ " WHERE C.CHAIN_CODE in ('" + StringUtils.join(chainList,"','") + "')";			
			log.info("updateIxDealershipChain sql:" + updateSql);
			run.update(conn, updateSql);
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}
	}

	private static List<IxDealershipResult> createIxDealershipResultByIxDealershipSource(List<IxDealershipSource> ixDealershipSourceList, Map<String, Integer> provinceRegionIdMap, String date) {
		List<IxDealershipResult> ixDealershipResultList = new ArrayList<IxDealershipResult>();
		for(IxDealershipSource ixDealershipSource:ixDealershipSourceList){
			IxDealershipResult ixDealershipResult = assembleResultBySource(ixDealershipSource, date, provinceRegionIdMap);
			ixDealershipResultList.add(ixDealershipResult);
		}
		return ixDealershipResultList;	
	}


	/**
	 * @param conn
	 * @param status
	 * @return
	 * @throws ServiceException 
	 */
	private Map<String, String> getChainListByStatus(Connection conn, int status) throws ServiceException {
		try{
			QueryRunner run = new QueryRunner();
			String sql= "select * from IX_DEALERSHIP_CHAIN c where c.CHAIN_STATUS = " + status;

			ResultSetHandler<Map<String, String>> rsHandler = new ResultSetHandler<Map<String, String>>() {
				public Map<String, String> handle(ResultSet rs) throws SQLException {
					Map<String, String> result = new HashMap<String, String>();
					while (rs.next()) {
						result.put(rs.getString("CHAIN_CODE"),rs.getString("CHAIN_NAME"));
					}
					return result;
				}	
			};
			log.info("getChainStatus sql:" + sql);
			return run.query(conn, sql,rsHandler);			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		}
	}

	/**
	 * 实时更新
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public long liveUpdate(long userId) throws Exception {
		Map<String, List> map = getChainCodeByLiveUpdate();//获取实时更新所需的chainCodeList
		List<String> chainCodeList = map.get("chainCodeList");
		List<Integer> resultIdList = map.get("resultIdList");
		if(null == chainCodeList || chainCodeList.isEmpty()){
			throw new Exception("不存在作业完成的数据，无法更新");
		}
		
		//启动表库差分
		JobApi jobApi=(JobApi) ApplicationContextUtil.getBean("jobApi");
		JSONObject dataJson = new JSONObject();
		dataJson.put("chainCodeList", chainCodeList);
		dataJson.put("resultIdList", resultIdList);
		dataJson.put("sourceType", 4);
		dataJson.put("userId", userId);
		long jobId=jobApi.createJob("dealershipLiveUpdateJob", dataJson, userId,0, "实时更新job");
		
		return jobId;
	}

	public Map<String,List> getChainCodeByLiveUpdate() throws Exception {
		Connection conn = null;
		try{
			conn=DBConnector.getInstance().getDealershipConnection();
			//获取source数据
			Map<String, List<IxDealershipSource>> dealershipSourceMap = IxDealershipSourceSelector.getAllIxDealershipSourceByChainWorkType(conn);
			//获取省份大区信息
			ManApi manApi = (ManApi)ApplicationContextUtil.getBean("manApi");
			Map<String,Integer> provinceRegionIdMap = manApi.getProvinceRegionIdMap();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHMMss");
		    String date = df.format(new Date());
			//转result,更新result表
			List<String> chainList = new ArrayList<String>();
			List<Integer> resultIdList = new ArrayList<Integer>();
			Map<String,List> map = new HashMap<>();
			
			for(Map.Entry<String, List<IxDealershipSource>> entry:dealershipSourceMap.entrySet()){
				List<IxDealershipSource> ixDealershipSourceList = entry.getValue();
				List<IxDealershipResult> ixDealershipResultList = createResultBySourceWhenLiveUpdate(ixDealershipSourceList,provinceRegionIdMap,date,conn);
				if(ixDealershipResultList!=null&&ixDealershipResultList.size()>0){
					for(IxDealershipResult bean:ixDealershipResultList){
						int resultId = IxDealershipResultOperator.getResultBySequence(conn);
						resultIdList.add(resultId);
						bean.setResultId(resultId);
						IxDealershipResultOperator.createIxDealershipResultWithId(conn,bean);
					}
					chainList.add(entry.getKey());
				}
			}
			if(chainList!=null&&chainList.size()>0){
				//更新chain表
				int workType = 2;
				int workStatus = 1;
				int chain_status = 1;
				updateIxDealershipChain(conn,chainList,workStatus,workType,chain_status);
			}
			
			map.put("chainCodeList", chainList);
			map.put("resultIdList", resultIdList);
			
			return map;
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}

	private List<IxDealershipResult> createResultBySourceWhenLiveUpdate(List<IxDealershipSource> ixDealershipSourceList,Map<String, Integer> provinceRegionIdMap, String date,
			Connection conn) throws Exception{
	    List<IxDealershipResult> ixDealershipResultList = new ArrayList<IxDealershipResult>();
		for(IxDealershipSource ixDealershipSource:ixDealershipSourceList){
			boolean flag = searchResultIsAllCompleteBySource(ixDealershipSource.getSourceId(),conn);
			if(!flag){continue;}
			IxDealershipResult ixDealershipResult = assembleResultBySource(ixDealershipSource, date, provinceRegionIdMap);
			ixDealershipResultList.add(ixDealershipResult);
		}
		return ixDealershipResultList;	
	}

	/**
	 * 根据source_id查询代理店RESULT表已全部作业完成的数据(可能存在多条记录)，
	 * 即代理店工艺状态为“外业处理完成，出品”(RESULT.workflow_status=9)且代理店状态为“已提交”（RESULT.deal_status=3）。
	 * @param sourceId
	 * @param conn
	 * @return
	 * @throws ServiceException
	 */
	private boolean searchResultIsAllCompleteBySource(int sourceId,Connection conn) throws ServiceException {
		try{
			
			QueryRunner run = new QueryRunner();
			String sql= "select count(1) from IX_DEALERSHIP_RESULT r where (r.workflow_status <> 9 OR "
					+ "r.deal_status <> 3) and r.source_id = " + sourceId ;

			ResultSetHandler<Boolean> rsHandler = new ResultSetHandler<Boolean>() {
				public Boolean handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						if(rs.getInt(1)>0){
							return false;
						}else{
							return true;
						}
					}
					return false;
				}	
			};
			return run.query(conn, sql,rsHandler);		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询失败，原因为:" + e.getMessage(), e);
		}
	}
	
	
	public static IxDealershipResult assembleResultBySource(IxDealershipSource ixDealershipSource,String date,Map<String,Integer> provinceRegionIdMap){
		IxDealershipResult ixDealershipResult = new IxDealershipResult();
		
		ixDealershipResult.setProvince(ixDealershipSource.getProvince());
		ixDealershipResult.setCity(ixDealershipSource.getCity());
		ixDealershipResult.setProject(ixDealershipSource.getProject());
		ixDealershipResult.setKindCode(ixDealershipSource.getKindCode());
		ixDealershipResult.setChain(ixDealershipSource.getChain());
		ixDealershipResult.setName(ixDealershipSource.getName());
		ixDealershipResult.setNameShort(ixDealershipSource.getNameShort());
		ixDealershipResult.setAddress(ixDealershipSource.getAddress());
		ixDealershipResult.setTelSale(ixDealershipSource.getTelSale());
		ixDealershipResult.setTelService(ixDealershipSource.getTelService());
		ixDealershipResult.setTelOther(ixDealershipSource.getTelOther());
		ixDealershipResult.setPostCode(ixDealershipSource.getPostCode());
		ixDealershipResult.setNameEng(ixDealershipSource.getNameEng());
		ixDealershipResult.setAddressEng(ixDealershipSource.getAddressEng());

		//时间
		ixDealershipResult.setProvideDate(date);
		
		//MATCH_METHOD:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，则赋值1，否则赋值0
		ixDealershipResult.setMatchMethod(null == ixDealershipSource.getCfmPoiNum()? 0:1);
		//POI_NUM_1:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，则赋值IX_DEALERSHIP_SOURCE.cfm_poi_num
		ixDealershipResult.setPoiNum1(ixDealershipSource.getCfmPoiNum());
		//cfm_poi_num赋值IX_DEALERSHIP_SOURCE.cfm_poi_num
		ixDealershipResult.setCfmPoiNum(ixDealershipSource.getCfmPoiNum());
		//SOURCE_ID赋值IX_POIDEALERSHIP_SOURCE.source_id
		ixDealershipResult.setSourceId(ixDealershipSource.getSourceId());
		//DEAL_CFM_DATE:赋值IX_POIDEALERSHIP_SOURCE.deal_cfm_date
		ixDealershipResult.setDealCfmDate(ixDealershipSource.getDealCfmDate());
		//POI_KIND_CODE:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_KIND_CODE
		//POI_CHAIN:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_CHAIN
		//POI_NAME:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_NAME
		//POI_NAME_SHORT:IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_NAME_SHORT
		//POI_ADDRESS:X_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_ADDRESS
		//POI_TEL	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_TEL
		//POI_POST_CODE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_POST_CODE
		//POI_X_DISPLAY	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_X_DISPLAY
		//POI_Y_DISPLAY	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_Y_DISPLAY
		//POI_X_GUIDE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_X_GUIDE
		//POI_Y_GUIDE	IX_DEALERSHIP_SOURCE表中cfm_poi_num有值，赋值IX_DEALERSHIP_SOURCE.POI_Y_GUIDE
		if(ixDealershipSource.getCfmPoiNum()!=null){
			ixDealershipResult.setPoiKindCode(ixDealershipSource.getPoiKindCode());
			ixDealershipResult.setPoiChain(ixDealershipSource.getPoiChain());
			ixDealershipResult.setPoiName(ixDealershipSource.getPoiName());
			ixDealershipResult.setPoiNameShort(ixDealershipSource.getPoiNameShort());
			ixDealershipResult.setPoiAddress(ixDealershipSource.getPoiAddress());
			ixDealershipResult.setPoiTel(ixDealershipSource.getPoiTel());
			ixDealershipResult.setPoiPostCode(ixDealershipSource.getPoiPostCode());
			ixDealershipResult.setPoiXDisplay(ixDealershipSource.getPoiXDisplay());
			ixDealershipResult.setPoiYDisplay(ixDealershipSource.getPoiYDisplay());
			ixDealershipResult.setPoiXGuide(ixDealershipSource.getPoiXGuide());
			ixDealershipResult.setPoiYGuide(ixDealershipSource.getPoiYGuide());
		}
		//GEOMETRY	赋值IX_DEALERSHIP_SOURCE.GEOMETRY
		ixDealershipResult.setGeometry(ixDealershipSource.getGeometry());
		//REGION_ID	根据IX_DEALERSHIP_RESULT.PROVINCE关联cp_region_province.province,查找对应的region_id赋值
		if(ixDealershipSource.getProvince()!=null&&provinceRegionIdMap.get(ixDealershipSource.getProvince())!=null){
			ixDealershipResult.setRegionId(provinceRegionIdMap.get(ixDealershipSource.getProvince()));
		}else{
			log.info("sourceId:" + ixDealershipResult.getSourceId() + "无法获取大区信息");
		}
		return ixDealershipResult;
		
	}
	
	/**
	 * 代理店：根据chainCode获取chainName
	 * @param chainCode
	 * @return
	 * @throws Exception
	 */
	public String getChainNameByChainCode(String chainCode) throws Exception {
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getDealershipConnection();
			String sql = "SELECT CHAIN_NAME FROM IX_DEALERSHIP_CHAIN WHERE CHAIN_CODE = ?"; 
			QueryRunner run = new QueryRunner();
			return run.query(conn, sql, new ResultSetHandler<String>(){
				@Override
				public String handle(ResultSet rs) throws SQLException {
					String chainName = null;
					if(rs.next()){
						return rs.getString("CHAIN_NAME");
					}
					return chainName;
				}}, chainCode);
		} catch (Exception e) {
			log.error("品牌名称失败，原因为："+e.getMessage(),e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception("品牌名称失败，原因为："+e.getMessage(),e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
}
