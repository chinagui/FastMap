package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RdTollgate001
 * @author zhangxiaoyi
 * @date 2016年8月18日
 * @Description: 
 * 收费站	word	RDTOLLGATE001	后台	收费站的退出线不能与车信的进入线是同一条link
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
		
		for(IRow obj:checkCommand.getGlmList()){
			//减速带 create
			if(obj instanceof RdTollgate ){
				RdTollgate rdTollgate = (RdTollgate)obj;
				int outLinkPid=rdTollgate.getOutLinkPid();
				String sql="SELECT 1 FROM RD_LANE_CONNEXITY WHERE U_RECORD != 2 AND IN_LINK_PID = "+outLinkPid;
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				if(resultList.size()>0){
					this.setCheckResult("", "", 0);
					return;
				}
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

}
