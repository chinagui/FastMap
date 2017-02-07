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
 * @ClassName GLM18007
 * @author Han Shaoming
 * @date 2017年1月19日 下午5:37:29
 * @Description TODO
 * 语音引导进入link与退出link等级至少有一条应为普通路（非高速或城市高速），否则报log
 * Link种别编辑	服务端后检查
 */
public class GLM18007 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//Link种别编辑
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
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			//Link种别编辑
			if(changedFields.containsKey("kind")){
				int kind = (int) changedFields.get("kind");
				if(kind == 1 || kind == 2){
					StringBuilder sb = new StringBuilder();
					
					sb.append("WITH R AS(");
					sb.append(" SELECT DISTINCT RV.PID PID,"+rdLink.getPid()+" LINK_PID FROM RD_VOICEGUIDE RV");
					sb.append(" WHERE RV.IN_LINK_PID = "+rdLink.getPid()+" AND RV.U_RECORD <>2");
					sb.append(" UNION");
					sb.append(" SELECT DISTINCT RVD.VOICEGUIDE_PID PID,"+rdLink.getPid()+" LINK_PID ");
					sb.append(" FROM RD_VOICEGUIDE_DETAIL RVD WHERE RVD.OUT_LINK_PID = "+rdLink.getPid());
					sb.append(" AND RVD.U_RECORD <>2)");
					sb.append(" SELECT DISTINCT R.LINK_PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL VD,");
					sb.append(" RD_LINK L1, RD_LINK L2,R WHERE V.PID = VD.VOICEGUIDE_PID");
					sb.append(" AND V.PID = R.PID AND V.IN_LINK_PID = L1.LINK_PID AND VD.OUT_LINK_PID = L2.LINK_PID");
					sb.append(" AND L1.KIND IN (1, 2) AND L2.KIND IN (1, 2)");
					sb.append(" AND V.U_RECORD <>2 AND VD.U_RECORD <>2 AND L1.U_RECORD <>2 AND L2.U_RECORD <>2");
					
					String sql = sb.toString();
					log.info("RdLink后检查GLM18007--sql:" + sql);
					
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
