package com.navinfo.dataservice.control.dealership.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.dealership.diff.DiffService;
import com.navinfo.dataservice.control.dealership.service.excelModel.DiffTableExcel;
import com.navinfo.dataservice.control.dealership.service.excelModel.exportWorkResultEntity;
import com.navinfo.dataservice.control.dealership.service.model.ExpClientConfirmResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpDbDiffResult;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
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
	private Logger log = LoggerRepos.getLogger(DataPrepareService.class);

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
	 * 差分结果列表
	 * @param chainCode品牌代码
	 * @return 
	 * @author songhe
	 * 
	 * */
	public List<Map<String, Object>> loadDiffList(String chainCode) throws SQLException{
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getDealershipConnection();
			QueryRunner run = new QueryRunner();
			String selectSql = "select r.poi_num_1,r.poi_num_2,r.poi_num_3,r.poi_num_4,r.poi_num_5,r.result_id,s.source_id,r.city,r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status "
					+ "from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_CHAIN c "
					+ "where r.source_id = s.source_id and c.chain_code = r.chain and r.chain =  '"+chainCode+"'";
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> diffList = new ArrayList();
					while (rs.next()) {
						int poiNum = 0;
						Map<String, Object> result = new HashMap<>();
						result.put("resultId", rs.getString("result_id"));
						result.put("sourceId", rs.getString("source_id"));
						result.put("city", rs.getString("city"));
						result.put("kindCode", rs.getString("kind_code"));
						result.put("resultName", rs.getString("result_name"));
						result.put("sourceName", rs.getString("source_name"));
						result.put("workType", rs.getInt("work_type"));
						result.put("dealSrcDiff", rs.getInt("work_status"));
						result.put("workflowStatus", rs.getInt("workflow_status"));
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
						result.put("poiNum", poiNum);
						diffList.add(result);
					}
					return diffList;
				}
			};
			
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			e.printStackTrace();
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
		return null;
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
		log.info("文件已上传至"+localFile);
		//导入表表差分结果excel
		List<Map<String, Object>> sourceMaps=impDiffExcel(localFile);
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getDealershipConnection();
			//导入到oracle库中
			//excel的listmap转成list<bean>
			Set<Integer> resultIdSet=new HashSet<Integer>();
			Set<Integer> sourceIdSet=new HashSet<Integer>();
			List<DiffTableExcel> excelSet=new ArrayList<DiffTableExcel>();
			for(Map<String,Object> source:sourceMaps){
				JSONObject json = JSONObject.fromObject(source);
				DiffTableExcel diffSub=(DiffTableExcel) JSONObject.toBean(json, DiffTableExcel.class);
				excelSet.add(diffSub);
				resultIdSet.add(diffSub.getResultId());
				sourceIdSet.add(diffSub.getOldSourceId());
				//导入时，判断导入文件中“代理店品牌”是否跟作业品牌一致，如果一致，则可以导入，否则不可以导入
				if(!chainCode.equals(diffSub.getChain())){
					log.info("导入文件中“代理店品牌”是否跟作业品牌不一致");
					throw new Exception("导入文件中“代理店品牌”是否跟作业品牌不一致");
			}
				if(diffSub.getDealSrcDiff()!=1&&diffSub.getDealSrcDiff()!=2
						&&diffSub.getDealSrcDiff()!=3&&diffSub.getDealSrcDiff()!=4){
					log.info("表表差分结果中“新旧一览表差分结果”，值域必须在｛1，2，3，4｝范围内");
					throw new Exception("表表差分结果中“新旧一览表差分结果”，值域必须在｛1，2，3，4｝范围内");
				}
			}
			//加载IX_DEALERSHIP_RESULT中的数据
			Map<Integer, IxDealershipResult> resultObjSet = IxDealershipResultSelector.getByResultIds(conn, resultIdSet);
			//Map<Integer, IxDealershipResult> sourceObjSet = IxDealershipResultSelector.getBySourceIds(conn, sourceIdSet);
			Map<Integer, IxDealershipSource> sourceObjSet = IxDealershipSourceSelector.getBySourceIds(conn, sourceIdSet);
			//根据导入原则，获取需要修改的数据
			Map<String,Set<IxDealershipResult>> changeMap=importMain(excelSet,resultObjSet,sourceObjSet);
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
			List<DiffTableExcel> excelSet, Map<Integer, IxDealershipResult> resultObjSet, Map<Integer, IxDealershipSource> sourceObjSet) throws Exception {
		log.info("start 表表差分修改result表记录");
		Map<String, Set<IxDealershipResult>> resultMap=new HashMap<String, Set<IxDealershipResult>>();
		resultMap.put("ADD", new HashSet<IxDealershipResult>());
		resultMap.put("UPDATE", new HashSet<IxDealershipResult>());
		
		ManApi api=(ManApi) ApplicationContextUtil.getBean("manApi");
		List<CpRegionProvince> cpRegionList = api.listCpRegionProvince();
		Map<String, Integer> cpRegionMap=new HashMap<String, Integer>();
		for(CpRegionProvince region:cpRegionList){
			cpRegionMap.put(region.getProvince(), region.getRegionId());
		}
		
		for (DiffTableExcel diffSub:excelSet){
			int resultId=diffSub.getResultId();
			IxDealershipResult resultObj = null;
			int oldSourceId = diffSub.getOldSourceId();
			if(resultId==0&&oldSourceId==0){
				log.info("表表差分结果中存在“UUID”和“旧一览表ID”都为0或字符串，数据错误");
				throw new Exception("表表差分结果中存在“UUID”和“旧一览表ID”都为0或字符串，数据错误");
			}
			if(resultId!=0){
				if(!resultObjSet.containsKey(resultId)){
					log.info("表表差分结果中“UUID”在IX_DEALERSHIP_RESULT.RESULT_ID中不存在:uuid="+resultId);
					throw new Exception("表表差分结果中“UUID”在IX_DEALERSHIP_RESULT.RESULT_ID中不存在:uuid="+resultId);
				}
				resultObj = resultObjSet.get(resultId);
				resultMap.get("UPDATE").add(resultObj);
			}else{
				resultObj = new IxDealershipResult();
				//resultObj.setSourceId(oldSourceId);
				resultMap.get("ADD").add(resultObj);
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
			DbUtils.rollbackAndClose(conn);
		}finally{
			DbUtils.commitAndClose(conn);
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
			conn=DBConnector.getInstance().getDealershipConnection();
			
			//保存文件
			String filePath = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.uploadPath)+"/dealership/fullChainExcel"; 
//			String filePath = "D:\\data\\resources\\upload\\dealership\\fullChainExcel";
			JSONObject  returnParam= InputStreamUtils.request2File(request, filePath);
			String localZipFile=returnParam.getString("filePath");
			log.info("load file");

			//解压
			String localUnzipDir = localZipFile.substring(0,localZipFile.indexOf("."));
			ZipUtils.unzipFile(localZipFile,localUnzipDir);
			log.info("unzip file");

			//获取个品牌状态
			Map<String,Integer> chainStatus = getChainStatus(conn);
			
			File file = new File(localUnzipDir);
			if (file.exists()) {
				List<String> pathList = new ArrayList<String>();
				getDirectory(file,pathList);

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
						//获取IxDealershipSource
						Map<String, List<IxDealershipSource>> dealershipSourceMap = IxDealershipSourceSelector.getAllIxDealershipSourceByChain(conn);

						List<IxDealershipResult> dealershipResult = new ArrayList<IxDealershipResult>();
						for(Map<String, Object> map:sourceMaps){
							IxDealershipResult ixDealershipResult = new IxDealershipResult();
							InputStreamUtils.transMap2Bean(map, ixDealershipResult);
							dealershipResult.add(ixDealershipResult);
							chain = ixDealershipResult.getChain();
						}
						if(chainStatus.containsKey(chain)&&chainStatus.get(chain)==0){
							List<IxDealershipSource> dealershipSources =  dealershipSourceMap.get(chain);
							//加载已有的result
							Map<Integer, IxDealershipResult> dealershipResultsPreMap = IxDealershipResultSelector.getIxDealershipResultMapByChain(conn, chain);
							//执行差分
							Map<Integer,List<IxDealershipResult>> resultMap = DiffService.diff(dealershipSources, dealershipResult, chain,dealershipResultsPreMap);
							//写库
							List<IxDealershipResult> insert = resultMap.get(1);
							List<IxDealershipResult> update = resultMap.get(3);
							log.info("insert object");
							if(insert!=null&&insert.size()>0){
								for(IxDealershipResult bean:insert){
									IxDealershipResultOperator.createIxDealershipResult(conn,bean);
								}
							}
							log.info("update object");
							if(update!=null&&update.size()>0){
								for(IxDealershipResult bean:update){
									IxDealershipResultOperator.updateIxDealershipResult(conn,bean,userId);
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

	public static void getDirectory(File file, List<String> list){
		File flist[] = file.listFiles();
		if (flist == null || flist.length == 0) {
		    return;
		}
		for (File f : flist) {
		    if (f.isDirectory()) {
		        for(File fileInner:f.listFiles()){
		        	if(fileInner.getAbsoluteFile().toString().contains(".xlsx")||fileInner.getAbsoluteFile().toString().contains(".xls")){
		        		list.add(fileInner.getAbsolutePath());
		        		break;
		        	}
		        }
		        getDirectory(f,list);
		    } else {
		    	if(f.getAbsoluteFile().toString().contains(".xlsx")||f.getAbsoluteFile().toString().contains(".xls")){
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
		excelHeader.put("厂商提供电话（服务）", "telService");
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
	 * 
	 * */
	public List<Map<String, Object>> cofirmDataList(JSONObject dataJson) throws SQLException{
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
			sb.append("select r.poi_num_1, r.poi_num_2, r.poi_num_3, r.poi_num_4, r.poi_num_5,r.result_id, r.name,r.address,r.kind_code,r.city,r.to_info_date,r.cfm_memo,r.fb_date,r.fb_content,r.fb_audit_remark,r.to_client_date from IX_DEALERSHIP_RESULT r where r.workflow_status = ");
			sb.append(workflowStatus+" and r.cfm_status = "+cfmStatus);
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
						resultMap.put("city", rs.getString("city"));
						resultMap.put("toInfoDate ", rs.getString("to_info_date"));
						resultMap.put("cfmMemo", rs.getString("cfm_memo"));
						resultMap.put("fbDate", rs.getString("fb_date"));
						resultMap.put("fbContent", rs.getString("fb_content"));
						resultMap.put("fbAuditRemark", rs.getString("fb_audit_remark"));
						int poiNum = calculatePoiNum(rs);
						resultMap.put("matchPoiNum", poiNum);
						resultMap.put("toClientDate", rs.getString("kind_code"));
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
					try {
						int regionId = getRegionId(result.getResultId(), conn);
						regionConn = DBConnector.getInstance().getConnectionById(regionId);
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
	 * @throws SQLException 
	 */
	public List<ExpDbDiffResult> searchDbDiff(String chainCode) throws SQLException {
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
			sb.append("   FROM IX_DEALERSHIP_RESULT R, IX_DEALERSHIP_SOURCE S   ");
			sb.append("  WHERE R.SOURCE_ID = S.SOURCE_ID                        ");
			sb.append("    AND R.CHAIN = '" + chainCode + "'");
			
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
								result.setDbDiff("无需处理");
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
								result.setDbDiff("处理完成");
								break;
							}
							int matchMethod = rs.getInt("MATCH_METHOD");
							switch(matchMethod){
							case 0:
								result.setDbDiff("不应用");
								break;
							case 1:
								result.setDbDiff("ID匹配");
								break;
							case 2:
								result.setDbDiff("推荐匹配");
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
			DbUtils.rollbackAndClose(conn);
		}finally{
			DbUtils.commitAndClose(conn);
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
	private void handleMatchedPois(List<ExpDbDiffResult> result, Map<Integer, Set<String>> regionIdPidSetMap) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, SQLException {
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
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				expDbDiffResult.setPoi1KindCode(ixPoi.getKindCode());
				expDbDiffResult.setPoi1Chain(ixPoi.getChain());
				expDbDiffResult.setPoi1PostCode(ixPoi.getPostCode());
				List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
				for(IxPoiName ixPoiName:ixPoiNameList){
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
						expDbDiffResult.setPoi1Name(ixPoiName.getName());
					}
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
						expDbDiffResult.setPoi1AliasName(ixPoiName.getName());
					}
				}
				List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
				for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
					if(ixPoiAddress.getLangCode().equals("CHI")){
						expDbDiffResult.setPoi1Name(ixPoiAddress.getFullname());
					}
				}
				List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
				List<String> contacts = new ArrayList<String>();
				for(IxPoiContact ixPoiContact:ixPoiContactList){
					contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
				}
				Collections.sort(contacts);
				expDbDiffResult.setPoi1Tel(StringUtils.join(contacts.toArray(),"|"));
				
				List<String> diffs = new ArrayList<String>();
				if(expDbDiffResult.getPoi1Name()==null
						||
						(expDbDiffResult.getPoi1Name()!=null&&!expDbDiffResult.getName().equals(expDbDiffResult.getPoi1Name()))){
					diffs.add("名称不同");
				}
				if(expDbDiffResult.getPoi1Address()==null
						||
						(expDbDiffResult.getPoi1Address()!=null&&!expDbDiffResult.getAddress().equals(expDbDiffResult.getPoi1Address()))){
					diffs.add("地址不同");
				}
				//电话todo
				
				if(expDbDiffResult.getPoi1Tel()==null
						||
						(expDbDiffResult.getPoi1Tel()!=null&&!expDbDiffResult.getPoi1Tel().equals(StringUtils.join(telList.toArray(),"|")))){
					diffs.add("电话不同");
				}
				if(expDbDiffResult.getPoi1PostCode()==null
						||
						(expDbDiffResult.getPoi1PostCode()!=null&&!expDbDiffResult.getPostCode().equals(expDbDiffResult.getPoi1PostCode()))){
					diffs.add("邮编不同");
				}
				if(expDbDiffResult.getPoi1KindCode()==null
						||
						(expDbDiffResult.getPoi1KindCode()!=null&&!expDbDiffResult.getKindCode().equals(expDbDiffResult.getPoi1KindCode()))
						||
						expDbDiffResult.getPoi1Chain()==null
						||
						(expDbDiffResult.getPoi1Chain()!=null&&!expDbDiffResult.getChain().equals(expDbDiffResult.getPoi1Chain()))){
					diffs.add("项目不同");
				}
				expDbDiffResult.setPoi1Diff(StringUtils.join(diffs,"|"));
			}
			if(expDbDiffResult.getPoi2Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi2Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi2Num());
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				expDbDiffResult.setPoi2KindCode(ixPoi.getKindCode());
				expDbDiffResult.setPoi2Chain(ixPoi.getChain());
				expDbDiffResult.setPoi2PostCode(ixPoi.getPostCode());
				List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
				for(IxPoiName ixPoiName:ixPoiNameList){
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
						expDbDiffResult.setPoi2Name(ixPoiName.getName());
					}
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
						expDbDiffResult.setPoi2AliasName(ixPoiName.getName());
					}
				}
				List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
				for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
					if(ixPoiAddress.getLangCode().equals("CHI")){
						expDbDiffResult.setPoi2Name(ixPoiAddress.getFullname());
					}
				}
				List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
				List<String> contacts = new ArrayList<String>();
				for(IxPoiContact ixPoiContact:ixPoiContactList){
					contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
				}
				Collections.sort(contacts);
				expDbDiffResult.setPoi1Tel(StringUtils.join(contacts.toArray(),"|"));
				
				List<String> diffs = new ArrayList<String>();
				if(expDbDiffResult.getPoi2Name()==null
						||
						(expDbDiffResult.getPoi2Name()!=null&&!expDbDiffResult.getName().equals(expDbDiffResult.getPoi2Name()))){
					diffs.add("名称不同");
				}
				if(expDbDiffResult.getPoi2Address()==null
						||
						(expDbDiffResult.getPoi2Address()!=null&&!expDbDiffResult.getAddress().equals(expDbDiffResult.getPoi2Address()))){
					diffs.add("地址不同");
				}
				//电话todo
				if(expDbDiffResult.getPoi1Tel()==null
						||
						(expDbDiffResult.getPoi1Tel()!=null&&!expDbDiffResult.getPoi1Tel().equals(StringUtils.join(telList.toArray(),"|")))){
					diffs.add("电话不同");
				}
				if(expDbDiffResult.getPoi2PostCode()==null
						||
						(expDbDiffResult.getPoi2PostCode()!=null&&!expDbDiffResult.getPostCode().equals(expDbDiffResult.getPoi2PostCode()))){
					diffs.add("邮编不同");
				}
				if(expDbDiffResult.getPoi2KindCode()==null
						||
						(expDbDiffResult.getPoi2KindCode()!=null&&!expDbDiffResult.getKindCode().equals(expDbDiffResult.getPoi2KindCode()))
						||
						expDbDiffResult.getPoi2Chain()==null
						||
						(expDbDiffResult.getPoi2Chain()!=null&&!expDbDiffResult.getChain().equals(expDbDiffResult.getPoi2Chain()))){
					diffs.add("项目不同");
				}
				expDbDiffResult.setPoi2Diff(StringUtils.join(diffs,"|"));
			}
			if(expDbDiffResult.getPoi3Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi3Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi3Num());
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				expDbDiffResult.setPoi3KindCode(ixPoi.getKindCode());
				expDbDiffResult.setPoi3Chain(ixPoi.getChain());
				expDbDiffResult.setPoi3PostCode(ixPoi.getPostCode());
				List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
				for(IxPoiName ixPoiName:ixPoiNameList){
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
						expDbDiffResult.setPoi3Name(ixPoiName.getName());
					}
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
						expDbDiffResult.setPoi3AliasName(ixPoiName.getName());
					}
				}
				List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
				for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
					if(ixPoiAddress.getLangCode().equals("CHI")){
						expDbDiffResult.setPoi3Name(ixPoiAddress.getFullname());
					}
				}
				List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
				List<String> contacts = new ArrayList<String>();
				for(IxPoiContact ixPoiContact:ixPoiContactList){
					contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
				}
				Collections.sort(contacts);
				expDbDiffResult.setPoi1Tel(StringUtils.join(contacts.toArray(),"|"));
				
				List<String> diffs = new ArrayList<String>();
				if(expDbDiffResult.getPoi3Name()==null
						||
						(expDbDiffResult.getPoi3Name()!=null&&!expDbDiffResult.getName().equals(expDbDiffResult.getPoi3Name()))){
					diffs.add("名称不同");
				}
				if(expDbDiffResult.getPoi3Address()==null
						||
						(expDbDiffResult.getPoi3Address()!=null&&!expDbDiffResult.getAddress().equals(expDbDiffResult.getPoi3Address()))){
					diffs.add("地址不同");
				}
				//电话todo
				if(expDbDiffResult.getPoi1Tel()==null
						||
						(expDbDiffResult.getPoi1Tel()!=null&&!expDbDiffResult.getPoi1Tel().equals(StringUtils.join(telList.toArray(),"|")))){
					diffs.add("电话不同");
				}
				if(expDbDiffResult.getPoi3PostCode()==null
						||
						(expDbDiffResult.getPoi3PostCode()!=null&&!expDbDiffResult.getPostCode().equals(expDbDiffResult.getPoi3PostCode()))){
					diffs.add("邮编不同");
				}
				if(expDbDiffResult.getPoi3KindCode()==null
						||
						(expDbDiffResult.getPoi3KindCode()!=null&&!expDbDiffResult.getKindCode().equals(expDbDiffResult.getPoi3KindCode()))
						||
						expDbDiffResult.getPoi3Chain()==null
						||
						(expDbDiffResult.getPoi3Chain()!=null&&!expDbDiffResult.getChain().equals(expDbDiffResult.getPoi3Chain()))){
					diffs.add("项目不同");
				}
				expDbDiffResult.setPoi3Diff(StringUtils.join(diffs,"|"));
			}
			if(expDbDiffResult.getPoi4Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi4Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi4Num());
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				expDbDiffResult.setPoi4KindCode(ixPoi.getKindCode());
				expDbDiffResult.setPoi4Chain(ixPoi.getChain());
				expDbDiffResult.setPoi4PostCode(ixPoi.getPostCode());
				List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
				for(IxPoiName ixPoiName:ixPoiNameList){
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
						expDbDiffResult.setPoi4Name(ixPoiName.getName());
					}
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
						expDbDiffResult.setPoi4AliasName(ixPoiName.getName());
					}
				}
				List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
				for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
					if(ixPoiAddress.getLangCode().equals("CHI")){
						expDbDiffResult.setPoi4Name(ixPoiAddress.getFullname());
					}
				}
				List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
				List<String> contacts = new ArrayList<String>();
				for(IxPoiContact ixPoiContact:ixPoiContactList){
					contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
				}
				Collections.sort(contacts);
				expDbDiffResult.setPoi1Tel(StringUtils.join(contacts.toArray(),"|"));
				
				List<String> diffs = new ArrayList<String>();
				if(expDbDiffResult.getPoi4Name()==null
						||
						(expDbDiffResult.getPoi4Name()!=null&&!expDbDiffResult.getName().equals(expDbDiffResult.getPoi4Name()))){
					diffs.add("名称不同");
				}
				if(expDbDiffResult.getPoi4Address()==null
						||
						(expDbDiffResult.getPoi4Address()!=null&&!expDbDiffResult.getAddress().equals(expDbDiffResult.getPoi4Address()))){
					diffs.add("地址不同");
				}
				//电话todo
				if(expDbDiffResult.getPoi1Tel()==null
						||
						(expDbDiffResult.getPoi1Tel()!=null&&!expDbDiffResult.getPoi1Tel().equals(StringUtils.join(telList.toArray(),"|")))){
					diffs.add("电话不同");
				}
				if(expDbDiffResult.getPoi4PostCode()==null
						||
						(expDbDiffResult.getPoi4PostCode()!=null&&!expDbDiffResult.getPostCode().equals(expDbDiffResult.getPoi4PostCode()))){
					diffs.add("邮编不同");
				}
				if(expDbDiffResult.getPoi4KindCode()==null
						||
						(expDbDiffResult.getPoi4KindCode()!=null&&!expDbDiffResult.getKindCode().equals(expDbDiffResult.getPoi4KindCode()))
						||
						expDbDiffResult.getPoi4Chain()==null
						||
						(expDbDiffResult.getPoi4Chain()!=null&&!expDbDiffResult.getChain().equals(expDbDiffResult.getPoi4Chain()))){
					diffs.add("项目不同");
				}
				expDbDiffResult.setPoi4Diff(StringUtils.join(diffs,"|"));
			}
			if(expDbDiffResult.getPoi5Num()!=null&&poi.get(expDbDiffResult.getRegionId()).keySet().contains(expDbDiffResult.getPoi5Num())){
				IxPoiObj ixPoiObj = poi.get(expDbDiffResult.getRegionId()).get(expDbDiffResult.getPoi5Num());
				IxPoi ixPoi = (IxPoi)ixPoiObj.getMainrow();
				expDbDiffResult.setPoi5KindCode(ixPoi.getKindCode());
				expDbDiffResult.setPoi5Chain(ixPoi.getChain());
				expDbDiffResult.setPoi5PostCode(ixPoi.getPostCode());
				List<IxPoiName> ixPoiNameList = ixPoiObj.getIxPoiNames();
				for(IxPoiName ixPoiName:ixPoiNameList){
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==1&&ixPoiName.getNameType()==1){
						expDbDiffResult.setPoi5Name(ixPoiName.getName());
					}
					if(ixPoiName.getLangCode().equals("CHI")&&ixPoiName.getNameClass()==3){
						expDbDiffResult.setPoi5AliasName(ixPoiName.getName());
					}
				}
				List<IxPoiAddress> ixPoiAddressList = ixPoiObj.getIxPoiAddresses();
				for(IxPoiAddress ixPoiAddress:ixPoiAddressList){
					if(ixPoiAddress.getLangCode().equals("CHI")){
						expDbDiffResult.setPoi5Name(ixPoiAddress.getFullname());
					}
				}
				List<IxPoiContact> ixPoiContactList = ixPoiObj.getIxPoiContacts();
				List<String> contacts = new ArrayList<String>();
				for(IxPoiContact ixPoiContact:ixPoiContactList){
					contacts.addAll(Arrays.asList(StringUtils.split(ixPoiContact.getContact(), "|")));
				}
				Collections.sort(contacts);
				expDbDiffResult.setPoi1Tel(StringUtils.join(contacts.toArray(),"|"));
				
				List<String> diffs = new ArrayList<String>();
				if(expDbDiffResult.getPoi5Name()==null
						||
						(expDbDiffResult.getPoi5Name()!=null&&!expDbDiffResult.getName().equals(expDbDiffResult.getPoi5Name()))){
					diffs.add("名称不同");
				}
				if(expDbDiffResult.getPoi5Address()==null
						||
						(expDbDiffResult.getPoi5Address()!=null&&!expDbDiffResult.getAddress().equals(expDbDiffResult.getPoi5Address()))){
					diffs.add("地址不同");
				}
				//电话todo
				if(expDbDiffResult.getPoi1Tel()==null
						||
						(expDbDiffResult.getPoi1Tel()!=null&&!expDbDiffResult.getPoi1Tel().equals(StringUtils.join(telList.toArray(),"|")))){
					diffs.add("电话不同");
				}
				if(expDbDiffResult.getPoi5PostCode()==null
						||
						(expDbDiffResult.getPoi5PostCode()!=null&&!expDbDiffResult.getPostCode().equals(expDbDiffResult.getPoi5PostCode()))){
					diffs.add("邮编不同");
				}
				if(expDbDiffResult.getPoi5KindCode()==null
						||
						(expDbDiffResult.getPoi5KindCode()!=null&&!expDbDiffResult.getKindCode().equals(expDbDiffResult.getPoi5KindCode()))
						||
						expDbDiffResult.getPoi5Chain()==null
						||
						(expDbDiffResult.getPoi5Chain()!=null&&!expDbDiffResult.getChain().equals(expDbDiffResult.getPoi5Chain()))){
					diffs.add("项目不同");
				}
				expDbDiffResult.setPoi5Diff(StringUtils.join(diffs,"|"));
			}
		}
	}

	public Map<Integer, Connection> queryAllRegionConn() throws SQLException {
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
			throw new SQLException("加载region失败：" + e.getMessage(), e);
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
			String sql = "select rownum,s.source_id,s.cfm_poi_num,c.chain_name,s.kind_code,s.chain,"
					+ "s.province,s.city,s.name,s.poi_name,s.name_eng,s.address,s.poi_address,"
					+ "s.address_eng,s.tel_sale,s.tel_service,s.tel_other,s.poi_tel,s.post_code,"
					+ "s.poi_chain,s.poi_post_code,s.poi_name_short,s.name_short,s.is_deleted,s.project,"
					+ "s.fb_content,s.fb_audit_remark,s.fb_date,s.deal_cfm_date,s.cfm_memo "
					+ "from IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_RESULT r,IX_DEALERSHIP_CHAIN c where r.chain in("+chainCode+") and s.source_id = r.source_id and r.chain = c.chain_code";
			ResultSetHandler<List<exportWorkResultEntity>> rs = new ResultSetHandler<List<exportWorkResultEntity>>() {
				@Override
				public List<exportWorkResultEntity> handle(ResultSet rs) throws SQLException {
					List<exportWorkResultEntity> excelBodyList = new ArrayList();
					while(rs.next()){
						exportWorkResultEntity entity = new exportWorkResultEntity();
						entity.setId(rs.getInt("rownum"));
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
}
