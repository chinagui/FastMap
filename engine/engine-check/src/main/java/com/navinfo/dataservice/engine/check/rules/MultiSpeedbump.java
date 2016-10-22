package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.speedbump.RdSpeedbump;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: MultiSpeedbump
 * @author zhangxiaoyi
 * @date 2016年8月18日
 * @Description: 
 * 减速带	word	MultiSpeedbump	后台	相同的线和点不能重复创建减速带
 * */
public class MultiSpeedbump extends baseRule {

	/**
	 * 
	 */
	public MultiSpeedbump() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
		for(IRow obj:checkCommand.getGlmList()){
			//减速带 create
			if(obj instanceof RdSpeedbump ){
				RdSpeedbump rdSpeedbump = (RdSpeedbump)obj;
				boolean result = checkRdSpeedbump(rdSpeedbump);
				if(!result){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}

	}
	private boolean checkRdSpeedbump(RdSpeedbump rdSpeedbump) throws Exception{
		int linkPid = rdSpeedbump.getLinkPid();
		int nodePid = rdSpeedbump.getNodePid();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_SPEEDBUMP RLC ");
		sb.append(" WHERE RLC.U_RECORD != 2 AND RLC.LINK_PID = ");
		sb.append(linkPid);
		sb.append(" AND RLC.NODE_PID = ");
		sb.append(nodePid);

		String sql = sb.toString();
		
        DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		
		if (resultList.size()>0){
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

}
