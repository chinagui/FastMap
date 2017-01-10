package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53061
 * @author songdongyan
 * @date 2017年1月10日
 * @Description: 等级小于8级的道路link，不能设置RTIC信息
 * 道路种别编辑
 */
public class GLM53061 extends baseRule{

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
		for(IRow obj:checkCommand.getGlmList()){
			//道路种别编辑
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
			if((kind < 8)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LINK_INT_RTIC R");
			sb2.append(" WHERE R.U_RECORD <> 2");
			sb2.append(" AND R.LINK_PID = " + rdLink.getPid());

			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM53014:" + sql2);

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

