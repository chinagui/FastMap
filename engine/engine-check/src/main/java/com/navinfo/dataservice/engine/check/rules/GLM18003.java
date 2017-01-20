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
 * @ClassName GLM18003
 * @author Han Shaoming
 * @date 2017年1月19日 下午5:18:12
 * @Description TODO
 * 路口语音引导与方向矛盾（包括进入线和退出线），即如果进入线为单向，必须是进入该路口点的，否则报log，
 * 退出线如果是单方向的，必须是退出该路口点的，否则报log；
 * 道路方向编辑	服务端后检查
 */
public class GLM18003 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路方向编辑
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
		//道路方向编辑
		Map<String, Object> changedFields = rdLink.changedFields();
		if(!changedFields.isEmpty()){
			if(changedFields.containsKey("direct")){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT L.LINK_PID FROM RD_VOICEGUIDE V, RD_LINK L");
				sb.append(" WHERE V.IN_LINK_PID = L.LINK_PID");
				sb.append(" AND L.LINK_PID = "+rdLink.getPid());
				sb.append(" AND V.U_RECORD <>2 AND L.U_RECORD <>2");
				sb.append(" AND L.DIRECT = 2 AND L.S_NODE_PID = V.NODE_PID");
				sb.append(" UNION");
				sb.append(" SELECT L.LINK_PID FROM RD_VOICEGUIDE V, RD_LINK L");
				sb.append(" WHERE V.IN_LINK_PID = L.LINK_PID");
				sb.append(" AND L.LINK_PID = "+rdLink.getPid());
				sb.append(" AND V.U_RECORD <>2 AND L.U_RECORD <>2");
				sb.append(" AND L.DIRECT = 3 AND L.E_NODE_PID = V.NODE_PID");
				sb.append(" UNION");
				sb.append(" SELECT L.LINK_PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL D, RD_LINK L");
				sb.append(" WHERE D.VOICEGUIDE_PID = V.PID AND D.OUT_LINK_PID = L.LINK_PID");
				sb.append(" AND L.LINK_PID = "+rdLink.getPid());
				sb.append(" AND V.U_RECORD <>2 AND D.U_RECORD <>2 AND L.U_RECORD <>2");
				sb.append(" AND L.DIRECT = 2 AND L.E_NODE_PID = V.NODE_PID");
				sb.append(" UNION");
				sb.append(" SELECT L.LINK_PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL D, RD_LINK L");
				sb.append(" WHERE D.VOICEGUIDE_PID = V.PID AND D.OUT_LINK_PID = L.LINK_PID");
				sb.append(" AND L.LINK_PID = "+rdLink.getPid());
				sb.append(" AND V.U_RECORD <>2 AND D.U_RECORD <>2 AND L.U_RECORD <>2");
				sb.append(" AND L.DIRECT = 3 AND L.S_NODE_PID = V.NODE_PID");
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM18003--sql:" + sql);
				
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
