package com.navinfo.dataservice.engine.check.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.check.core.baseRule;

import net.sf.json.JSONObject;

/** 
* @ClassName: CheckRuleFMZY20237 
* @author: zhangpengpeng 
* @date: 2016年11月12日
* @Desc: 通用深度信息传真格式检查
* 		   检查原则：当CONTACT_TYPE=11，必须满足“区号-电话”标准，
* 				区号应包含在《CI_PARA_TEL》表中“CODE”列中，
* 				同时长度不能超过对应“TEL_LEN”值。
*		 Log：传真区号或位数错误，正确区号为**，位数应为**。
*/
public class CheckRuleFMZY20237 extends baseRule{
	public void preCheck(CheckCommand checkCommand){
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof IxPoi){
				IxPoi poi = (IxPoi) obj;
				//需要判断POI的状态不为删除
				LogReader logRead=new LogReader(this.getConn());
				int poiState = logRead.getObjectState(poi.getPid(), "IX_POI");
				//state=2为删除
				if (poiState == 2){
					return ;
				}
				int regionId = poi.getRegionId();
				//通过poi的regionId获取adminCode
				String adminCode = getAdminCodeByRegionId(regionId);
				//获取adminCode对应的电话object
				MetadataApi metaApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				JSONObject adminCodeObj = metaApi.searchByAdminCode(adminCode);
				
				List<IRow> contacts = poi.getContacts();
				if (contacts.size() > 0){
					for (IRow contact: contacts){
						IxPoiContact ixPoiContact = (IxPoiContact) contact;
						String poiContact = ixPoiContact.getContact();
						//CONTACT_TYPE=11 传真格式
						if (ixPoiContact.getContactType() == 11){

							if (poiContact.contains("-")){
								String[] tel= poiContact.split("-");
								JSONObject telObj = adminCodeObj.getJSONObject(adminCode);
								//获取正确的poi areaCode 和 telLen
								String rightAreaCode = telObj.getString("code");
								int rightLen = telObj.getInt("telLength");
								
								//poi的 areaCode 和 telLen
								String poiAreaCode = tel[0];
								//表sc_point_adminarea 存的长度值不包括"-",所以去掉"-"计算电话长度
								int poiTelLen = tel[0].length() + tel[1].length();
								
								StringBuffer logMsg = new StringBuffer();
								//判断传真区号正确
								if (!poiAreaCode.equals(rightAreaCode)){
									logMsg.append("传真区号错误，正确区号为"+rightAreaCode + "; ");
								}
								//判断传真位数正确
								if (rightLen != poiTelLen){
									logMsg.append("传真位数错误，正确位数应为"+String.valueOf(rightLen) + "; ");
								}
								String checkLog = logMsg.toString();
								if (StringUtils.isNotEmpty(checkLog)){
									this.setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(),
											checkLog);
									break;
								}
							}
							
						}
					}
				}
				
			}
		}
	}
	
	/**
	 * @param regionId
	 * @return adminCode
	 * @throws Exception
	 */
	public String getAdminCodeByRegionId(int regionId) throws Exception{
		int adminCode = 0;
		Connection conn = this.getConn();
		StringBuilder sb = new StringBuilder();
        sb.append("select admin_id from AD_ADMIN where region_id = ");
        sb.append(regionId);
		String sql = sb.toString();
		
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		
		pstmt = conn.prepareStatement(sql);
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			adminCode = resultSet.getInt("admin_id");
		}
		return Integer.toString(adminCode);
	}
}
