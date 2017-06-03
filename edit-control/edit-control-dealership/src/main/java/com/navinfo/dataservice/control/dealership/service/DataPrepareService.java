package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.alibaba.druid.support.logging.Log;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.control.dealership.service.excelModel.DiffTableExcel;
import com.navinfo.dataservice.control.dealership.service.model.ExpIxDealershipResult;
import com.navinfo.navicommons.database.QueryRunner;

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
						result.put("CHAIN_CODE", rs.getString("CHAIN_CODE"));
						result.put("CHAIN_NAME", rs.getString("CHAIN_NAME"));
						result.put("CHAIN_WEIGHT", rs.getInt("CHAIN_WEIGHT"));
						result.put("CHAIN_STATUS", rs.getInt("CHAIN_STATUS"));
						result.put("WORK_TYPE", rs.getInt("WORK_TYPE"));
						result.put("WORK_STATUS", rs.getInt("WORK_STATUS"));
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
	public void impTableDiff(String chainCode,
			String upFile)throws Exception {
		//导入表表差分结果excel
		List<Map<String, Object>> sourceMaps=impDiffExcel(upFile);
		//记录检查
		//TODO
		//导入到oracle库中
		//excel的listmap转成list<bean>
		for(Map<String,Object> source:sourceMaps){
			JSONObject json = JSONObject.fromObject(source);
			DiffTableExcel diffSub=(DiffTableExcel) JSONObject.toBean(json, DiffTableExcel.class);
		}
		//加载IX_DEALERSHIP_RESULT中的数据
		
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
		
		excelHeader.put("uuid", "resultId");
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
	
	public List<Map<String, Object>> expTableDiff(String chainCode) throws SQLException{
		
		List<ExpIxDealershipResult> shipResults = searchTableDiff(chainCode);
		ExportExcel<ExpIxDealershipResult> ex = new ExportExcel<ExpIxDealershipResult>();  
		String[] headers =  
	        { "UUID", "省份", "城市", "项目", "代理店分类", "代理店品牌", "厂商提供名称", "厂商提供简称", "厂商提供地址" ,
	        		"厂商提供电话（销售）", "厂商提供电话（服务）", "厂商提供电话（其他）", "厂商提供邮编" , "厂商提供英文名称",
	        		"厂商提供英文地址", "旧一览表ID", "旧一览表省份" ,
	        		"旧一览表城市", "旧一览表项目", "旧一览表分类", "旧一览表品牌" , "旧一览表名称", "旧一览表简称", "旧一览表地址",
	        		"旧一览表电话（其他）" ,
	        		"旧一览表电话（销售）", "旧一览表电话（服务）", "旧一览表邮编", "旧一览表英文名称" , "旧一览表英文地址", 
	        		"新旧一览表差分结果"  };  
		return null;
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
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getConnectionById(399);
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
			
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
		return null;
	}
	
	
}
