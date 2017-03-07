package com.navinfo.dataservice.engine.check.rules;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * GLM23007 电子眼关联link检查
 * 
 * @ClassName: GLM23007
 * @author luyao
 * @date 2017年3月7日 
 * @Description: log1：非机动车道摄像头关联LINK不能是交叉口内LINK、航线；
 *               log2:公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线
 *               log3：电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改
 */

public class GLM23007 extends baseRule {

	public GLM23007() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {

	}
	
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		Set<Integer> eyeKind13 = new HashSet<Integer>();

		Set<Integer> eyeKind15 = new HashSet<Integer>();

		Set<Integer> eyes = new HashSet<Integer>();

		Set<Integer> linkPids = new HashSet<Integer>();

		for (IRow row : checkCommand.getGlmList()) {

			if (row.status() == ObjStatus.DELETE) {
				continue;
			}

			if (row instanceof RdElectroniceye) {

				RdElectroniceye eye = (RdElectroniceye) row;

				int kind = eye.getKind();

				if (eye.changedFields().containsKey("kind")) {

					kind = (int) eye.changedFields().get("kind");
				}

				eyes.add(eye.getPid());

				if (kind == 13) {

					eyeKind13.add(eye.getPid());

				} else if (kind == 15) {

					eyeKind15.add(eye.getPid());
				}
			} else if (row instanceof RdLink) {

				RdLink rdLink = (RdLink) row;

				int kind = rdLink.getKind();

				if (rdLink.changedFields().containsKey("kind")) {

					kind = (int) rdLink.changedFields().get("kind");
				}

				if (kind == 13 || kind == 11 || kind == 10 || kind == 9) {

					linkPids.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkForm) {

				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.changedFields().containsKey("formOfWay")) {

					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (formOfWay == 50) {

					linkPids.add(form.getLinkPid());
				}
			}
		}

		checkEye(eyeKind13, eyeKind15, eyes);

		List<Integer> pids = new ArrayList<Integer>();

		pids.addAll(linkPids);

		checkLink(pids);
	}

	/**
	 * @author luyao
	 * @param rdLink
	 * @throws Exception
	 */
	private void checkEye(Set<Integer> eyeKind13, Set<Integer> eyeKind15,
			Set<Integer> eyes) throws Exception {

		String strFormat = "SELECT Distinct E.PID FROM RD_ELECTRONICEYE E, RD_LINK L, RD_LINK_FORM F WHERE E.PID = {0} AND E.LINK_PID = L.LINK_PID AND (L.KIND IN (11, 13) OR (F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 50 AND F.U_RECORD <> 2)) AND L.U_RECORD <> 2 AND E.U_RECORD <> 2";

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		for (int eyePid : eyeKind13) {

			String sql = MessageFormat.format(strFormat, String.valueOf(eyePid));

			resultList = getObj.exeSelect(this.getConn(), sql);

			for (Object obj : resultList) {

				int pid = Integer.parseInt(String.valueOf(obj));

				String target = "[RD_ELECTRONICEYE," + pid + "]";

				this.setCheckResult("", target, 0,
						"非机动车道摄像头关联LINK不能是交叉口内LINK、航线");

				eyes.remove(eyePid);
			}
		}

		strFormat = "SELECT Distinct E.PID FROM RD_ELECTRONICEYE E, RD_LINK L, RD_LINK_FORM F WHERE E.PID = {0} AND E.LINK_PID = L.LINK_PID AND (L.KIND IN (10,11, 13) OR (F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 50 AND F.U_RECORD <> 2)) AND L.U_RECORD <> 2 AND E.U_RECORD <> 2";

		for (int eyePid : eyeKind15) {

			String sql = MessageFormat.format(strFormat,String.valueOf(eyePid));

			resultList = getObj.exeSelect(this.getConn(), sql);

			for (Object obj : resultList) {

				int pid = Integer.parseInt(String.valueOf(obj));

				String target = "[RD_ELECTRONICEYE," + pid + "]";

				this.setCheckResult("", target, 0,
						"公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线");

				eyes.remove(eyePid);
			}
		}

		strFormat = "SELECT Distinct E.PID FROM RD_ELECTRONICEYE E, RD_LINK L, RD_LINK_FORM F WHERE E.PID = {0} AND E.LINK_PID = L.LINK_PID AND (L.KIND IN (9,10,11, 13) OR (F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 50 AND F.U_RECORD <> 2)) AND L.U_RECORD <> 2 AND E.U_RECORD <> 2";

		for (int eyePid : eyes) {

			String sql = MessageFormat.format(strFormat, String.valueOf(eyePid));

			resultList = getObj.exeSelect(this.getConn(), sql);

			for (Object obj : resultList) {

				int pid = Integer.parseInt(String.valueOf(obj));

				String target = "[RD_ELECTRONICEYE," + pid + "]";

				this.setCheckResult("", target, 0,
						"电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改");
			}
		}
	}

	/**
	 * @author luyao
	 * @param rdLink
	 * @throws Exception
	 */
	private void checkLink(List<Integer> linkPids) throws Exception {

		if (linkPids.isEmpty()) {
			return;
		}

		String ids = StringUtils.getInteStr(linkPids);

		String sql = "SELECT DISTINCT E.LINK_PID FROM RD_ELECTRONICEYE E WHERE E.LINK_PID IN ( "
				+ ids + " ) AND E.U_RECORD <> 2";

		DatabaseOperator getObj = new DatabaseOperator();

		List<Object> resultList = new ArrayList<Object>();

		resultList = getObj.exeSelect(this.getConn(), sql);

		List<Integer> eyeLinkPids = new ArrayList<Integer>();

		for (Object obj : resultList) {

			int pid = Integer.parseInt(String.valueOf(obj));

			eyeLinkPids.add(pid);
		}

		if (eyeLinkPids.isEmpty()) {
			return;
		}

		String strFormat = "SELECT DISTINCT E.PID FROM RD_ELECTRONICEYE E, RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = {0} AND E.LINK_PID = L.LINK_PID  AND E.KIND = 13 AND (L.KIND IN (11, 13) OR (F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 50 AND F.U_RECORD <> 2)) AND L.U_RECORD <> 2 AND E.U_RECORD <> 2";

		for (int linkPid : eyeLinkPids) {
			
			sql = MessageFormat.format(strFormat,  String.valueOf(linkPid));

			String logInfo = "电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改";

			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {

				logInfo = "非机动车道摄像头关联LINK不能是交叉口内LINK、航线";

			} else {

				strFormat = "SELECT DISTINCT E.PID FROM RD_ELECTRONICEYE E, RD_LINK L, RD_LINK_FORM F WHERE L.LINK_PID = {0} AND E.LINK_PID = L.LINK_PID  AND E.KIND = 15 AND (L.KIND IN (10,11, 13) OR (F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 50 AND F.U_RECORD <> 2)) AND L.U_RECORD <> 2 AND E.U_RECORD <> 2";

				sql = MessageFormat.format(strFormat,  String.valueOf(linkPid));

				resultList = getObj.exeSelect(this.getConn(), sql);

				if (!resultList.isEmpty()) {

					logInfo = "公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线";
				}
			}

			String target = "[RD_LINK," + linkPid + "]";

			this.setCheckResult("", target, 0, logInfo);
		}
	}

	

}
