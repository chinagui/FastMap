package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM15007
 * @author Han Shaoming
 * @date 2017年1月5日 下午2:50:38
 * @Description TODO
 * 当道路为单方向时，顺向或逆向速度限制为0时，对应的速度限制来源应该为0，否则报log
 * 条件线限速限速来源  服务端后检查       
 * 条件线限速限速值	服务端后检查
 * 普通线限速限速来源  服务端后检查
 * 普通线限速限速值	服务端后检查

 */
public class GLM15007 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//条件线限速限速来源,条件线限速限速值
			//普通线限速限速来源,普通线限速限速值
			if (row instanceof RdLinkSpeedlimit){
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;
				this.checkRdLinkSpeedlimit(rdLinkSpeedlimit);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdSpeedlimit
	 * @throws Exception 
	 */
	private void checkRdLinkSpeedlimit(RdLinkSpeedlimit rdLinkSpeedlimit) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLinkSpeedlimit.changedFields();
		if(!changedFields.isEmpty()){
			//条件线限速限速值,普通线限速限速值
			//顺方向 
			if(changedFields.containsKey("fromSpeedLimit")){
				int fromSpeedLimit = Integer.parseInt((String) changedFields.get("fromSpeedLimit"));
				if(fromSpeedLimit == 0){
					boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
			//逆方向
			else if(changedFields.containsKey("toSpeedLimit")){
				int toSpeedLimit = Integer.parseInt((String) changedFields.get("toSpeedLimit"));
				if(toSpeedLimit == 0){
					boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
			//条件线限速限速来源,普通线限速限速来源
			//顺方向 
			else if(changedFields.containsKey("fromLimitSrc")){
				int fromLimitSrc = (int) changedFields.get("fromLimitSrc");
				if(fromLimitSrc != 0){
					boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
			//逆方向
			else if(changedFields.containsKey("toLimitSrc")){
				int toLimitSrc = (int) changedFields.get("toLimitSrc");
				if(toLimitSrc != 0){
					boolean check = this.check(rdLinkSpeedlimit.getLinkPid());
					
					if(check){
						String target = "[RD_LINK," + rdLinkSpeedlimit.getLinkPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int pid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		     
		sb.append("SELECT L.LINK_PID FROM RD_LINK_SPEEDLIMIT RLS, RD_LINK L WHERE RLS.LINK_PID = L.LINK_PID");
		sb.append(" AND L.LINK_PID = "+pid);
		sb.append(" AND L.U_RECORD <>2 AND RLS.U_RECORD <>2 AND L.DIRECT IN (2, 3)");
		sb.append(" AND ((RLS.FROM_SPEED_LIMIT = 0 AND RLS.FROM_LIMIT_SRC <> 0)");
		sb.append(" OR (RLS.TO_SPEED_LIMIT = 0 AND RLS.TO_LIMIT_SRC <> 0))");
		String sql = sb.toString();
		log.info("后检查GLM15007--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
