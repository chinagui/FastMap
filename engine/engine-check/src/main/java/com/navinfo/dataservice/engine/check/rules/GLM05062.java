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
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM05062
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description:大路口交叉点模式图中，进入线或退出线如果是单方向，则进入线方向必须为进入该node，退出线方向为退出该node，否则报LOG 
 */
public class GLM05062 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01089.class);

	private Set<Integer> check1 = new HashSet<>();

	public GLM05062() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : check1) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH T AS (SELECT DISTINCT L.S_NODE_PID AS NODEPID, B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_SCHEMATIC BR, RD_LINK L, RD_BRANCH_VIA VIA WHERE B.OUT_LINK_PID = RDLINK_PID ");

			sb.append(linkPid);

			sb.append(
					" AND B.BRANCH_PID = BR.BRANCH_PID AND B.BRANCH_PID = VIA.BRANCH_PID AND VIA.LINK_PID = L.LINK_PID AND B.U_RECORD <> 2 AND BR.U_RECORD <> 2 AND L.U_RECORD <> 2 AND VIA.U_RECORD <> 2 UNION SELECT DISTINCT L.E_NODE_PID AS NODEPID, B.BRANCH_PID FROM RD_BRANCH B, RD_BRANCH_SCHEMATIC BR, RD_LINK L, RD_BRANCH_VIA VIA WHERE B.OUT_LINK_PID = ");

			sb.append(linkPid);
			
			sb.append(
					" AND B.BRANCH_PID = BR.BRANCH_PID AND B.BRANCH_PID = VIA.BRANCH_PID AND VIA.LINK_PID = L.LINK_PID AND B.U_RECORD <> 2 AND BR.U_RECORD <> 2 AND L.U_RECORD <> 2 AND VIA.U_RECORD <> 2)  SELECT 0 AS GEOMETRY, '[RD_BRANCH,' || B.BRANCH_PID || ']' TARGET, 0 AS MESH_ID FROM RD_BRANCH B, RD_BRANCH_SCHEMATIC BR, RD_LINK L WHERE B.OUT_LINK_PID = ");

			sb.append(linkPid);
			
			sb.append(
					"AND B.BRANCH_PID = BR.BRANCH_PID AND B.RELATIONSHIP_TYPE = 2 AND L.LINK_PID = B.OUT_LINK_PID AND B.U_RECORD <> 2 AND BR.U_RECORD <> 2 AND L.U_RECORD <> 2 AND ((L.DIRECT = 2 AND NOT EXISTS (SELECT 1 FROM T WHERE T.NODEPID = L.S_NODE_PID AND B.BRANCH_PID = T.BRANCH_PID)) OR (L.DIRECT = 3 AND NOT EXISTS (SELECT 1 FROM T WHERE T.NODEPID = L.E_NODE_PID AND B.BRANCH_PID = T.BRANCH_PID))) UNION  SELECT 0 AS GEOMETRY, '[RD_BRANCH,' || B.BRANCH_PID || ']' TARGET, 0 AS MESH_ID FROM RD_BRANCH B, RD_BRANCH_SCHEMATIC BR, RD_LINK L WHERE B.BRANCH_PID = BR.BRANCH_PID AND B.IN_LINK_PID =  ");

			sb.append(linkPid);
			
			sb.append(
					"AND L.LINK_PID = B.IN_LINK_PID AND B.U_RECORD <> 2 AND BR.U_RECORD <> 2 AND L.U_RECORD <> 2 AND ((L.DIRECT = 3 AND L.S_NODE_PID != B.NODE_PID) OR (L.DIRECT = 2 AND L.E_NODE_PID != B.NODE_PID)) UNION  SELECT 0 AS GEOMETRY, '[RD_BRANCH,' || B.BRANCH_PID || ']' TARGET, 0 AS MESH_ID FROM RD_BRANCH B, RD_BRANCH_SCHEMATIC BR, RD_LINK L WHERE B.BRANCH_PID = BR.BRANCH_PID AND B.OUT_LINK_PID = ");

			sb.append(linkPid);

			sb.append("AND L.LINK_PID = B.OUT_LINK_PID AND B.RELATIONSHIP_TYPE = 1 AND B.U_RECORD <> 2 AND BR.U_RECORD <> 2 AND L.U_RECORD <> 2 AND ((L.DIRECT = 2 AND L.S_NODE_PID != B.NODE_PID) OR (L.DIRECT = 3 AND L.E_NODE_PID != B.NODE_PID))");
			
			logger.info("RdLink后检查GLM05062 check1-> SQL:" + sb.toString());

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
			if(row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;

				int direct = rdLink.getDirect();

				if (rdLink.status() != ObjStatus.DELETE) {
					if (rdLink.changedFields().containsKey("direct")) {
						direct = (int) rdLink.changedFields().get("direct");
					}
					if(direct == 2 || direct == 3)
					{
						check1.add(rdLink.getPid());
					}
				}
			}
		}
	}

}
