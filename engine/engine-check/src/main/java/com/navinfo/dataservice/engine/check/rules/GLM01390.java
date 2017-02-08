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
 * GLM01307 检查对象：具有“停车场出入口连接路”属性的双方向（道路方向为未调查或双方向）道路
 * 检查原则：检查对象应具有“POI连接路”属性，否则报log
 * 
 * @ClassName: GLM01307
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 检查对象应具有“POI连接路”属性，否则报log
 */

public class GLM01390 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01390.class);

	private Set<Integer> modDirectLinkSet = new HashSet<>();

	private Set<Integer> formOf36And53LinkSet = new HashSet<>();

	public GLM01390() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		modDirectLinkSet.addAll(formOf36And53LinkSet);
		for (int linkPid : modDirectLinkSet) {
			logger.info("检查类型：postCheck， 检查规则：GLM01390， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID FROM RD_LINK RL, RD_LINK_FORM RLF WHERE RL.DIRECT in (0, 1) AND RLF.LINK_PID = RL.LINK_PID AND RLF.FORM_OF_WAY = 53 AND RL.U_RECORD != 2 AND RLF.U_RECORD != 2 AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLFTMP WHERE RLFTMP.LINK_PID = RL.LINK_PID AND RLFTMP.U_RECORD != 2 AND RLFTMP.FORM_OF_WAY = 36)");
			
			log.info("RdLink后检查GLM01390 SQL:" + sb.toString());

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
				int direct = rdLink.getDirect();
				if (rdLink.changedFields().containsKey("direct")) {
					direct = (int) rdLink.changedFields().get("direct");
				}
				if (direct == 1 || direct == 0) {
					modDirectLinkSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();
				
				if(form.status() != ObjStatus.DELETE)
				{
					if(form.changedFields().containsKey("formOfWay"))
					{
						formOfWay = (int) form.changedFields().get("formOfWay");
					}
					if (formOfWay == 53) {
						formOf36And53LinkSet.add(form.getLinkPid());
					}
				}
				else{
					//删除停车场属性后该link不需要检查
					if (formOfWay == 53) {
						formOf36And53LinkSet.remove(form.getLinkPid());
					}
					//删除poi连接路，该link之前可能是停车场出入口所以需要检查
					else if(formOfWay == 36)
					{
						formOf36And53LinkSet.add(form.getLinkPid());
					}
				}
			}
		}
	}
}
