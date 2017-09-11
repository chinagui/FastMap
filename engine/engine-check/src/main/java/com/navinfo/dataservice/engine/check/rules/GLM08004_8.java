package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

public class GLM08004_8 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction);
			} else if (obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail);
			} else if (obj instanceof RdRestrictionVia) {
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) obj;
				checkRdRestrictionVia(rdRestrictionVia);
			}
		}
	}

	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		if (rdRestrictionDetail.status().equals(ObjStatus.DELETE)) {
			return;
		}

		int outlinkpid = rdRestrictionDetail.getOutLinkPid();
		List<Integer> linkPids = new ArrayList<Integer>();
		
		if (rdRestrictionDetail.changedFields().containsKey("outLinkPid")) {
			outlinkpid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
		}
		
		linkPids.add(outlinkpid);
		checkRdLink(linkPids, rdRestrictionDetail.getRestricPid());
	}

	private void checkRdRestriction(RdRestriction rdRestriction) throws Exception {
		if (rdRestriction.status().equals(ObjStatus.INSERT)) {
			int restricPid = rdRestriction.getPid();
			
			List<Integer> linkPids = new ArrayList<Integer>();
			linkPids.add(rdRestriction.getInLinkPid());
			
			List<IRow> details = rdRestriction.getDetails();
			
			for (IRow temp : details) {
				RdRestrictionDetail restrictionDetail = (RdRestrictionDetail) temp;
				linkPids.add(restrictionDetail.getOutLinkPid());

				List<IRow> vias = restrictionDetail.getVias();
				for (IRow viarow : vias) {
					RdRestrictionVia via = (RdRestrictionVia) viarow;
					if (isIntersectLink(via.getLinkPid()) == true && restrictionDetail.getType() ==2) {
						continue;
					}
					linkPids.add(via.getLinkPid());
				}
			}
			checkRdLink(linkPids, restricPid);
		}
	}

	private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia) throws Exception {
		List<Integer> linkPids = new ArrayList<>();

		if (rdRestrictionVia.status() == ObjStatus.DELETE) {
			return;
		}
		int linkpid = rdRestrictionVia.getLinkPid();

		if (rdRestrictionVia.changedFields().containsKey("linkPid")) {
			linkpid = (int) rdRestrictionVia.changedFields().get("linkPid");
		}
		
		RdRestrictionSelector restricSelector = new RdRestrictionSelector(this.getConn());
		List<RdRestriction> restriction = restricSelector.loadByLink(linkpid, 3, true);

		for (RdRestriction res : restriction) {
			List<IRow> details = res.getDetails();
			
			for(IRow row:details){
				RdRestrictionDetail detail = (RdRestrictionDetail)row;
				
				if(isIntersectLink(linkpid) == true && detail.getType() == 2){
					continue;
				}
				
				linkPids.add(linkpid);
				checkRdLink(linkPids, res.getPid());
			}//for
		}//for
	}

	private void checkRdLink(List<Integer> linkPids, int restricPid) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1");
		sb.append(" FROM rd_link_limit l");
		sb.append(" WHERE l.time_domain IS NULL");
		sb.append(" AND l.vehicle=2147483786");
		sb.append(" AND l.type=3");
		sb.append(" AND l.link_pid IN (" + StringUtils.join(linkPids, ",") + ")");
		// sb.append(" AND NOT EXISTS (select 1 from rd_cross_link c where
		// c.link_pid=l.link_pid and c.u_record <> 2)");
		sb.append(" AND l.u_record <> 2");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM08004_8:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (resultList.size() > 0) {
			String target = "[RD_RESTRUCTION," + restricPid + "]";
			this.setCheckResult("", target, 0);
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdLinkLimit) {
				RdLinkLimit rdLinkLimit = (RdLinkLimit) obj;
				checkRdLinkLimit(rdLinkLimit);
			}
		}
	}

	private boolean isIntersectLink(int linkpid) throws Exception {
		boolean res = false;

		RdLinkSelector selector = new RdLinkSelector(this.getConn());
		RdLink link = (RdLink) selector.loadById(linkpid, true);
		List<IRow> forms = link.getForms();

		for (IRow formrow : forms) {
			RdLinkForm form = (RdLinkForm) formrow;
			if (form.getFormOfWay() == 50) {
				res = true;
				break;
			}
		}

		return res;
	}

	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		boolean checkFlg = false;
		if (rdLinkLimit.status().equals(ObjStatus.INSERT)) {
			if ((rdLinkLimit.getType() == 3) && (rdLinkLimit.getVehicle() == 2147483786L)
					&& (rdLinkLimit.getTimeDomain().isEmpty())) {
				checkFlg = true;
			}
		} else if (rdLinkLimit.status().equals(ObjStatus.UPDATE)) {
			int type;
			long vehicle;
			String timeDomain;

			if (rdLinkLimit.changedFields().containsKey("type")) {
				type = Integer.parseInt(rdLinkLimit.changedFields().get("type").toString());
			} else {
				type = rdLinkLimit.getType();
			}

			if (rdLinkLimit.changedFields().containsKey("vehicle")) {
				vehicle = Long.parseLong(rdLinkLimit.changedFields().get("vehicle").toString());
			} else {
				vehicle = rdLinkLimit.getVehicle();
			}

			if (rdLinkLimit.changedFields().containsKey("timeDomain")) {
				if (rdLinkLimit.changedFields().get("timeDomain") != null) {
					timeDomain = rdLinkLimit.changedFields().get("timeDomain").toString();
					if (timeDomain.isEmpty()) {
						timeDomain = null;
					}
				} else {
					timeDomain = null;
				}
			} else {
				timeDomain = rdLinkLimit.getTimeDomain();
				if (timeDomain == null || timeDomain.isEmpty()) {
					timeDomain = null;
				}
			}
			if ((type == 3) && (vehicle == 2147483786L) && (timeDomain == null)) {
				checkFlg = true;
			}
		}
		if (checkFlg) {
			int linkPid = rdLinkLimit.getLinkPid();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT 1");
			sb.append(" FROM rd_restriction r,rd_restriction_detail d,rd_restriction_via v");
			sb.append(" WHERE r.pid=d.restric_pid and d.detail_id = v.detail_id");
			sb.append(" AND (r.in_link_pid = " + linkPid + " or d.out_link_pid = " + linkPid + " or v.link_pid = "
					+ linkPid + ")");
			// sb.append(" AND NOT EXISTS (select 1 from rd_cross_link c where
			// c.link_pid=v.link_pid and c.u_record <> 2)");
			// sb.append(" AND NOT EXISTS (select 1 from rd_cross_link c where
			// c.link_pid=d.out_link_pid and c.u_record <> 2)");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM f WHERE f.link_pid = v.link_pid AND f.u_record <> 2"
					+ " AND f.FORM_OF_WAY = 50 and d.RELATIONSHIP_TYPE = 2)");
			sb.append(" AND r.u_record <> 2");
			sb.append(" AND d.u_record <> 2");
			sb.append(" AND v.u_record <> 2");

			String sql = sb.toString();
			log.info("RdLinkLimit后检查GLM08004_8:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (resultList.size() > 0) {
				String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}
}
