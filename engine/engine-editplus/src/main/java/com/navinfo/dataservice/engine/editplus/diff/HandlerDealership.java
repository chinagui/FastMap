package com.navinfo.dataservice.engine.editplus.diff;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
import oracle.sql.STRUCT;

/**
 * 代理店与Poi属性比较类
 * 
 * @author jch
 *
 */
public class HandlerDealership {

	protected Logger log = LoggerRepos.getLogger(this.getClass());

	/**
	 * 一览表与库相同判断条件：通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、中文别名、分类、品牌、中文地址、邮编、电话（
	 * 多个电话｜分割合并对比）
	 * 与RESULT表中“厂商提供名称”、“厂商提供简称”、“代理店分类”、“代理店品牌”、“厂商提供地址”、“厂商提供邮编”、“厂商提供电话（销售、
	 * 维修、其它）”（多个电话｜分割合并对比，跟顺序无关）均相同，则“一览表与库相同”；
	 * 
	 * @param dealershipMR
	 * @param p
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	public static boolean isSameTableAndDb(IxDealershipResult dealershipMR, FastPoi obj) throws Exception {

		if (dealershipMR == null || obj == null) {
			return false;
		}

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean nameShortFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		// 判断官方标准名称是否相等
		if (dealershipMR.getName() != null && (!dealershipMR.getName().equals(obj.getName()))) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断别名是否相等
		if (dealershipMR.getNameShort() != null && (!dealershipMR.getNameShort().equals(obj.getShortName()))) {
			str.append("别名不同；");
			nameShortFlag = false;
		}
		// 判断分类是否相等
		if (dealershipMR.getKindCode() != null && (!dealershipMR.getKindCode().equals(obj.getKindCode()))) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (!dealershipMR.getChain().equals(obj.getChain())) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (dealershipMR.getAddress() != null && (!dealershipMR.getAddress().equals(obj.getAddr()))) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (dealershipMR.getPostCode() != null && (!dealershipMR.getPostCode().equals(obj.getPostCode()))) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		String telephone = StringUtil.sortPhone(StringUtil.contactFormat(obj.getTel()));
		String dealershipTel=StringUtil.sortPhone(StringUtil.contactFormat(dealershipMR.getTelephone()));
		if (!dealershipTel.equals(telephone)) {
			str.append("电话不同；");
			phoneFlag = false;
		}

		if (nameFlag && nameShortFlag && kindFlag && chainFlag && addressFlag && phoneFlag && postCodeFlag) {
			isEqual = true;
		}

		return isEqual;
	}

	/**
	 * 日库POI属性无变化判断条件：
	 * 通过RESULT.cfm_poi_num关联的非删除POI官方标准中文名称、分类、品牌、中文地址、邮编、电话（多个电话｜分割合并对比）
	 * 与RESULT表中已采纳POI名称、
	 * 已采纳POI分类、已采纳POI品牌、已采纳POI地址、已采纳POI邮编、已采纳POI电话（电话对比顺序无关）均相同，则“日库POI属性无变化”；
	 * 
	 * @param dealershipMR
	 * @param p
	 * @param poiObj
	 * @return
	 * @throws Exception
	 */
	public static boolean isNoChangePoiNature(IxDealershipResult dealershipMR, FastPoi obj) throws Exception {
		if (dealershipMR == null || obj == null) {
			return false;
		}

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		// 判断官方标准名称是否相等
		if (dealershipMR.getPoiName() != null && !("".equals(dealershipMR.getPoiName()))
				&& (!dealershipMR.getPoiName().equals(obj.getName()))) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断分类是否相等
		if (dealershipMR.getPoiKindCode() != null && !("".equals(dealershipMR.getPoiKindCode()))
				&& (!dealershipMR.getPoiKindCode().equals(obj.getKindCode()))) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (dealershipMR.getPoiChain() != null && !("".equals(dealershipMR.getPoiChain()))
				&& (!dealershipMR.getPoiChain().equals(obj.getChain()))) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (dealershipMR.getPoiAddress() != null && !("".equals(dealershipMR.getPoiAddress()))
				&& (!dealershipMR.getPoiAddress().equals(obj.getAddr()))) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (dealershipMR.getPoiPostCode() != null && !("".equals(dealershipMR.getPoiPostCode()))
				&& (!dealershipMR.getPoiPostCode().equals(obj.getPostCode()))) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		String telephone = StringUtil.sortPhone(StringUtil.contactFormat(obj.getTel()));
		String adoptPoiTel = StringUtil.sortPhone(StringUtil.contactFormat(dealershipMR.getPoiTel()));
		if (!adoptPoiTel.equals(telephone)) {
			str.append("电话不同；");
			phoneFlag = false;
		}

		if (nameFlag && kindFlag && chainFlag && addressFlag && phoneFlag && postCodeFlag) {
			isEqual = true;
		}

		return isEqual;
	}

	// 是否是一栏表品牌
	// 通过RESULT.cfm_poi_num关联的非删除POI的品牌与元数据库表SC_POINT_SPEC_KINDCODE_NEW表中type＝15的chain是否相同，若相同则为“一览表品牌”，否则为“非一览表品牌”；
	public static boolean isDealershipChain(IxDealershipResult obj, Map<String, String> mapKindChain) throws Exception {
		if (obj == null) {
			return false;
		}
		if (obj.getChain() != null && (obj.getChain().equals(mapKindChain.get(obj.getKindCode())))) {
			return true;
		}
		return false;
	}

	// 分类与品牌是否一致
	// 通过RESULT.cfm_poi_num关联的非删除POI的分类和品牌与RESULT表中POI分类和POI品牌一致，则“分类与品牌一致”，否则“分类与品牌不一致”；
	public static boolean isSameKindChain(IxDealershipResult dealershipMR, FastPoi poiObj) {
		if (poiObj == null) {
			return false;
		}
		if (poiObj.getChain() != null && (poiObj.getChain().equals(dealershipMR.getPoiChain()))
				&& poiObj.getKindCode().equals(dealershipMR.getPoiKindCode())) {
			return true;
		}
		return false;
	}

	public void updateDealershipDb(List<IxDealershipResult> diffFinishResultList, List<String> chainList, Map dbMap,
			Logger log) throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getDealershipConnection();

			// conn =
			// DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.131:1521/orcl",
			// "FM_DEALERSHIP", "FM_DEALERSHIP");

			// 更新result表
			String updateResultSql = "UPDATE ix_dealership_result r SET r.workflow_status=?,r.is_deleted=?,r.match_method=?,r.poi_num_1=?,r.poi_num_2=?,r.poi_num_3=?,r.poi_num_4=?,r.poi_num_5=?, "
					+ " r.similarity=?,r.cfm_poi_num=?,r.cfm_is_adopted=?,r.deal_cfm_date=?,r.poi_kind_code=?,r.poi_chain=?,r.poi_name=?,r.poi_name_short=?,r.poi_address=?,r.poi_tel=?,"
					+ " r.poi_post_code=?,r.poi_x_display=?,r.poi_y_display=?,r.poi_y_guide=?,r.poi_x_guide=? where r.result_id=?";

			Object[][] param = new Object[diffFinishResultList.size()][];

			for (int i = 0; i < diffFinishResultList.size(); i++) {
				IxDealershipResult dealResult = diffFinishResultList.get(i);
				updateResultObj(dealResult, dbMap);
				log.info("resultId:" + dealResult.getResultId() + ",province:" + dealResult.getProvince()+",city:"+dealResult.getCity()+",address:"+dealResult.getAddress()+",cfm_poi_num:"+dealResult.getCfmPoiNum());

				Object[] obj = new Object[] { dealResult.getWorkflowStatus(), dealResult.getIsDeleted(),
						dealResult.getMatchMethod(), dealResult.getPoiNum1(), dealResult.getPoiNum2(),
						dealResult.getPoiNum3(), dealResult.getPoiNum4(), dealResult.getPoiNum5(),
						dealResult.getSimilarity(), dealResult.getCfmPoiNum(), dealResult.getCfmIsAdopted(),
						dealResult.getDealCfmDate(), dealResult.getPoiKindCode(), dealResult.getPoiChain(),
						dealResult.getPoiName(), dealResult.getPoiNameShort(), dealResult.getPoiAddress(),
						dealResult.getPoiTel(), dealResult.getPoiPostCode(), dealResult.getPoiXDisplay(),
						dealResult.getPoiYDisplay(), dealResult.getPoiXGuide(), dealResult.getPoiYGuide(),
						dealResult.getResultId() };
				param[i] = obj;
			}

			int[] rows = null;
			if (param.length != 0 && param[0] != null) {
				rows = run.batch(conn, updateResultSql, param);
			}
			String chains = "('";
			chains += StringUtils.join(chainList.toArray(), "','") + "')";

			// 更新chain表
			run.update(conn, "update ix_dealership_chain set work_status=2 where  chain_code in " + chains);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	public static FastPoi queryPoiByPoiNum(String cfmNum, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		FastPoi fastPoi = new FastPoi();

		try {
			StringBuilder sb = new StringBuilder();
			sb.append("WITH A AS ");
			sb.append(" (SELECT I.POI_NUM,");
			sb.append("         I.PID,");
			sb.append("         I.KIND_CODE,");
			sb.append("         I.CHAIN,");
			sb.append("         I.POST_CODE,");
			sb.append("         I.X_GUIDE,");
			sb.append("         I.Y_GUIDE,");
			sb.append("         I.GEOMETRY,");
			sb.append("         P1.NAME OFFICENAME,");
			sb.append("        (SELECT NAME");
			sb.append("            FROM IX_POI_NAME");
			sb.append("           WHERE POI_PID = I.PID");
			sb.append("             AND NAME_CLASS = 3");
			sb.append("             AND NAME_TYPE = 1");
			sb.append("             AND U_RECORD <> 2");
			sb.append("             AND LANG_CODE IN ('CHI', 'CHT')) SHORT_NAME,");
			sb.append("         A.FULLNAME");
			sb.append("    FROM IX_POI I, IX_POI_NAME P1, IX_POI_ADDRESS A");
			sb.append("   WHERE I.POI_NUM = :1");
			sb.append("     AND I.PID = P1.POI_PID");
			sb.append("     AND P1.U_RECORD <> 2");
			sb.append("     AND P1.NAME_CLASS = 1");
			sb.append("     AND P1.NAME_TYPE = 1");
			sb.append("    AND P1.LANG_CODE IN ('CHI', 'CHT')");
			sb.append("     AND I.PID = A.POI_PID");
			sb.append("     AND A.U_RECORD <> 2");
			sb.append("    AND A.LANG_CODE IN ('CHI', 'CHT')),");
			sb.append(" B AS");
			sb.append(" (SELECT C.POI_PID,");
			sb.append("         LISTAGG(C.CONTACT, '|') WITHIN GROUP(ORDER BY C.POI_PID) AS TEL");
			sb.append("    FROM IX_POI_CONTACT C,IX_POI I");
			sb.append("   WHERE  C.POI_PID = I.PID AND I.POI_NUM=:2 ");
			sb.append("      AND (C.CONTACT_TYPE IN (1,2,3,4) AND C.CONTACT_DEPART IN (0, 16, 8))");
			sb.append("     AND C.U_RECORD <> 2");
			sb.append("   GROUP BY C.POI_PID)");
			sb.append(" SELECT POI_NUM,");
			sb.append("       PID,");
			sb.append("       KIND_CODE,");
			sb.append("       CHAIN,");
			sb.append("       POST_CODE,");
			sb.append("       X_GUIDE,");
			sb.append("       Y_GUIDE,");
			sb.append("       GEOMETRY,");
			sb.append("       OFFICENAME,");
			sb.append("       SHORT_NAME,");
			sb.append("       FULLNAME,");
			sb.append("       TEL");
			sb.append("  FROM A, B");
			sb.append(" WHERE A.PID = B.POI_PID");
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setString(1, cfmNum);
			pstmt.setString(2, cfmNum);

			resultSet = pstmt.executeQuery();
			if (resultSet.next()) {
				fastPoi.setAddr(resultSet.getString("FULLNAME") != null ? resultSet.getString("FULLNAME") : "");
				fastPoi.setChain(resultSet.getString("CHAIN") != null ? resultSet.getString("CHAIN") : "");
				fastPoi.setGeometry(GeoTranslator.struct2Jts((STRUCT) resultSet.getObject("GEOMETRY")));
				fastPoi.setKindCode(resultSet.getString("KIND_CODE") != null ? resultSet.getString("KIND_CODE") : "");
				fastPoi.setName(resultSet.getString("OFFICENAME") != null ? resultSet.getString("OFFICENAME") : "");
				fastPoi.setPid(resultSet.getInt("PID"));
				fastPoi.setPoiNum(resultSet.getString("POI_NUM"));
				fastPoi.setPostCode(resultSet.getString("POST_CODE") != null ? resultSet.getString("POST_CODE") : "");
				fastPoi.setShortName(
						resultSet.getString("SHORT_NAME") != null ? resultSet.getString("SHORT_NAME") : "");
				fastPoi.setTel(resultSet.getString("TEL") != null ? resultSet.getString("TEL") : "");
				fastPoi.setxGuide(resultSet.getDouble("X_GUIDE"));
				fastPoi.setyGuide(resultSet.getDouble("Y_GUIDE"));
			}

			return fastPoi;

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 
	 * @param dealershipMR
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static IxDealershipResult updateResultObj(IxDealershipResult dealership, Map dbConMap) throws Exception {

		if (dealership.getDealSrcDiff() == 2) {
			dealership.setIsDeleted(1);
		}
		if (dealership.getMatchMethod() == 0) {
			dealership.setMatchMethod(2);
		}
		if (dealership.getCfmPoiNum() != null && !"".equals(dealership.getCfmPoiNum())) {
			dealership.setCfmIsAdopted(1);
		}

		
		Connection regionConn = (Connection) dbConMap.get(dealership.getRegionId());
		FastPoi obj = queryPoiByPoiNum(dealership.getCfmPoiNum(), regionConn);

		if (dealership.getWorkflowStatus() == 1 || dealership.getWorkflowStatus() == 2) {
			dealership.setDealCfmDate(DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT));

			if (obj != null) {
				dealership.setPoiKindCode(obj.getKindCode());
				dealership.setPoiChain(obj.getChain());

				dealership.setPoiName(obj.getName());
				dealership.setPoiNameShort(obj.getShortName());
				dealership.setAddress(obj.getAddr());

				dealership.setPoiTel(obj.getTel());
				dealership.setPoiPostCode(obj.getPostCode());
				dealership.setPoiXGuide(obj.getxGuide());
				dealership.setPoiYGuide(obj.getyGuide());
				
				if (obj.getGeometry()!=null)
				{
					JSONArray array = GeoTranslator.jts2JSONArray(obj.getGeometry());
					dealership.setPoiXDisplay(array.getDouble(0));
					dealership.setPoiXDisplay(array.getDouble(1));
				}
				
			}
		}
		if (obj != null) {
			updateResultGeo(dealership, obj);
		}

		return dealership;

	}

	/**
	 * 
	 * @param dealershipMR
	 * @param obj
	 * @return
	 * @throws Exception
	 * @throws ParseException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static IxDealershipResult updateResultGeo(IxDealershipResult dealership, FastPoi obj)
			throws ClientProtocolException, IOException, ParseException, Exception {
		if (obj == null) {
			return dealership;
		}

		if (dealership.getMatchMethod() == 1) {
			dealership.setGeometry(obj.getGeometry());
		} else {
			try {
				StringBuffer sb = new StringBuffer();
				if (dealership.getProvince() != null && !"".equals(dealership.getProvince())) {
					sb.append(dealership.getProvince());
				}
				if (dealership.getCity() != null && !"".equals(dealership.getCity())) {
					sb.append(dealership.getCity());
				}
				if (dealership.getAddress() != null && !"".equals(dealership.getAddress())) {
					sb.append(dealership.getAddress());
				}
				dealership.setGeometry(BaiduGeocoding.geocoder(sb.toString()));
			} catch (Exception e) {
				throw new Exception("无法获取geometry");
			}
		}
		return dealership;

	}

	public static int queryPidByPoiNum(String poiNum, Connection conn) throws Exception {
		String sql = "select pid from ix_poi t where t.poi_num =?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int pid = 0;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, poiNum);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pid = rs.getInt("pid");
			}
			return pid;

		} catch (Exception e) {
			throw new SQLException("加载ix_poi失败：" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}

}
