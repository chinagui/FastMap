package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RdTollgate001
 * @author zhangxiaoyi
 * @date 2016年8月18日
 * @Description: 
 * 收费站	word	RDTOLLGATE001	后台	收费站的退出线不能与车信的进入线是同一条link
 * 新增收费站服务端前检查:RdTollgate
 * 新增车信,修改车信服务端前检查:RdLaneConnexity	
 * */
public class RdTollgate001 extends baseRule {

	/**
	 * 
	 */
	public RdTollgate001() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
		for(IRow row:checkCommand.getGlmList()){
			//新增收费站
			if(row instanceof RdTollgate ){
				RdTollgate rdTollgate = (RdTollgate)row;
				int outLinkPid=rdTollgate.getOutLinkPid();
				String sql="SELECT 1 FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2 AND IN_LINK_PID = "+outLinkPid;
				log.info("RdTollgate后检查RDTOLLGATE001--sql:" + sql);
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				if(resultList.size()>0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
			//新增车信,修改车信
			else if (row instanceof RdLaneConnexity){
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity) row;
				checkRdLaneConnexity(rdLaneConnexity);
			}
		}

	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @author Han Shaoming
	 * @param rdLaneConnexity
	 * @throws Exception 
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity) throws Exception {
		// TODO Auto-generated method stub
		//新增车信,修改车信,触发检查
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT 1 FROM RD_TOLLGATE RT WHERE RT.OUT_LINK_PID ="+rdLaneConnexity.getInLinkPid());
		sb.append(" AND RT.U_RECORD <> 2");
		
		String sql = sb.toString();
		log.info("RdLaneConnexity后检查RDTOLLGATE001--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(!resultList.isEmpty()){
			String target = "[RD_LANE_CONNEXITY," + rdLaneConnexity.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
	}
}
