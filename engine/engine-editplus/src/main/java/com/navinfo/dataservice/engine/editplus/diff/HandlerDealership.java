package com.navinfo.dataservice.engine.editplus.diff;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;

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
	public static boolean isSameTableAndDb(IxDealershipResult dealershipMR, BasicObj obj) throws Exception {

		if (dealershipMR == null || obj == null) {
			return false;
		}

		IxPoi p = (IxPoi) obj.getMainrow();
		IxPoiObj poiObj = (IxPoiObj) obj;

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean nameShortFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		IxPoiName poiName = poiObj.getOfficeStandardCHName();
		IxPoiName poiAliasName = poiObj.getAliasCHITypeName();

		IxPoiAddress poiAddr = poiObj.getChiAddress();
		System.out.println(dealershipMR.getName());
		System.out.println(poiName.getName());
		// 判断官方标准名称是否相等
		if (dealershipMR.getName() != null && poiName.getName() != null
				&& (!dealershipMR.getName().equals(poiName.getName()))) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断别名是否相等
		if (dealershipMR.getNameShort() != null && poiAliasName.getName() != null
				&& (!dealershipMR.getNameShort().equals(poiAliasName.getName()))) {
			str.append("别名不同；");
			nameShortFlag = false;
		}
		// 判断分类是否相等
		if ((dealershipMR.getKindCode()!=null&&p.getKindCode()!=null) &&(!dealershipMR.getKindCode().equals(p.getKindCode()))) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (!dealershipMR.getChain().equals(p.getChain())) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (dealershipMR.getAddress() != null && !("".equals(dealershipMR.getAddress())) && poiAddr.getAddrname() != null
				&& (!dealershipMR.getAddress().equals(poiAddr.getAddrname()))) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (dealershipMR.getPostCode()!=null && p.getPostCode()!=null &&(!dealershipMR.getPostCode().equals(p.getPostCode()))) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for (IxPoiContact c : poiObj.getIxPoiContacts()) {
			sb.append(c.getContact()).append(";");
		}
		if (sb.length() > 0)
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
		telephone = StringUtil.sortPhone(telephone);
		if (!dealershipMR.getTelephone().equals(telephone)) {
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
	public static boolean isNoChangePoiNature(IxDealershipResult dealershipMR, BasicObj obj) throws Exception {
		if (dealershipMR == null || obj == null) {
			return false;
		}
		
		IxPoi p = (IxPoi) obj.getMainrow();
		IxPoiObj poiObj = (IxPoiObj) obj;

		boolean isEqual = false;
		boolean nameFlag = true;
		boolean kindFlag = true;
		boolean chainFlag = true;
		boolean addressFlag = true;
		boolean postCodeFlag = true;
		boolean phoneFlag = true;

		StringBuffer str = new StringBuffer();

		IxPoiName poiName = poiObj.getOfficeStandardCHName();

		IxPoiAddress poiAddr = poiObj.getChiAddress();
		// 判断官方标准名称是否相等
		if (dealershipMR.getPoiName() != null && !("".equals(dealershipMR.getPoiName())) && poiName.getName() != null
				&& (!dealershipMR.getPoiName().equals(poiName.getName()))) {
			str.append("官方标准名称不同；");
			nameFlag = false;
		}
		// 判断分类是否相等
		if (dealershipMR.getPoiKindCode() != null && !("".equals(dealershipMR.getPoiKindCode())) && p.getKindCode() != null
				&& (!dealershipMR.getPoiKindCode().equals(p.getKindCode()))) {
			str.append("分类不同；");
			kindFlag = false;
		}
		// 判断品牌是否相等
		if (dealershipMR.getPoiChain() != null && !("".equals(dealershipMR.getPoiChain())) && p.getChain() != null
				&& (!dealershipMR.getPoiChain().equals(p.getChain()))) {
			str.append("品牌不同；");
			chainFlag = false;
		}
		// 判断地址是否相等
		if (dealershipMR.getPoiAddress() != null && !("".equals(dealershipMR.getPoiAddress())) && poiAddr.getAddrname() != null
				&& (!dealershipMR.getPoiAddress().equals(poiAddr.getAddrname()))) {
			str.append("地址不同；");
			kindFlag = false;
		}
		// 判断邮编是否相等
		if (dealershipMR.getPoiPostCode() != null && !("".equals(dealershipMR.getPoiPostCode())) && p.getPostCode() != null
				&& (!dealershipMR.getPoiPostCode().equals(p.getPostCode()))) {
			str.append("邮编不同；");
			postCodeFlag = false;
		}

		// 判断联系方式是否相同
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for (IxPoiContact c : poiObj.getIxPoiContacts()) {
			sb.append(c.getContact()).append(";");
		}
		if (sb.length() > 0)
			telephone = sb.toString().substring(0, sb.toString().length() - 1);
		telephone = StringUtil.sortPhone(telephone);
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
	public static boolean isDealershipChain(BasicObj obj) throws Exception {
		if (obj==null){return false;}
		IxPoi p = (IxPoi) obj.getMainrow();
		MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		Map<String, String> mapKindChain = metadataApi.scPointSpecKindCodeType15();
		if (p.getChain().equals(mapKindChain.get(p.getKindCode()))) {
			return true;
		}
		return false;
	}

	// 分类与品牌是否一致
	// 通过RESULT.cfm_poi_num关联的非删除POI的分类和品牌与RESULT表中POI分类和POI品牌一致，则“分类与品牌一致”，否则“分类与品牌不一致”；
	public static boolean isSameKindChain(IxDealershipResult dealershipMR, BasicObj obj) {
		if(obj==null) {return false;}
		IxPoi p = (IxPoi) obj.getMainrow();
		if (p.getChain().equals(dealershipMR.getPoiChain()) && p.getKindCode().equals(dealershipMR.getPoiKindCode())) {
			return true;
		}
		return false;
	}
	
	public void updateDealershipDb(List<IxDealershipResult> diffFinishResultList, String chain,Map dbMap)
			throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getDealershipConnection();
			
//			 conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.131:1521/orcl",
//					"FM_DEALERSHIP", "FM_DEALERSHIP");
			

			// 更新result表
			String updateResultSql = "UPDATE ix_dealership_result r SET r.workflow_status=?,r.is_deleted=?,r.match_method=?,r.poi_num_1=?,r.poi_num_2=?,r.poi_num_3=?,r.poi_num_4=?,r.poi_num_5=?, "
					+ " r.similarity=?,r.cfm_poi_num=?,r.cfm_is_adopted=?,r.deal_cfm_date=?,r.poi_kind_code=?,r.poi_chain=?,r.poi_name=?,r.poi_name_short=?,r.poi_address=?,r.poi_tel=?,"
					+ " r.poi_post_code=?,r.poi_x_display=?,r.poi_y_display=?,r.poi_y_guide=?,r.poi_x_guide=? where r.result_id=?";

			Object[][] param = new Object[diffFinishResultList.size()][];

			for (int i = 0; i < diffFinishResultList.size(); i++) {
				IxDealershipResult dealResult = diffFinishResultList.get(i);
			
				updateResultObj(dealResult, dbMap);
				
				Object[] obj = new Object[] { dealResult.getWorkflowStatus(), dealResult.getIsDeleted(),
						dealResult.getMatchMethod(), dealResult.getPoiNum1(), dealResult.getPoiNum2(),
						dealResult.getPoiNum3(), dealResult.getPoiNum4(), dealResult.getPoiNum5(),
						dealResult.getSimilarity(), dealResult.getCfmPoiNum(), dealResult.getCfmIsAdopted(),
						dealResult.getDealCfmDate(), dealResult.getPoiKindCode(), dealResult.getPoiChain(),
						dealResult.getPoiName(), dealResult.getPoiNameShort(), dealResult.getPoiAddress(),
						dealResult.getPoiTel(), dealResult.getPoiPostCode(), dealResult.getPoiXDisplay(),dealResult.getPoiYDisplay(),
						dealResult.getPoiXGuide(), dealResult.getPoiYGuide(), dealResult.getResultId() };
				param[i] = obj;
			}

			int[] rows = null;
			if (param.length!=0&& param[0] != null) {
				rows = run.batch(conn, updateResultSql, param);
			}

			// 更新source表
//			String updateSourceSql = "UPDATE ix_dealership_source s SET s.PROVINCE=r.PROVINCE,s.PROVINCE=r.CITY,s.PROJECT=r.PROJECT,s.KIND_CODE=r.KIND_CODE,"
//					+ " s.PROVINCE=r.PROVINCE,s.PROVINCE=r.CITY,s.PROJECT=r.PROJECT,s.KIND_CODE=r.KIND_CODE,s.NAME=r.NAME,s.NAME_SHORT=r.NAME_SHORT,"
//					+ " s.ADDRESS=r.ADDRESS,s.TEL_SALE=r.TEL_SALE,s.TEL_SERVICE=r.TEL_SERVICE,s.TEL_OTHER=r.TEL_OTHER,s.POST_CODE=r.POST_CODE,s.NAME_ENG=r.NAME_ENG,"
//					+ " s.ADDRESS_ENG=r.ADDRESS_ENG,s.PROVIDE_DATE=r.PROVIDE_DATE,s.IS_DELETED=r.IS_DELETED,s.FB_SOURCE=r.FB_SOURCE,s.FB_CONTENT=r.FB_CONTENT,"
//					+ " s.FB_AUDIT_REMARK=r.FB_AUDIT_REMARK,s.FB_DATE=r.FB_DATE,s.CFM_POI_NUM=r.CFM_POI_NUM,s.CFM_MEMO=r.CFM_MEMO,s.DEAL_CFM_DATE=r.DEAL_CFM_DATE,"
//					+ " s.POI_KIND_CODE=r.POI_KIND_CODE,s.POI_CHAIN=r.POI_CHAIN,s.POI_NAME=r.POI_NAME,s.POI_NAME_SHORT=r.POI_NAME_SHORT,s.POI_ADDRESS=r.POI_ADDRESS,"
//					+ " s.POI_TEL=r.POI_TEL,s.POI_POST_CODE=r.POI_POST_CODE,s.POI_X_DISPLAY=r.POI_X_DISPLAY,s.POI_Y_DISPLAY=r.POI_Y_DISPLAY,s.POI_X_GUIDE=r.POI_X_GUIDE,"
//					+ " s.POI_Y_GUIDE=r.POI_Y_GUIDE,s.GEOMETRY=r.GEOMETRY "
//					+ " from ix_dealership_result r WHERE s.source_id=r.source_id AND r.deal_status=1 AND s.chain="
//					+ chain;
//
//			run.update(conn, updateSourceSql);

			// 更新chain表
			run.update(conn,
					"update ix_dealership_chain set work_status=2 where  chain_code='" + chain+"'");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("更新失败:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	public static int queryPidByPoiNum(String poiNum, Connection conn) throws Exception {	
		String sql = "select pid from ix_poi t where t.poi_num =?";

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int pid=0;
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, poiNum);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				pid=rs.getInt("pid");
			}
			return pid;
		
	} catch (Exception e) {
		throw new SQLException("加载ix_poi失败：" + e.getMessage(), e);
	} finally {
		DbUtils.closeQuietly(rs);
		DbUtils.closeQuietly(pstmt);
	}
}

public static BasicObj queryPoiByPid(IxDealershipResult dealResult, Connection conn) throws Exception {
int pid = queryPidByPoiNum(dealResult.getCfmPoiNum(), conn);
BasicObj obj = ObjSelector.selectByPid(conn, "IX_POI", null, false, pid, false);
return obj;
}
	/**
	 * 
	 * @param dealershipMR
	 * @param obj
	 * @return
	 * @throws Exception 
	 */
	public static IxDealershipResult updateResultObj(IxDealershipResult dealership, Map dbConMap) throws Exception {
		
		
		if(dealership.getDealSrcDiff()==2){dealership.setIsDeleted(1);}
		if(dealership.getMatchMethod()==0){dealership.setMatchMethod(2);}
		if(dealership.getCfmPoiNum()!=null&&!"".equals(dealership.getCfmPoiNum())){dealership.setCfmIsAdopted(1);}
		
		if(!dbConMap.containsKey(dealership.getRegionId())) {
//			throw new JobException("resultId:"+dealResult.getResultId()+"赋值的region_id对应的大区库不存在");
			return dealership;
		}
		Connection regionConn = (Connection) dbConMap.get(dealership.getRegionId());
		BasicObj obj = queryPoiByPid(dealership, regionConn);
		
		
		if(dealership.getWorkflowStatus()==1 || dealership.getWorkflowStatus()==2){
			dealership.setDealCfmDate(DateUtils.dateToString(new Date(), DateUtils.DATE_COMPACTED_FORMAT));
			
			if (obj!=null)
			{
			IxPoi p = (IxPoi) obj.getMainrow();
			IxPoiObj poiObj = (IxPoiObj) obj;
			
			IxPoiName poiName = poiObj.getOfficeStandardCHName();
			IxPoiName poiAliasName = poiObj.getAliasCHITypeName();
			IxPoiAddress poiAddr = poiObj.getChiAddress();
			
			dealership.setPoiKindCode(p.getKindCode());
			dealership.setPoiChain(p.getChain());
			dealership.setPoiChain(p.getChain());
			dealership.setPoiName(poiName.getName());
			dealership.setPoiNameShort(poiAliasName.getName());
			dealership.setAddress(poiAddr.getAddrname());
			
			StringBuffer sb = new StringBuffer();
			String telephone = "";
			for (IxPoiContact c : poiObj.getIxPoiContacts()) {
				sb.append(c.getContact()).append(";");
			}
			if (sb.length() > 0)
				telephone = sb.toString().substring(0, sb.toString().length() - 1);
			dealership.setPoiTel(telephone);
			dealership.setPoiPostCode(p.getPostCode());
			dealership.setPoiXGuide(p.getXGuide());
			dealership.setPoiYGuide(p.getYGuide());

			JSONArray array =GeoTranslator.jts2JSONArray(p.getGeometry());
			dealership.setPoiXDisplay(array.getDouble(0));
			dealership.setPoiXDisplay(array.getDouble(1));
			}
		}
		if (obj!=null)
		{
		IxPoi p = (IxPoi) obj.getMainrow();
		updateResultGeo(dealership,p);
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
	public static IxDealershipResult updateResultGeo(IxDealershipResult dealership, IxPoi obj) throws ClientProtocolException, IOException, ParseException, Exception {
		if(obj==null) {return dealership;}
		
		if (dealership.getMatchMethod()==1){
			dealership.setGeometry(obj.getGeometry());
		}else
		{
			try{
					StringBuffer sb = new StringBuffer();
					if(dealership.getProvince()!=null&&!"".equals(dealership.getProvince())){
						sb.append(dealership.getProvince());
					}
					if(dealership.getCity()!=null&&!"".equals(dealership.getCity())){
						sb.append(dealership.getCity());
					}
					if(dealership.getAddress()!=null&&!"".equals(dealership.getAddress())){
						sb.append(dealership.getAddress());
					}
					dealership.setGeometry(BaiduGeocoding.geocoder(sb.toString()));
			}catch(Exception e) {
				throw new Exception("无法获取geometry");
			}
		}
        return dealership;

	}
	
	

}
