package com.navinfo.dataservice.control.dealership.service;

import java.io.File;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.aop.ThrowsAdvice;

import com.mongodb.client.result.DeleteResult;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.edit.model.IxDealershipChain;
import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.edit.model.IxDealershipSource;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.CpRegionProvince;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.control.dealership.diff.DiffService;
import com.navinfo.dataservice.control.dealership.service.excelModel.DiffTableExcel;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.dataservice.control.dealership.service.utils.InputStreamUtils;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

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
		int begainSize = (pageSize - 1) * pageNum;
		int endSize = pageSize * pageNum;
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getConnectionById(399);
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT * FROM "
					+ "(SELECT A.*, ROWNUM RN FROM "
					+ "(SELECT t.* FROM IX_DEALERSHIP_CHAIN t where t.chain_status  = " + chainStatus + ") "
							+ "A WHERE ROWNUM <= " + endSize + ")WHERE RN > " + begainSize;
			
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
						result.put("chainStauts", rs.getInt("WORK_STATUS"));
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
			con = DBConnector.getInstance().getConnectionById(399);
			QueryRunner run = new QueryRunner();
			String selectSql = "select r.result_id,s.source_id,r.city,r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status "
					+ "from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_CHAIN c "
					+ "where r.source_id = s.source_id and c.chain_code = s.source_id and r.result_id = '"+chainCode+"'";
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> diffList = new ArrayList();
					while (rs.next()) {
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
						diffList.add(result);
					}
					return diffList;
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
	 * 1.功能描述：表差分结果人工整理完毕后，上传入库
	 * 2.实现逻辑：
	 * 详见需求：一体化代理店业务需求-》表表差分结果导入
	 * 3.使用场景：
	 * 	1）代理店编辑平台-数据准备-表表差分
	 * @param chainCode
	 * @param upFile
	 * @throws Exception
	 */
	public void impTableDiff(HttpServletRequest request,String chainCode,
			Long userId)throws Exception {
		log.info("start 文件表表差分导入");
		//excel文件上传到服务器		
		//保存文件
		String filePath = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.uploadPath)+"/dealership/fullChainExcel";  //服务器部署路径 /data/resources/upload
		//String filePath ="D:/temp/dealership/fullChainExcel";
		log.info("文件由本地上传到服务器指定位置"+filePath);
		String localFile = InputStreamUtils.request2File(request, filePath);
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
				+ "   AND RESULT_ID  IN ";
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
				}
				else{sourceObj=new IxDealershipSource();}
				changeResultObj(resultObj,sourceObj);
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
					+ " where r.source_id = s.source_id "
					+ " and r.chain = '"+chainCode+"'";
			
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
							/*result.put("", rs.getString(""));
							result.put("", rs.getString(""));
							result.put("", rs.getString(""));
							result.put("", rs.getString(""));*/
						
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
			//获取代理店数据库连接
			conn=DBConnector.getInstance().getDealershipConnection();
			
			//保存文件
			String filePath = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.uploadPath)+"/dealership/fullChainExcel"; 
			String localZipFile = InputStreamUtils.request2File(request, filePath);

			//解压
			String localUnzipDir = filePath+localZipFile.substring(0,localZipFile.indexOf("."));
			ZipUtils.unzipFile(localZipFile,localUnzipDir);
			
			File file = new File(localUnzipDir);
			if (file.exists()) {
				File[] files = file.listFiles();
				for (File file2 : files) {
					if (file2.isDirectory()) {
						continue;
					} else {
						System.out.println("文件:" + file2.getAbsolutePath());
						//解析excel,读取result
						String chain = null;
						String fileName = file2.getAbsolutePath();
						List<Map<String, Object>> sourceMaps = impIxDealershipResultExcel(fileName);
						Map<Integer, IxDealershipSource> dealershipSourceMap = IxDealershipSourceSelector.getAllIxDealershipSource(conn);
						List<IxDealershipSource> dealershipSources = (List<IxDealershipSource>) dealershipSourceMap.values();
						Map<Integer, IxDealershipResult> dealershipResultsPreMap = IxDealershipResultSelector.getBySourceIds(conn, dealershipSourceMap.keySet());
						List<IxDealershipResult> dealershipResult = new ArrayList<IxDealershipResult>();
						for(Map<String, Object> map:sourceMaps){
							IxDealershipResult ixDealershipResult = new IxDealershipResult();
							InputStreamUtils.transMap2Bean(map, ixDealershipResult);
							dealershipResult.add(ixDealershipResult);
							chain = ixDealershipResult.getChain();
						}

						//执行差分
						Map<Integer,List<IxDealershipResult>> resultMap = DiffService.diff(dealershipSources, dealershipResult, chain,dealershipResultsPreMap);
						//写库
						List<IxDealershipResult> insert = resultMap.get(1);
						List<IxDealershipResult> update = resultMap.get(2);
						List<IxDealershipResult> delete = resultMap.get(3);
						for(IxDealershipResult bean:insert){
							IxDealershipResultOperator.createIxDealershipResult(conn,bean);
						}
						for(IxDealershipResult bean:update){
							IxDealershipResultOperator.updateIxDealershipResult(conn,bean,Long.valueOf(0));
						}
						for(IxDealershipResult bean:delete){
							IxDealershipResultOperator.updateIxDealershipResult(conn,bean,Long.valueOf(0));
						}
						

						updateIxDealershipChain(chain);

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
	 * @param ixDealershipChain
	 * @throws ServiceException 
	 */
	private void updateIxDealershipChain(String chainName) throws ServiceException {
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn=DBConnector.getInstance().getDealershipConnection();
			
			String updateSql = "update IX_DEALERSHIP_CHAIN C SET C.WORK_STATUS = ,C.WORK_TYPE = 2 WHERE C.CHAIN_NAME = " + chainName;			


			run.update(conn, 
					updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
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
	
	
}
