package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: GLM26017
 * @author songdongyan
 * @date 上午10:03:12
 * @Description: GLM26017：如果交限、语音引导、顺行进入线和退出线挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改；
 */
public class GLM26017 extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		
		String sql = "select node_pid from rd_cross_node where node_pid=:1";
		
		PreparedStatement pstmt = getConn().prepareStatement(sql);
		
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction)obj;
				int inNodePid = rdRestriction.getNodePid();
						
				for(IRow deObj:rdRestriction.getDetails()){
					if(deObj instanceof RdRestrictionDetail){
						RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
						//获取outNodePid
						int outNodePid = rdRestrictionDetail.igetOutNodePid();
						//如果交限、语音引导、顺行进入线和退出线挂接在同一点上
						if(inNodePid == outNodePid){
							pstmt.setInt(1, inNodePid);
							
							ResultSet resultSet = pstmt.executeQuery();

							boolean flag = false;

							if (resultSet.next()) {
								flag = true;
							}

							resultSet.close();

							
							
							if (!flag) {

								this.setCheckResult("", "", 0);
								return;
							}
						}

					}
				}
				
				
				
			}
					
		}
		pstmt.close();

	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
