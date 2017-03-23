package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM01028_6 10级路/步行街/人渡不能是顺行的进入线，退出线，经过线
 * 
 * @ClassName: GLM01028_6
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 10级路/步行街/人渡不能是顺行的进入线，退出线，经过线
 * 新增顺行，修改顺行前检查
 * link种别编辑，道路属性编辑后检查
 */

public class GLM01028_6 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01028_6.class);

	private Set<Integer> formOf20LinkSet = new HashSet<>();

	private Set<Integer> kindOf10And11LinkSet = new HashSet<>();

	public GLM01028_6() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				if(rdDirectroute.status().equals(ObjStatus.INSERT)){
					linkPidSet.add(rdDirectroute.getInLinkPid());
					linkPidSet.add(rdDirectroute.getOutLinkPid());
					
					for(IRow irow:rdDirectroute.getVias()){
						if(irow instanceof RdDirectrouteVia){
							RdDirectrouteVia rdDirectrouteVia = (RdDirectrouteVia)irow;
							linkPidSet.add(rdDirectrouteVia.getLinkPid());
						}
					}
				}
				else if(rdDirectroute.status().equals(ObjStatus.UPDATE)){
					if(rdDirectroute.changedFields().containsKey("inLinkPid")){
						linkPidSet.add(Integer.parseInt(rdDirectroute.changedFields().get("inLinkPid").toString()));
					}
					if(rdDirectroute.changedFields().containsKey("outLinkPid")){
						linkPidSet.add(Integer.parseInt(rdDirectroute.changedFields().get("outLinkPid").toString()));
					}
				}
			}
			else if(row instanceof RdDirectrouteVia){
				RdDirectrouteVia rdDirectrouteVia = (RdDirectrouteVia)row;
				if(rdDirectrouteVia.status().equals(ObjStatus.INSERT)){
					linkPidSet.add(rdDirectrouteVia.getLinkPid());
				}
				else if(rdDirectrouteVia.status().equals(ObjStatus.UPDATE)){
					if(rdDirectrouteVia.changedFields().containsKey("linkPid")){
						linkPidSet.add(Integer.parseInt(rdDirectrouteVia.changedFields().get("linkPid").toString()));
					}
				}
			}
			if(!linkPidSet.isEmpty()){
				check(linkPidSet);
			}
		}
	}

	/**
	 * @param linkPidSet
	 * @throws Exception 
	 */
	private void check(Set<Integer> linkPidSet) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT T.GEOMETRY, '[RD_LINK,' || T.LINK_PID || ']' TARGET, T.MESH_ID");
		sb.append(" FROM RD_LINK T");
		sb.append(" WHERE T.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND T.KIND IN (10,11)");
		sb.append(" UNION ALL");
		sb.append("	SELECT T.GEOMETRY, '[RD_LINK,' || T.LINK_PID || ']' TARGET, T.MESH_ID");
		sb.append(" FROM RD_LINK T, RD_LINK_FORM F");
		sb.append(" WHERE T.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");
		sb.append(" AND T.LINK_PID = F.LINK_PID");
		sb.append(" AND F.FORM_OF_WAY = 20");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND F.U_RECORD <> 2");


		log.info("RdDirectroute前检查GLM01028_6 SQL:" + sb.toString());

		DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sb.toString());

		if (!resultList.isEmpty()) {
			this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),(int) resultList.get(2));
		}

		
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		formOf20LinkSet.addAll(kindOf10And11LinkSet);
		for (int linkPid : formOf20LinkSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM01028_6， 检查要素：RDLINK(" + linkPid + "), 触法时机：LINK种别编辑");

			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT T.GEOMETRY, '[RD_LINK,' || T.LINK_PID || ']' TARGET, T.MESH_ID FROM RD_LINK T WHERE T.link_pid =");

			sb.append(linkPid);

			sb.append(
					" and EXISTS (	SELECT 1 FROM RD_DIRECTROUTE_VIA RT WHERE T.LINK_PID = RT.LINK_PID AND RT.U_RECORD <> 2 UNION ALL 	SELECT 1 FROM RD_DIRECTROUTE R WHERE T.LINK_PID IN (R.IN_LINK_PID, R.OUT_LINK_PID) AND R.U_RECORD <> 2)");

			log.info("RdLink后检查GLM01028_6 SQL:" + sb.toString());

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
				if (kind == 10 || kind == 11) {
					kindOf10And11LinkSet.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;
				int formOfWay = form.getFormOfWay();
				if (row.status() != ObjStatus.DELETE) {
					if (form.changedFields().containsKey("formOfWay")) {
						formOfWay = (int) form.changedFields().get("formOfWay");
					}
					if (formOfWay == 20) {
						formOf20LinkSet.add(form.getLinkPid());
					}
				} else {
					if (formOfWay == 20) {
						formOf20LinkSet.remove(form.getLinkPid());
					}
				}
			}
		}
	}
}
