package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;

import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: GLM10009_1
 * @author zhaokaikai
 * @date 2017年1月11
 * @Description: 制作坡度的link和延长LINK不能为作业中道路，步行道路，轮渡，人渡，私道，非引导道路
 */
public class GLM10009_1 extends baseRule {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo
	 * .dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo
	 * .dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 修改RdLink触发
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				this.checkRdLinkKind(rdLink);
			}
			// RdLinkForm新增修改会触发
			else if (obj instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				this.checkRdLinkForm(rdLinkForm);
			}
		}
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		// 新增RdLinkForm
		if (rdLinkForm.status().equals(ObjStatus.INSERT)) {
			int formOfWay = rdLinkForm.getFormOfWay();
			// 私道触发检查
			if (formOfWay == 18) {
				checkFlg = true;
			}
		}
		// 修改RdLinkName类型
		else if (rdLinkForm.status().equals(ObjStatus.UPDATE)) {
			if (rdLinkForm.changedFields().containsKey("formOfWay")) {
				if (rdLinkForm.getFormOfWay() != 18) {
					int formOfWay = Integer.parseInt(rdLinkForm.changedFields()
							.get("formOfWay").toString());
					if (formOfWay == 18) {
						checkFlg = true;
					}
				}
			}
		}

		if (checkFlg) {

			checkRdLink(rdLinkForm.getLinkPid());
		}

	}

	/**
	 * @param rdLinkForm
	 * @throws Exception
	 */
	private void checkRdLinkKind(RdLink rdLink) throws Exception {
		boolean checkFlg = false;

		if (rdLink.changedFields() != null) {
			if (rdLink.changedFields().containsKey("kind")) {
				if ((int) rdLink.changedFields().get("kind") >= 9) {
					checkFlg = true;
				}
			}
		}

		// 修改RdLink
		if (rdLink.status().equals(ObjStatus.UPDATE)) {
			if (rdLink.changedFields().containsKey("kind")) {
				if (rdLink.getKind() < 9 || rdLink.getKind() != 0) {
					int kind = Integer.parseInt(rdLink.changedFields()
							.get("kind").toString());
					if (kind >= 9 || kind == 0) {
						checkFlg = true;
					}
				}

			}
		}
		// 执行检查
		if (checkFlg) {

			checkRdLink(rdLink.getPid());
		}

	}

	/**
	 * @param linkPid
	 * @throws Exception
	 */
	private void checkRdLink(int linkPid) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT L.LINK_PID ");
		sb.append("  FROM RD_SLOPE S, RD_LINK L ");
		sb.append(" WHERE S.U_RECORD != 2 ");
		sb.append(" AND L.U_RECORD != 2 AND S.LINK_PID = L.LINK_PID AND AND L.LINK_PID = "
				+ linkPid);
		sb.append(" UNION ALL ");
		sb.append(" SELECT L.LINK_PID ");
		sb.append(" FROM RD_SLOPE_VIA V, RD_LINK L ");
		sb.append(" WHERE     L.U_RECORD != 2 ");
		sb.append(" AND V.LINK_PID = L.LINK_PID  ");
		sb.append(" AND L.LINK_PID = " + linkPid);
		String sql = sb.toString();
		log.info("RdSlope后检查GLM010009_1:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (Integer.parseInt(resultList.get(0).toString()) != 0) {
			String target = "[RD_LINK," + linkPid + "]";
			this.setCheckResult("", target, 0);
		}

	}
}
