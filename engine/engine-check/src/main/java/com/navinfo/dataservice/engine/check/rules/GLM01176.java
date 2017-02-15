package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01176
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: IC以外的高速或城高不能挂接普通道路（步行道路种别不检查）
 */
public class GLM01176 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01176.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();
	
	public GLM01176() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : resultLinkPidSet) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK L, RD_LINK L2 WHERE L.KIND IN (1, 2) AND L.U_RECORD <> 2 AND L2.U_RECORD <> 2 AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.LINK_PID = L.LINK_PID AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 10) AND (L.S_NODE_PID = L2.S_NODE_PID OR L.S_NODE_PID = L2.E_NODE_PID OR L.E_NODE_PID = L2.S_NODE_PID OR L.E_NODE_PID = L2.E_NODE_PID) AND L.LINK_PID <> L2.LINK_PID AND L2.KIND NOT IN (1, 2) AND L2.KIND <> 10 AND L.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" UNION ALL SELECT L2.GEOMETRY, '[RD_LINK,' || L2.LINK_PID || ']' TARGET, L2.MESH_ID FROM RD_LINK L, RD_LINK L2 WHERE L.KIND IN (1, 2) AND L.U_RECORD <> 2 AND L2.U_RECORD <> 2 AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 10 AND F.U_RECORD <> 2) AND (L.S_NODE_PID = L2.S_NODE_PID OR L.S_NODE_PID = L2.E_NODE_PID OR L.E_NODE_PID = L2.S_NODE_PID OR L.E_NODE_PID = L2.E_NODE_PID) AND L.LINK_PID <> L2.LINK_PID AND L2.KIND NOT IN (1, 2) AND L2.KIND <> 10 AND L2.LINK_PID = ");
			
			sb.append(linkPid);
			
			log.info("RdLink后检查GLM01176 SQL:" + sb.toString());
			
			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
	}

	/**
	 * @param checkCommand
	 * @throws Exception
	 */
	private void prepareData(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink) {
				
				RdLink rdLink = (RdLink) row;

				if (rdLink.status() == ObjStatus.UPDATE && rdLink.changedFields().containsKey("kind")) {
					resultLinkPidSet.add(rdLink.getPid());
				}
				if (row instanceof RdLinkForm) {
					RdLinkForm form = (RdLinkForm) row;
					if (form.status() != ObjStatus.DELETE) {
						resultLinkPidSet.add(form.getLinkPid());
					}
				}
			}
		}
	}

}
