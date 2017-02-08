package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.crosswalk.RdCrosswalk;
import com.navinfo.dataservice.dao.glm.model.rd.crosswalk.RdCrosswalkInfo;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * PERMIT_CHECK_RDCROSSWALK_TENLEVEL 10级路不允许制作人行过道
 * 
 * @ClassName: GLM01307
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 10级路不允许制作人行过道
 */

public class PERMIT_CHECK_RDCROSSWALK_TENLEVEL extends baseRule {

	private static Logger logger = Logger.getLogger(PERMIT_CHECK_RDCROSSWALK_TENLEVEL.class);

	/**
	 * 10级路linkPid,人行过道linkPid
	 */
	private Set<Integer> checkLinkPidSet = new HashSet<>();

	public PERMIT_CHECK_RDCROSSWALK_TENLEVEL() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : checkLinkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：PERMIT_CHECK_RDCROSSWALK_TENLEVEL， 检查要素：RDLINK(" + linkPid + ")");

			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID ");
			
			sb.append(" FROM RD_LINK R, RD_CROSSWALK_INFO RCI WHERE R.LINK_PID = RCI.LINK_PID AND R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND R.KIND = 10 AND R.U_RECORD <> 2 AND RCI.U_RECORD <> 2 ");
			
			log.info("RdLink后检查PERMIT_CHECK_RDCROSSWALK_TENLEVEL SQL:" + sb.toString());
			
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
			if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
				RdLink rdLink = (RdLink) row;
				int kind = rdLink.getKind();
				if (rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				if (kind == 10) {
					checkLinkPidSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdCrosswalk && row.status() != ObjStatus.DELETE) {
				RdCrosswalk crosswalk = (RdCrosswalk) row;
				
				List<IRow> infoRows = crosswalk.getInfos();
				
				for(IRow info : infoRows)
				{
					RdCrosswalkInfo crosswalkInfo = (RdCrosswalkInfo) info;
					checkLinkPidSet.add(crosswalkInfo.getLinkPid());
				}
			}
		}
	}
}
