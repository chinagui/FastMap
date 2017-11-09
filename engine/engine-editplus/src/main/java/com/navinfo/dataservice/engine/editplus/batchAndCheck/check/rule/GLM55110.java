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
 * @Title:GLM55110
 * @Package:com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 
 *  检查条件：
	非删除点门牌对象
	检查原则：
	点门牌引导非删除link的开发状态是未验证RD_LINK.DEVELOP_STATE=2，报LOG：点门牌引导link为未验证！
 * @author:Jarvis 
 * @date: 2017年10月10日
 */
public class GLM55110 extends BasicCheckRule {
	
	public void run() throws Exception {
		for(Map.Entry<Long, BasicObj> entry : getRowList().entrySet()){
			BasicObj obj = entry.getValue();
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED) && obj.objName().equals(ObjectName.IX_POINTADDRESS)){
				IxPointAddressObj pointAddressObj=(IxPointAddressObj) obj;
				IxPointaddress ixPointaddress = (IxPointaddress) pointAddressObj.getMainrow();
				Long linkPid = ixPointaddress.getGuideLinkPid();
				Integer delevopState = getDelevopStateByLinkPid(linkPid);
				if(delevopState == 2){
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
	
	/**
	 * 根据linkPid求is_viaduct
	 * @param linkPid
	 * @return
	 * @throws Exception
	 */
	public Integer getDelevopStateByLinkPid(Long linkPid) throws Exception{
		String sql = "select develop_state from rd_link where u_record <> 2 and link_pid = "+linkPid;
    	
    	PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		Connection conn = null;
		
    	try {
    		conn = getCheckRuleCommand().getConn();
    		pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				return resultSet.getInt(1);
			}
    		return 0;
    	}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}

}
