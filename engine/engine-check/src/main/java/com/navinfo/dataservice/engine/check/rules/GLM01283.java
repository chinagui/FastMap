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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01283
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 如果link为双向道路，那么该link上设置的单行限制的“限制方向”不允许为“双向限制”或“未调查”
 */
public class GLM01283 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01283.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01283() {
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
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM RD_LINK_LIMIT LM, RD_LINK L WHERE LM.TYPE = 1 AND LM.U_RECORD <> 2 AND L.U_RECORD <> 2 AND LM.LINK_PID = L.LINK_PID AND L.LINK_PID =");

			sb.append(linkPid);
			
			sb.append(" AND L.DIRECT IN (0, 1) AND LM.LIMIT_DIR IN (0, 1)");

			logger.info("RdLink后检查GLM01283 SQL:" + sb.toString());

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
				int direct = rdLink.getDirect();
				if (rdLink.status() != ObjStatus.DELETE && rdLink.changedFields().containsKey("direct")) {
					direct = (int) rdLink.changedFields().get("direct");
				}
				if (direct == 0 || direct == 1) {
					resultLinkPidSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkLimit) {
				RdLinkLimit rdLinkLimit = (RdLinkLimit) row;

				int limitDirect = rdLinkLimit.getLimitDir();
				
				if(rdLinkLimit.status() != ObjStatus.DELETE)
				{
					if(rdLinkLimit.changedFields().containsKey("limitDir"))
					{
						limitDirect = (int) rdLinkLimit.changedFields().get("limitDir");
					}
					if(limitDirect == 0 || limitDirect == 1)
					{
						resultLinkPidSet.add(rdLinkLimit.getLinkPid());
					}
				}
			}
		}
	}

}
