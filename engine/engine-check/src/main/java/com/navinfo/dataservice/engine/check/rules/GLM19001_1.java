package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM19001_1
 * @author songdongyan
 * @date 2017年1月12日
 * @Description: 非引导道路种别不能作为路口车信的进入Link、退出Link，否则报err
 * Link种别编辑
 */
public class GLM19001_1 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//道路属性编辑
			if(obj instanceof RdLink ){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		boolean checkFlag = false;
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
			if((kind==9)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T");
			sb2.append(" WHERE C.PID = T.CONNEXITY_PID");
			sb2.append(" AND T.RELATIONSHIP_TYPE = 1");
			sb2.append(" AND C.U_RECORD <> 2");
			sb2.append(" AND T.U_RECORD <> 2");
			sb2.append(" AND C.IN_LINK_PID = " + rdLink.getPid());
			sb2.append(" UNION");
			sb2.append("SELECT 1 FROM RD_LANE_TOPOLOGY T");
			sb2.append(" WHERE T.RELATIONSHIP_TYPE = 1");
			sb2.append(" AND T.U_RECORD <> 2");
			sb2.append(" AND T.OUT_LINK_PID = " + rdLink.getPid());
			
			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM19001_2:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}


}
