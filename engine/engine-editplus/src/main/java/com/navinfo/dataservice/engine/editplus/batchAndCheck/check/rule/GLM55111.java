package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPointAddressObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
/**
 * 
 * @Title:GLM55111
 * @Package:com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 
 *  检查条件：
	非删除点门牌对象
	检查原则：
	点门牌引导非删除link的车辆限制（link限制信息-车辆限制的车辆类型）包含客车不允许且步行者不允许的，报LOG：点门牌引导link有车辆限制！
 * @author:Jarvis 
 * @date: 2017年10月11日
 */
public class GLM55111 extends BasicCheckRule {
	
	public void run() throws Exception {
		for(Map.Entry<Long, BasicObj> entry : getRowList().entrySet()){
			BasicObj obj = entry.getValue();
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED) && obj.objName().equals(ObjectName.IX_POINTADDRESS)){
				IxPointAddressObj pointAddressObj=(IxPointAddressObj) obj;
				IxPointaddress ixPointaddress = (IxPointaddress) pointAddressObj.getMainrow();
				Long linkPid = ixPointaddress.getGuideLinkPid();
				boolean flag = judgeBanByVehicleType(linkPid);
				if(flag){
					setCheckResult(ixPointaddress.getGeometry(), pointAddressObj, ixPointaddress.getMeshId(),null);
				}
			}
		}
	}
	
	@Override
	public void runCheck(BasicObj obj) throws Exception {
	}
    
	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
	}
	
	
	public boolean judgeBanByVehicleType(Long linkPid) throws Exception{
		String sql = "select count(1) from rd_link_limit ll where ip.guide_link_pid = "+linkPid+" and ll.type=2 and ll.vehicle=9 ";
    	
    	PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		Connection conn = null;
		
    	try {
    		conn = getCheckRuleCommand().getConn();
    		pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			if (resultSet.next()&&resultSet.getInt(1)>0) {
				return true;
			}
    		return false;
    	}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}

}
