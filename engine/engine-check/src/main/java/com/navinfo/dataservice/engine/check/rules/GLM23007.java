package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * GLM01514 窄道路的车道数等级只能为1
 * 
 * @ClassName: GLM01514
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: log1：非机动车道摄像头关联LINK不能是交叉口内LINK、航线；
 *               log2:公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线
 *               log3：电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改
 */

public class GLM23007 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM23007.class);

	/**
	 * 航线linkPid
	 */
	private Set<Integer> kind11And13LinkPidSet = new HashSet<>();

	/**
	 * 9级路linkPid
	 */
	private Set<Integer> kindOf9LinkPidSet = new HashSet<>();

	/**
	 * 10级路linkPid
	 */
	private Set<Integer> kindOf10LinkPidSet = new HashSet<>();

	/**
	 * 交叉口内linkPid
	 */
	private Set<Integer> crossLinkKindPidSet = new HashSet<>();

	/**
	 * 非机动车道摄像头关联的linkPid
	 */
	private Set<Integer> kindOf13LinkPidSet = new HashSet<>();

	/**
	 * 公交车道摄像头关联的linkPid
	 */
	private Set<Integer> kindOf15LinkPidSet = new HashSet<>();

	/**
	 * 电子眼关联的linkPid
	 */
	private Set<Integer> kindNot13And15LinkPidSet = new HashSet<>();

	public GLM23007() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		// 非机动车道摄像头关联LINK不能是交叉口内LINK、航线
		kindOf13LinkPidSet.addAll(crossLinkKindPidSet);
		kindOf13LinkPidSet.addAll(kind11And13LinkPidSet);
		for (Integer linkPid : kindOf13LinkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM23007-log1， 检查要素：RDLINK(" + linkPid + ")");
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID, '非机动车道摄像头关联LINK不能是交叉口内LINK、航线'LOG");
			sb.append(" FROM RD_LINK R, RD_ELECTRONICEYE RE ");
			sb.append(" WHERE R.LINK_PID = " + linkPid);
			sb.append(" AND R.LINK_PID = RE.LINK_PID ");
			sb.append(" AND R.u_record!=2 ");
			sb.append(" AND RE.u_record!=2 ");
			sb.append(" AND RE.KIND = 13 ");
			sb.append(" AND ((EXISTS(SELECT 1 FROM RD_LINK_FORM RF WHERE R.LINK_PID = RF.LINK_PID AND RF.FORM_OF_WAY = 50 AND RF.u_record！=2)) OR (R.KIND IN (11,13)))");

			log.info("GLM23007-log1后检查SQL："+sb.toString());
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (resultList.size() > 0) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
		//公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线
		kindOf15LinkPidSet.addAll(crossLinkKindPidSet);
		kindOf15LinkPidSet.addAll(kindOf10LinkPidSet);
		kindOf15LinkPidSet.addAll(kind11And13LinkPidSet);
		for (Integer linkPid : kindOf15LinkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM23007-log2， 检查要素：RDLINK(" + linkPid + ")");
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID, '公交车道摄像头关联LINK不能是交叉口内LINK、10级路、航线'LOG");
			sb.append(" FROM RD_LINK R, RD_ELECTRONICEYE RE ");
			sb.append(" WHERE R.LINK_PID = " + linkPid);
			sb.append(" AND R.LINK_PID = RE.LINK_PID ");
			sb.append(" AND R.u_record!=2 ");
			sb.append(" AND RE.u_record!=2 ");
			sb.append(" AND RE.KIND = 15 ");
			sb.append(" AND ((EXISTS(SELECT 1 FROM RD_LINK_FORM RF WHERE R.LINK_PID = RF.LINK_PID AND RF.FORM_OF_WAY = 50 AND RF.u_record！=2)) OR (R.KIND IN (10,11,13)))");

			log.info("GLM23007-log2后检查SQL："+sb.toString());
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (resultList.size() > 0) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2));
			}
		}
		//电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改
		kindNot13And15LinkPidSet.addAll(crossLinkKindPidSet);
		kindNot13And15LinkPidSet.addAll(kind11And13LinkPidSet);
		kindNot13And15LinkPidSet.addAll(kindOf10LinkPidSet);
		kindNot13And15LinkPidSet.addAll(kindOf9LinkPidSet);
		for (Integer linkPid : kindOf15LinkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM23007-log3， 检查要素：RDLINK(" + linkPid + ")");
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID, '电子眼关联的link不能是交叉口内link，9级路，10级路及航线，请修改'LOG");
			sb.append(" FROM RD_LINK R, RD_ELECTRONICEYE RE ");
			sb.append(" WHERE R.LINK_PID = " + linkPid);
			sb.append(" AND R.LINK_PID = RE.LINK_PID ");
			sb.append(" AND R.u_record!=2 ");
			sb.append(" AND RE.u_record!=2 ");
			sb.append(" AND RE.KIND NOT IN (13,15) ");
			sb.append(" AND ((EXISTS(SELECT 1 FROM RD_LINK_FORM RF WHERE R.LINK_PID = RF.LINK_PID AND RF.FORM_OF_WAY = 50 AND RF.u_record！=2)) OR (R.KIND IN (9,10,11,13)))");

			log.info("GLM23007-log3后检查SQL："+sb.toString());
			
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (resultList.size() > 0) {
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
		AbstractSelector selector = new AbstractSelector(RdLink.class, getConn());
		for (IRow row : checkCommand.getGlmList()) {
			if (row instanceof RdLink) {
				RdLink rdLink = (RdLink) row;
				int kind = rdLink.getKind();
				if (rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				if (kind == 13 || kind == 11) {
					kind11And13LinkPidSet.add(rdLink.getPid());
				} else if (kind == 10) {
					kindOf10LinkPidSet.add(rdLink.getPid());
				} else if (kind == 9) {
					kindOf9LinkPidSet.add(rdLink.getPid());
				}
				for (IRow formRow : rdLink.getForms()) {
					RdLinkForm form = (RdLinkForm) formRow;

					if (form.getFormOfWay() == 50) {
						crossLinkKindPidSet.add(form.getLinkPid());
						break;
					}
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.changedFields().containsKey("formOfWay")) {
					formOfWay = (int) form.changedFields().get("formOfWay");
				}
				if (form.status() != ObjStatus.DELETE && formOfWay == 50) {
					crossLinkKindPidSet.add(form.getLinkPid());
					RdLink rdLink = (RdLink) selector.loadById(form.getLinkPid(), true, true);
					int kind = rdLink.getKind();
					if (kind == 13 || kind == 11) {
						kind11And13LinkPidSet.add(rdLink.getPid());
					} else if (kind == 10) {
						kindOf10LinkPidSet.add(rdLink.getPid());
					} else if (kind == 9) {
						kindOf9LinkPidSet.add(rdLink.getPid());
					}
				}
				if (form.status() == ObjStatus.DELETE && formOfWay == 50) {
					crossLinkKindPidSet.remove(form.getLinkPid());
				}
			} else if (row instanceof RdElectroniceye) {
				RdElectroniceye eye = (RdElectroniceye) row;

				int kind = eye.getKind();

				if (eye.changedFields().containsKey("kind")) {
					kind = (int) eye.changedFields().get("kind");
				}
				if (eye.status() != ObjStatus.DELETE) {
					if (kind == 13) {
						kindOf13LinkPidSet.add(eye.getLinkPid());
					} else if (kind == 15) {
						kindOf15LinkPidSet.add(eye.getLinkPid());
					} else {
						kindNot13And15LinkPidSet.add(eye.getLinkPid());
					}
				}
			}
		}
	}
}
