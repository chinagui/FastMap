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
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM01288 一组线线的所有link中不能有步行街属性的link
 * 
 * @ClassName: GLM01288
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 检查对象：若link的种别为“9级路、“10级路”，或含有“公交车专用道”、“步行街”形态。
 *               检查原则：该link上必须制作时间段为空的穿行限制，且不能制作车辆限制，该限制信息的“赋值方式”必须为“未验证（程序赋值）”，
 *               否则报err
 */

public class GLM01288 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01288.class);

	private Set<Integer> checkLinkSet = new HashSet<>();

	public GLM01288() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkSet) {
			logger.info("检查类型：postCheck， 检查规则：GLM01288， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, L.KIND || '级道路没有永久禁止穿行!' AS LOG FROM RD_LINK L WHERE L.LINK_PID = ");
			sb.append(linkPid);
			sb.append(
					" AND L.U_RECORD <> 2 AND L.KIND IN (9, 10) AND NOT EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 3 AND LM.U_RECORD <> 2 AND LM.TIME_DOMAIN IS NULL AND L.LINK_PID = LM.LINK_PID)   ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, L.KIND || '级道路不能制作车辆限制!' AS LOG FROM RD_LINK L WHERE L.LINK_PID = ");
			sb.append(linkPid);
			sb.append(
					" AND L.KIND IN (9, 10) AND L.U_RECORD <> 2 AND EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 2 AND LM.U_RECORD <> 2 AND L.LINK_PID = LM.LINK_PID)  ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '步行街道路没有永久禁止穿行!' FROM RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = ");
			sb.append(linkPid);

			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 20 AND NOT EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 3 AND LM.U_RECORD <> 2 AND LM.TIME_DOMAIN IS NULL AND LM.LINK_PID = F.LINK_PID)  ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '步行街道路不能制作车辆限制!' FROM RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = ");
			sb.append(linkPid);
			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 20 AND EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 2 AND LM.U_RECORD <> 2 AND LM.LINK_PID = F.LINK_PID) ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '公交车专用道道路没有永久禁止穿行!' FROM RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = ");
			sb.append(linkPid);
			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 22 AND NOT EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 3 AND LM.U_RECORD <> 2 AND LM.TIME_DOMAIN IS NULL AND LM.LINK_PID = F.LINK_PID)  ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '公交车专用道道路不能制作车辆限制!' FROM RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = ");
			sb.append(linkPid);

			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 22 AND EXISTS (SELECT 1 FROM RD_LINK_LIMIT LM WHERE LM.TYPE = 2 AND LM.U_RECORD <> 2 AND LM.LINK_PID = F.LINK_PID) ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, L.KIND || '级路的“禁止穿行标识”必须为“未验证（程序赋值）”' FROM RD_LINK L, RD_LINK_LIMIT LM WHERE L.LINK_PID = ");
			sb.append(linkPid);

			sb.append(
					" AND L.LINK_PID = LM.LINK_PID AND L.U_RECORD <> 2 AND LM.U_RECORD <> 2 AND L.KIND IN (9, 10) AND LM.TYPE = 3 AND LM.TIME_DOMAIN IS NULL AND LM.PROCESS_FLAG <> 2 ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '步行街的“禁止穿行标识”必须为“未验证（程序赋值）”' FROM RD_LINK L, RD_LINK_FORM F, RD_LINK_LIMIT LM WHERE L.LINK_PID = ");
			sb.append(linkPid);
			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND LM.U_RECORD <> 2 AND F.LINK_PID = LM.LINK_PID AND L.LINK_PID = LM.LINK_PID AND F.FORM_OF_WAY = 20 AND LM.TYPE = 3 AND LM.TIME_DOMAIN IS NULL AND LM.PROCESS_FLAG <> 2  ");

			sb.append(
					" UNION ALL  SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID, '公交车专用道的“禁止穿行标识”必须为“未验证（程序赋值）”' FROM RD_LINK L, RD_LINK_FORM F, RD_LINK_LIMIT LM WHERE L.LINK_PID = ");
			sb.append(linkPid);

			sb.append(
					" AND F.LINK_PID = L.LINK_PID AND L.U_RECORD <> 2 AND F.U_RECORD <> 2 AND LM.U_RECORD <> 2 AND L.LINK_PID = LM.LINK_PID AND F.LINK_PID = LM.LINK_PID AND F.FORM_OF_WAY = 22 AND LM.TYPE = 3 AND LM.TIME_DOMAIN IS NULL AND LM.PROCESS_FLAG <> 2  ");

			sb.append(linkPid);

			log.info("RdLink后检查GLM01288 SQL:" + sb.toString());

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

				int kind = rdLink.getKind();

				if (row.status() == ObjStatus.UPDATE && row.changedFields().containsKey("kind")) {
					kind = (int) row.changedFields().get("kind");

					if (kind == 9 || kind == 10) {
						checkLinkSet.add(rdLink.getPid());
					}
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() != ObjStatus.DELETE) {
					if (form.changedFields().containsKey("formOfWay")) {
						formOfWay = (int) form.changedFields().get("formOfWay");
					}

					if (formOfWay == 20 || formOfWay == 22) {
						checkLinkSet.add(form.getLinkPid());
					}
				}
			} else if (row instanceof RdLinkLimit) {
				RdLinkLimit limit = (RdLinkLimit) row;
				checkLinkSet.add(limit.getLinkPid());
			}
		}
	}
}
