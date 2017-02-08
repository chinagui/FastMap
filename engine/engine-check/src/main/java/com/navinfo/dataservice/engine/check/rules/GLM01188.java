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
 * GLM01188
 * 
 * @ClassName: GLM01188
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 一个Node上挂接的特殊交通类型link数大于2时，报log1
 */

public class GLM01188 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01188.class);

	private Set<Integer> checkNodeList = new HashSet<>();

	public GLM01188() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int nodePid : checkNodeList) {
			logger.info("检查类型：postCheck， 检查规则：GLM01188， 检查要素：RDNODE(" + nodePid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT N.GEOMETRY, '[RD_NODE,' || N.NODE_PID || ']' TARGET, M.MESH_ID FROM RD_NODE N, RD_NODE_MESH M WHERE N.NODE_PID = M.NODE_PID AND N.NODE_PID = ");

			sb.append(nodePid);

			sb.append(
					" AND N.U_RECORD <> 2 AND M.U_RECORD <> 2 AND ROWNUM = 1 AND EXISTS (SELECT 1 FROM (SELECT MAX(ROWNUM) NUM FROM RD_LINK L WHERE (L.E_NODE_PID =");
			
			sb.append(nodePid);
			
			sb.append(" OR L.S_NODE_PID = ");
			
			sb.append(nodePid);
			
			sb.append(" ) AND L.SPECIAL_TRAFFIC = 1 AND L.U_RECORD <> 2) WHERE NUM > 1)");
			
			log.info("RdLink后检查GLM01188 SQL:" + sb.toString());

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
				RdLink link = (RdLink) row;
				if (link.status() != ObjStatus.DELETE) {
					int specialTraffic = link.getSpecialTraffic();
					if(link.changedFields().containsKey("specialTraffic"))
					{
						specialTraffic = (int) link.changedFields().get("specialTraffic");
					}
					//特殊交通类型的link
					if(specialTraffic == 1)
					{
						checkNodeList.add(link.getsNodePid());
						
						checkNodeList.add(link.geteNodePid());
					}
				}
			}
		}
	}
}
