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
 * @Title:GLM55106
 * @Package:com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 
 *  检查条件：
            非删除点门牌对象
            检查原则：
	点门牌引导非删除link的种别是人渡或轮渡RD_LINK.kind=11或13，报LOG：点门牌引导link为航线！
 * @author:Jarvis 
 * @date: 2017年10月10日
 */
public class GLM55106 extends BasicCheckRule {
	
	public void run() throws Exception {
		for(Map.Entry<Long, BasicObj> entry : getRowList().entrySet()){
			BasicObj obj = entry.getValue();
			if(!obj.getMainrow().getOpType().equals(OperationType.PRE_DELETED) && obj.objName().equals(ObjectName.IX_POINTADDRESS)){
				IxPointAddressObj pointAddressObj=(IxPointAddressObj) obj;
				IxPointaddress ixPointaddress = (IxPointaddress) pointAddressObj.getMainrow();
				Long linkPid = ixPointaddress.getGuideLinkPid();
				Integer kind = getKindByLinkPid(linkPid);
				if(kind==11 || kind==13){
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
	 * 根据linkPid求kind
	 * @param linkPid
	 * @return
	 * @throws Exception
	 */
	public Integer getKindByLinkPid(Long linkPid) throws Exception{
		String sql = "select kind from rd_link where link_pid = "+linkPid;
    	
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
			log.error("查询kind出错",e);
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}

}
