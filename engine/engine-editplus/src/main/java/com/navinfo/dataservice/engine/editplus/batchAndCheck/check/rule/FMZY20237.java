package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

/**
 * 检查条件： 非删除（根据履历判断删除） 检查原则：
 * 当CONTACT_TYPE=11，必须满足“区号-电话”标准，区号应包含在《sc_point_adminarea》表中“AREACODE”列中，
 * 同时长度不能超过对应“‘PHONENUM_LEN’值+1”（以POI的行政区划号关联sc_point_adminarea表ADMINAREACODE）。
 * 
 * Log：传真区号或位数错误，正确区号为**，位数应为**。
 * 
 *
 */
public class FMZY20237 extends BasicCheckRule {

	@Override
	public void runCheck(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {
			return;
		}
		List<IxPoiContact> contacts = poiObj.getIxPoiContacts();

		MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
		for (IxPoiContact contact : contacts) {
			if (contact.getContactType() == 11) {
				String number = contact.getContact();
				if (number == null) {
					continue;
				}
				if (number.contains("-")) {
					String[] tel = number.split("-");
					int regionId = (int) poi.getRegionId();
					// 通过poi的regionId获取adminCode
					String adminCode = getAdminCodeByRegionId(regionId);
					JSONObject adminCodeObj = metaApi.searchByAdminCode(adminCode);
					JSONObject telObj = adminCodeObj.getJSONObject(adminCode);
					String rightAreaCode = telObj.getString("code");
					int rightLen = telObj.getInt("telLength");
					// poi的 areaCode 和 telLen
					String poiAreaCode = tel[0];
					// 表sc_point_adminarea 存的长度值不包括"-",所以去掉"-"计算电话长度
					int poiTelLen = tel[0].length() + tel[1].length();
					if (!rightAreaCode.equals(poiAreaCode) || rightLen != poiTelLen) {
						this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
								"传真区号或位数错误，正确区号为" + rightAreaCode + "，位数应为" + rightLen + "。");
					}

				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @param regionId
	 * @return adminCode
	 * @throws Exception
	 */
	public String getAdminCodeByRegionId(int regionId) throws Exception {
		int adminCode = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try{
			conn = getCheckRuleCommand().getConn();
			StringBuilder sb = new StringBuilder();
			sb.append("select admin_id from AD_ADMIN where region_id = ");
			sb.append(regionId);
			String sql = sb.toString();

			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				adminCode = rs.getInt("admin_id");
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		
		return Integer.toString(adminCode);
	}

}
