package com.navinfo.dataservice.dealership.job;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.diff.HandlerDealership;
import com.navinfo.dataservice.engine.editplus.diff.PoiRecommender;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

import oracle.sql.STRUCT;

/**
 * @ClassName: DealershipTableAndDbDiffJob
 * @author jicaihua
 * @date 2017年5月31日
 * @Description: 代理店表库差分job 库差分原则：
 *               表表差分完成后，即CHAIN.work_status＝1时，可以执行［启动表库差分］按钮，程序执行表库差分，
 *               即RESULT表与大区日库进行差分，则差分原则如下： A：“有PID”判断条件： 与POI匹配方式为“ID匹配（RESULT
 *               .match_method=1）”, 且RESULT.cfm_poi_num关联的POI在大区日库存在且非删除；
 *               B：“无PID”判断条件： 不满足A的，即为“无PID”判断条件； C：一览表与库相同判断条件：
 *               通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、中文别名、分类、品牌、中文地址、邮编、电话（
 *               多个电话｜分割合并对比）与RESULT表中“厂商提供名称”、“厂商提供简称”、“代理店分类”、“代理店品牌”、“厂商提供地址”
 *               、“厂商提供邮编”、“厂商提供电话（销售、维修、其它）”（多个电话｜分割合并对比，跟顺序无关）均相同，则“一览表与库相同”；
 *               D：一览表与库不相同判断条件： C中一览表字段与大区日库中只要有一个字段不同，则“一览表与库不相同”；
 *               E：日库POI属性无变化判断条件：
 *               通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、分类、品牌、中文地址、邮编、电话（多个电话｜
 *               分割合并对比）与RESULT表中已采纳POI名称、已采纳POI分类、已采纳POI品牌、已采纳POI地址、已采纳POI邮编、
 *               已采纳POI电话（电话对比顺序无关）均相同，则“日库POI属性无变化”； F：日库POI属性有变化判断条件：
 *               E对比字段只要有一个不字段不相同，则“日库POI属性有变化”； G：是否为一栏表品牌判断条件： 通过RESULT.
 *               cfm_poi_num关联的非删除POI的品牌与元数据库表SC_POINT_SPEC_KINDCODE_NEW表中type＝
 *               15的chain是否相同，若相同则为“一览表品牌”，否则为“非一览表品牌”； H：分类与品牌是否一致判断条件：
 *               通过RESULT.cfm_poi_num关联的非删除POI的分类和品牌与RESULT表中POI分类和POI品牌一致，则“
 *               分类与品牌一致”，否则“分类与品牌不一致”； （1）
 *               若表表差分结果为“旧版有新版没有需删除（RESULT.deal_src_diff=2）”且“有PID”，则RESULT.
 *               workflow_status＝2（需删除）； （2）
 *               若表表差分结果为“旧版有新版没有需删除（RESULT.deal_src_diff=2）”且“无PID”，则RESULT.
 *               workflow_status＝1（差分一致，无需处理）； （3）
 *               若表表差分结果为“新版交旧版有变更需变更（RESULT.deal_src_diff=4）”且“有PID”，且“一览表与库相同”
 *               ，则RESULT.workflow_status＝2（需删除）； （4）
 *               若表表差分结果为“新版交旧版有变更需变更（RESULT.deal_src_diff=4）”且“有PID”，且“一览表与库不相同
 *               ”，则RESULT.workflow_status＝3（内业录入作业）； （5）
 *               若表表差分结果为“新版交旧版有变更需变更（RESULT.deal_src_diff=4）”且“无PID”，
 *               通过GEOCODE和一览表与库差分推荐，不管是否存在匹配POI，均赋值RESULT.workflow_status＝3（
 *               内业录入作业）； （6）
 *               若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“有PID”，且“一览表与库相同”，
 *               则RESULT.workflow_status＝2（需删除）； （7）
 *               若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“有PID”，且“一览表与库不相同”且“
 *               日库POI属性无变化”，则RESULT.workflow_status＝2（需删除）； （8）
 *               若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“有PID”，且“一览表与库不相同”且“
 *               日库POI属性有变化”，且为“一览表品牌”，则RESULT.workflow_status＝3（内业录入作业）； （9）
 *               若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“有PID”，且“一览表与库不相同”且“
 *               日库POI属性有变化”，且为“非一览表品牌”，且“分类与品牌一致”，则RESULT.workflow_status＝3（
 *               内业录入作业）； （10）
 *               若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“有PID”，且“一览表与库不相同”且“
 *               日库POI属性有变化”，且为“非一览表品牌”，且“分类与品牌不一致”，则RESULT.workflow_status＝2（
 *               需删除）； （11） 若表表差分结果为“新旧一致（RESULT.deal_src_diff=1）”且“无PID”，
 *               通过GEOCODE和一览表与库差分推荐，不管是否存在匹配POI，则RESULT.workflow_status＝3（
 *               内业录入作业）； （12）
 *               若表表差分结果为“新版有旧版没有需新增（RESULT.deal_src_diff=3）”通过GEOCODE和一览表与库差分推荐
 *               ，不管是否存在匹配POI，则RESULT.workflow_status＝3（内业录入作业）；
 * 
 */
public class DealershipTableAndDbDiffJob extends AbstractJob {

	public DealershipTableAndDbDiffJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("static-access")
	@Override
	public void execute() throws JobException {

		log.info("dealershipTableAndDbDiffJob start...");

		Connection conn = null;
		Map<Integer, Connection> dbConMap = new HashMap<Integer, Connection>();
		try {
			DealershipTableAndDbDiffJobRequest jobRequest = (DealershipTableAndDbDiffJobRequest) this.request;
			String chainCode = jobRequest.getChainCode();

			log.info("chainCode:" + chainCode);

			// 从result表查询品牌数据
			// 表差分状态为删除deal_src_diff=2的数据
			List<IxDealershipResult> delDealResultList = new ArrayList();
			delDealResultList = (ArrayList) getResultByChain(chainCode, 2);

			// 表差分状态为更新deal_src_diff=4的数据
			List<IxDealershipResult> updateDealResultList = new ArrayList();
			updateDealResultList = (ArrayList) getResultByChain(chainCode, 4);

			// 表差分状态为无变更deal_src_diff=1的数据
			List<IxDealershipResult> nochangeDealResultList = new ArrayList();
			nochangeDealResultList = (ArrayList) getResultByChain(chainCode, 1);

			// 表差分状态为新增deal_src_diff=3的数据
			List<IxDealershipResult> addDealResultList = new ArrayList();
			addDealResultList = (ArrayList) getResultByChain(chainCode, 3);

			dbConMap = queryAllRegionConn();

			// 差分完成的结果list
			List<IxDealershipResult> diffFinishResultList = new ArrayList();

			// 对各种状态的resultList，分别处理

			log.info("开始处理deal_src_diff=2删除的数据");
			for (IxDealershipResult dealResult : delDealResultList) {
				log.info("resultId:" + dealResult.getResultId());
				if(!dbConMap.containsKey(dealResult.getRegionId())) {
					throw new JobException("resultId:"+dealResult.getResultId()+"赋值的region_id对应的大区库不存在");
				}
				Connection regionConn = (Connection) dbConMap.get(dealResult.getRegionId());
				if (hasValidPoi(dealResult, regionConn)) {
					// 有pid,需要删除
					dealResult.setWorkflowStatus(2);
				} else {
					// 无pid,无需编辑
					dealResult.setWorkflowStatus(1);
				}
				diffFinishResultList.add(dealResult);
			}

			log.info("开始处理deal_src_diff=4更新的数据");
			for (IxDealershipResult dealResult : updateDealResultList) {
				log.info("resultId:" + dealResult.getResultId());
				if(!dbConMap.containsKey(dealResult.getRegionId())) {
					throw new JobException("resultId:"+dealResult.getResultId()+"赋值的region_id对应的大区库不存在");
				}
				Connection regionConn = (Connection) dbConMap.get(dealResult.getRegionId());
				if (hasValidPoi(dealResult, regionConn)) {
					BasicObj poiObj = HandlerDealership.queryPoiByPid(dealResult, regionConn);
					// 有pid
					// 一览表与库是否相同
					if (HandlerDealership.isSameTableAndDb(dealResult, poiObj)) {
						dealResult.setWorkflowStatus(1); // 无需编辑
					} else {
						dealResult.setWorkflowStatus(3); // 需内业录入
					}
				} else {
					// 无pid
					// GEOCODING
					// 推荐
					PoiRecommender.conn = regionConn;
					PoiRecommender.recommenderPoi(dealResult);
					dealResult.setMatchMethod(2);
					dealResult.setWorkflowStatus(3); // 需内业录入
				}
				diffFinishResultList.add(dealResult);
			}

			log.info("开始处理deal_src_diff=1无变更的数据");
			for (IxDealershipResult dealResult : nochangeDealResultList) {
				log.info("resultId:" + dealResult.getResultId());
				if(!dbConMap.containsKey(dealResult.getRegionId())) {
					throw new JobException("resultId:"+dealResult.getResultId()+"赋值的region_id对应的大区库不存在");
				}
				Connection regionConn = (Connection) dbConMap.get(dealResult.getRegionId());
				if (hasValidPoi(dealResult, regionConn)) {
					BasicObj poiObj = HandlerDealership.queryPoiByPid(dealResult, regionConn);
					// 有pid
					// 一览表与库是否相同
					if (HandlerDealership.isSameTableAndDb(dealResult, poiObj)) {
						dealResult.setWorkflowStatus(1); // 无需编辑
					} else {
						// 补充实时更新逻辑
						// 母库POI属性无变更
						if (HandlerDealership.isNoChangePoiNature(dealResult, poiObj)) {
							dealResult.setWorkflowStatus(1); // 无需编辑
						} else {
							// 是否为一览表品牌
							if (HandlerDealership.isDealershipChain(poiObj)) {
								dealResult.setWorkflowStatus(3); // 需内业录入
							} else {
								// 分类与品牌是否变更
								if (HandlerDealership.isSameKindChain(dealResult, poiObj)) {
									dealResult.setWorkflowStatus(1); // 无需编辑
								} else {
									dealResult.setWorkflowStatus(3); // 需内业录入
								}
							}
						}
					}
				} else {
					// 无pid
					// GEOCODING
					// 推荐补充
					PoiRecommender.conn = regionConn;
					PoiRecommender.recommenderPoi(dealResult);
					dealResult.setMatchMethod(2);
					dealResult.setWorkflowStatus(3); // 需内业录入
				}
				diffFinishResultList.add(dealResult);
			}

			log.info("开始处理deal_src_diff=3新增的数据");
			for (IxDealershipResult dealResult : addDealResultList) {
				log.info("resultId:" + dealResult.getResultId());
				if(!dbConMap.containsKey(dealResult.getRegionId())) {
					throw new JobException("resultId:"+dealResult.getResultId()+"赋值的region_id对应的大区库不存在");
				}
				Connection regionConn = (Connection) dbConMap.get(dealResult.getRegionId());
				// GEOCODING 补充
				// 推荐补充
				PoiRecommender.conn = regionConn;
				PoiRecommender.recommenderPoi(dealResult);
				dealResult.setMatchMethod(2);
				dealResult.setWorkflowStatus(3); // 需内业录入
				diffFinishResultList.add(dealResult);
			}

			log.info("差分完成，开始更新result、source、chain表");
			HandlerDealership handler = new HandlerDealership();
			handler.updateDealershipDb(diffFinishResultList, chainCode,dbConMap);
			log.info("dealershipTableAndDbDiffJob end...");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
			for (Connection value : dbConMap.values()) {  
				DbUtils.commitAndCloseQuietly(value);}  


		}
	}

	/**
	 * 从result中查询对应品牌数据
	 * 
	 * @return List
	 * @author jicaihua
	 * @throws Exception
	 * 
	 */
	public List getResultByChain(String chainCode, int dealSrcDiff) throws SQLException {

		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getDealershipConnection();

			QueryRunner run = new QueryRunner();
			String selectSql = "select r.result_id,r.kind_code,r.name,r.name_short,r.chain,r.address,r.tel_sale,r.tel_service,r.tel_other,r.post_code,r.region_id,r.match_method,r.cfm_poi_num,r.source_id,r.GEOMETRY "
					+ "from IX_DEALERSHIP_RESULT r where r.workflow_status=0 and r.chain= '" + chainCode
					+ "' and r.deal_src_diff=" + dealSrcDiff;

			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List handle(ResultSet rs) throws SQLException {

					List resultList = new ArrayList();
					while (rs.next()) {
						IxDealershipResult dealResult = new IxDealershipResult();
						dealResult.setResultId(rs.getInt("result_id"));
						dealResult.setKindCode(rs.getString("kind_code"));
						dealResult.setName(rs.getString("name"));
						dealResult.setNameShort(rs.getString("name_short"));
						dealResult.setChain(rs.getString("chain"));
						dealResult.setAddress(rs.getString("address"));
						dealResult.setTelSale(rs.getString("tel_sale"));
						dealResult.setTelService(rs.getString("tel_service"));
						dealResult.setTelOther(rs.getString("tel_other"));
						dealResult.setPostCode(rs.getString("post_code"));
						dealResult.setRegionId(rs.getInt("region_id"));
						dealResult.setMatchMethod(rs.getInt("match_method"));
						dealResult.setCfmPoiNum(rs.getString("cfm_poi_num"));
						dealResult.setSourceId(rs.getInt("source_id"));

						try {
							dealResult.setGeometry(GeoTranslator.struct2Jts((STRUCT) rs.getObject("GEOMETRY")));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						resultList.add(dealResult);

					}
					return resultList;
				}
			};

			return run.query(conn, selectSql, rs);
		} catch (Exception e) {
			DbUtils.rollbackAndClose(conn);
		} finally {
			DbUtils.commitAndClose(conn);
		}
		return null;
	}

	public boolean hasValidPoi(IxDealershipResult dealResult, Connection conn) throws Exception {
		LogReader logRead = new LogReader(conn);
		int poiState = logRead.getObjectState(HandlerDealership.queryPidByPoiNum(dealResult.getCfmPoiNum(), conn), "IX_POI");
		if (1 == dealResult.getMatchMethod() && 2 != poiState) {
			return true;
		} else {
			return false;
		}
	}

	public int queryDbIdByregionId(int reginId) throws Exception {
		try {
			Connection conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			String sql = "select t.daily_db_id from region t where t.region_id =" + reginId;
			ResultSetHandler<Integer> rs = new ResultSetHandler<Integer>() {
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						int dbId = rs.getInt("daily_db_id");
						return dbId;
					}
					return -1;
				}
			};
			return run.query(conn, sql, rs);
		} catch (Exception e) {
			return 0;
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

	public static void main(String[] args) throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.131:1521/orcl", "FM_DEALERSHIP",
				"FM_DEALERSHIP");
		QueryRunner run = new QueryRunner();
		PoiRecommender.conn = conn;
		IxDealershipResult dealResult1 = new IxDealershipResult();
		dealResult1.setResultId(21424);
		dealResult1.setIsDeleted(1);
		dealResult1.setName("eeee");
		dealResult1.setNameShort("eerer");
		dealResult1.setChain("4007");
		dealResult1.setAddress("jdfdfdd36号");
		dealResult1.setTelSale("89903899");
		dealResult1.setTelService("89903899");
		dealResult1.setTelOther("89903899");
		dealResult1.setPostCode("0357");
		dealResult1.setMatchMethod(0);
		dealResult1.setCfmPoiNum("0751120826XJJ00009");
		dealResult1.setSourceId(26360);

		List<IxDealershipResult> diffFinishResultList = new ArrayList();
		diffFinishResultList.add(dealResult1);

		HandlerDealership job = new HandlerDealership();
//		job.updateDealershipDb(diffFinishResultList, "4007");
		// System.out.println(queryPidByPoiNum("0010061024HYX00212",con));

		// int status = new LogReader(con).getObjectState(objPid, objTable);
		// System.out.println(new Date());
		// System.out.println(status);
		// System.out.println(flag);
	}

}
