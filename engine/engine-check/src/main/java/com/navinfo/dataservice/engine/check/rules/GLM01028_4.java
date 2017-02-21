package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01028_4
 * @author songdongyan
 * @date 2016年12月28日
 * @Description: 10级路/步行街/人渡不能是交限的进入线，退出线，经过线。 新增交限/卡车交限：RdRestriction
 *               修改交限/卡车交限：RdRestrictionDetail(新增，修改outLinkPid),RdRestrictionVia
 *               (新增，修改LinkPid)
 */
public class GLM01028_4 extends baseRule {

	private Set<Integer> linkPidSet = new HashSet<>();

	private static Logger logger = Logger.getLogger(GLM01008.class);

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增交限/卡车交限
			if (obj instanceof RdRestriction) {
				RdRestriction restriObj = (RdRestriction) obj;
				if (restriObj.status().equals(ObjStatus.INSERT)) {
					checkRdRestriction(restriObj);
				}
			}
			// 修改交限/卡车交限
			else if (obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail, checkCommand);
			}
			// 修改交限/卡车交限
			else if (obj instanceof RdRestrictionVia) {
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) obj;
				checkRdRestrictionVia(rdRestrictionVia, checkCommand);
			}
		}
	}

	/**
	 * @param rdRestrictionVia
	 * @param checkCommand
	 * @throws Exception
	 */
	private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia, CheckCommand checkCommand) throws Exception {
		int linkPid = 0;
		// 新增的经过线
		if (rdRestrictionVia.status().equals(ObjStatus.INSERT)) {
			linkPid = rdRestrictionVia.getLinkPid();
		}
		// 修改linkPid的经过线
		else if (rdRestrictionVia.status().equals(ObjStatus.UPDATE)) {
			if (rdRestrictionVia.changedFields().containsKey("linkPid")) {
				linkPid = Integer.parseInt(rdRestrictionVia.changedFields().get("linkPid").toString());
			}
		}
		if (linkPid != 0) {
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID =" + linkPid);
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.LINK_PID =" + linkPid);

			String sql2 = sb.toString();
			log.info("RdRestrictionVia前检查GLM01028_4:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if (resultList.size() > 0) {
				this.setCheckResult("", "", 0);
			}
		}

	}

	/**
	 * @param rdRestrictionDetail
	 * @param checkCommand
	 * @throws Exception
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, CheckCommand checkCommand)
			throws Exception {
		Set<Integer> linkPids = new HashSet<Integer>();
		if (rdRestrictionDetail.status().equals(ObjStatus.INSERT)) {
			linkPids.add(rdRestrictionDetail.getOutLinkPid());
			for (IRow rdRestrictionViaObj : rdRestrictionDetail.getVias()) {
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) rdRestrictionViaObj;
				linkPids.add(rdRestrictionVia.getLinkPid());
			}
		} else if (rdRestrictionDetail.status().equals(ObjStatus.UPDATE)) {
			if (rdRestrictionDetail.changedFields().containsKey("outLinkPid")) {
				int outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
				linkPids.add(outLinkPid);
			}
		}

		if (!linkPids.isEmpty()) {
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");

			String sql = sb.toString();
			log.info("RdRestrictionDetail前检查GLM01028_4:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (resultList.size() > 0) {
				this.setCheckResult("", "", 0);
			}
		}
	}

	/**
	 * @param restriObj
	 * @throws Exception
	 */
	private void checkRdRestriction(RdRestriction restriObj) throws Exception {
		// 进入线与退出线与经过线
		Set<Integer> linkPids = new HashSet<Integer>();

		linkPids.add(restriObj.getInLinkPid());
		for (IRow objTmp : restriObj.getDetails()) {
			RdRestrictionDetail detailObj = (RdRestrictionDetail) objTmp;
			linkPids.add(detailObj.getOutLinkPid());
			for (IRow rdRestrictionViaObj : detailObj.getVias()) {
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) rdRestrictionViaObj;
				linkPids.add(rdRestrictionVia.getLinkPid());

			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
		sb.append(" AND F.U_RECORD <> 2");
		sb.append(" AND F.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");
		sb.append(" UNION");
		sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(), ",") + ")");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM01028_4:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			this.setCheckResult("", "", 0);
		}

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		preparedData(checkCommand);

		for (int linkPid : linkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM01028_4， 检查要素：RDLINK(" + linkPid + "), 触法时机：LINK种别编辑");

			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH T AS (SELECT R.LINK_PID, R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R WHERE R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND R.u_record!=2 AND (R.KIND IN (10, 11) OR EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE R.LINK_PID = F.LINK_PID AND F.u_record!=2 AND F.FORM_OF_WAY = 20))) ");

			sb.append(
					" SELECT T.GEOMETRY, T.TARGET, T.MESH_ID FROM T WHERE EXISTS (SELECT 1 FROM RD_RESTRICTION_VIA RT WHERE T.LINK_PID = RT.LINK_PID AND RT.u_record!=2 UNION ALL SELECT 1 FROM RD_RESTRICTION R WHERE R.IN_LINK_PID = t.LINK_PID AND R.u_record!=2 UNION ALL SELECT 1 FROM RD_RESTRICTION_DETAIL D WHERE D.OUT_LINK_PID = t.LINK_PID AND D.u_record!=2) ");
			
			log.info("RdLink后检查GLM01028_4 SQL:" + sb.toString());
			
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
	 */
	private void preparedData(CheckCommand checkCommand) {
		for (IRow row : checkCommand.getGlmList()) {
			if (row.status() != ObjStatus.DELETE) {
				if (row instanceof RdLink) {
					RdLink link = (RdLink) row;

					int kind = link.getKind();

					if (link.changedFields().containsKey("kind")) {
						kind = (int) link.changedFields().get("kind");
					}
					if (kind == 10 || kind == 11) {
						linkPidSet.add(link.getPid());
					}
				} else if (row instanceof RdLinkForm) {
					RdLinkForm form = (RdLinkForm) row;

					int formOfWay = form.getFormOfWay();

					if (form.changedFields().containsKey("formOfWay")) {
						formOfWay = (int) form.changedFields().get("formOfWay");
					}
					if (formOfWay == 20) {
						linkPidSet.add(form.getLinkPid());
					}
				}
			}
		}
	}

}
