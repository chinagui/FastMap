package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM03079
 * @author Han Shaoming
 * @date 2017年1月19日 下午4:04:15
 * @Description TODO
 * 一个点上挂接的Link中，开发状态既有详细开发又有未验证时，报log
 * 开发状态编辑	服务端后检查
 */
public class GLM03079 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//开发状态编辑
			if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		//开发状态编辑
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			if(changedFields.containsKey("developState")){
				int developState = Integer.parseInt((String) changedFields.get("developState"));
				if(developState == 1 || developState == 2){
					StringBuilder sb = new StringBuilder();
					
					sb.append("WITH T AS(");
					sb.append(" SELECT R.S_NODE_PID NODE_PID,R.LINK_PID FROM RD_LINK R");
					sb.append(" WHERE R.LINK_PID =220003225 AND R.U_RECORD <>2");
					sb.append(" UNION");
					sb.append(" SELECT R.E_NODE_PID NODE_PID,R.LINK_PID FROM RD_LINK R");
					sb.append(" WHERE R.LINK_PID =220003225 AND R.U_RECORD <>2)");
					sb.append(" SELECT DISTINCT T.LINK_PID FROM RD_LINK R1,RD_LINK R2,T");
					sb.append(" WHERE R1.U_RECORD <>2 AND R2.U_RECORD <>2 AND R1.LINK_PID <> R2.LINK_PID");
					sb.append(" AND (R1.S_NODE_PID=T.NODE_PID OR R1.E_NODE_PID=T.NODE_PID)");
					sb.append(" AND (R2.S_NODE_PID=T.NODE_PID OR R2.E_NODE_PID=T.NODE_PID)");
					sb.append(" AND R1.DEVELOP_STATE=1 AND R2.DEVELOP_STATE=2");
					
					String sql = sb.toString();
					log.info("RdLink后检查GLM03079--sql:" + sql);
					
					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);
					
					if(!resultList.isEmpty()){
						String target = "[RD_LINK," + rdLink.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
	}
}
