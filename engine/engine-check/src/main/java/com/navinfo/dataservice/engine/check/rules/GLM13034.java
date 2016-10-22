package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM13034
 * @author zhangxiaoyi
 * @date 2016年8月18日
 * @Description: 
 * 收费站	word	GLM13034	后台	
 * 检查对象:RD_LINK
 * 检查原则：如果一个收费站的退出link与另一个收费站（主点不同）的进入link为同一link，则报log
 * */
public class GLM13034 extends baseRule {

	/**
	 * 
	 */
	public GLM13034() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
		for(IRow obj:checkCommand.getGlmList()){
			//收费站 create
			if(obj instanceof RdTollgate ){
				RdTollgate rdTollgate = (RdTollgate)obj;
				int outLinkPid=rdTollgate.getOutLinkPid();
				int inLinkPid=rdTollgate.getInLinkPid();
				int nodePid=rdTollgate.getNodePid();
				String sql="SELECT 1"
						+ "  FROM RD_TOLLGATE T"
						+ " WHERE T.NODE_PID <> "+nodePid
						+ "  AND T.U_RECORD != 2 AND (T.IN_LINK_PID = "+outLinkPid+" OR T.OUT_LINK_PID = "+inLinkPid+")";
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
