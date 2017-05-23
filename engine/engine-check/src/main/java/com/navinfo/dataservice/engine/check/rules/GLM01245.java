package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: GLM01245
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 
 *  1） 步行道路种别的Link,限速类型为“普通”时,速度限制必须等于10km/h，否则报log1 
 * 	2）非引导道路、步行道路种别的Link的限速类型为“普通”时,限速来源必须是“未调查”，否则报log2。 
 *  3）不具有的SA或PA属性的高速、城市高速的限速类型为“普通”时,限速来源不能是“未调查”，否则报log3 
 *  4）非引导道路种别的Link限速类型为“普通”时,速度限制必须等于1115km/h，否则报log4
 */
public class GLM01245 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01245.class);

	private Set<Integer> check1 = new HashSet<>();

	private Set<Integer> check2 = new HashSet<>();

	private Set<Integer> check3 = new HashSet<>();

	private Set<Integer> check4 = new HashSet<>();

	public GLM01245() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : check1) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID, '10级路的速度限制必须等于1010KM/H' AS LOG FROM RD_LINK RL, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RL.KIND = 10 AND RLS.SPEED_TYPE = 0 AND RL.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND (RLS.FROM_SPEED_LIMIT <> 100 OR RLS.TO_SPEED_LIMIT <> 100)");

			logger.info("RdLink后检查GLM01245 check1->SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2),"10级路的速度限制必须等于1010KM/H");
			}
		}
		
		for (Integer linkPid : check2) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID, '9/10级路的限速来源必须是“未调查”' AS LOG FROM RD_LINK RL, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND RL.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND (RL.KIND = 9 OR RL.KIND = 10) AND RLS.SPEED_TYPE = 0 AND (RLS.FROM_LIMIT_SRC <> 9 OR RLS.TO_LIMIT_SRC <> 9)");

			logger.info("RdLink后检查GLM01245 check2->SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2),"9/10级路的限速来源必须是“未调查”");
			}
		}
		
		for (Integer linkPid : check3) {
			String sql = "SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID, '高速或城市高速的限速来源必须不是“未调查”"
                    + "' AS LOG FROM RD_LINK RL, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID = "
                    + linkPid + " AND RL.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND (RL.KIND = 1 OR RL.KIND = 2) AND (RLS.FROM_LIMIT_SRC = 9 "
                    + "OR RLS.TO_LIMIT_SRC = 9) AND RL.LINK_PID NOT IN (SELECT RLF.LINK_PID FROM RD_LINK_FORM RLF WHERE RLF.LINK_PID = "
                    + linkPid + " AND (RLF.FORM_OF_WAY = 12 OR RLF.FORM_OF_WAY = 13) AND RLF.U_RECORD <>　2)";
			logger.info("RdLink后检查GLM01245 check3->SQL:" + sql);

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2),"高速或城市高速的限速来源必须不是“未调查”");
			}
		}
		
		for (Integer linkPid : check4) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID, '9级路的速度限制必须等于15KM/H' AS LOG FROM RD_LINK RL, RD_LINK_SPEEDLIMIT RLS WHERE RL.LINK_PID = RLS.LINK_PID AND RL.LINK_PID = ");

			sb.append(linkPid);

			sb.append(
					" AND RL.U_RECORD <> 2 AND RLS.U_RECORD <> 2 AND RL.KIND = 9 AND (RLS.FROM_SPEED_LIMIT <> 150 OR RLS.TO_SPEED_LIMIT <> 150)");

			logger.info("RdLink后检查GLM01245 check4->SQL:" + sb.toString());

			DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
			List<Object> resultList = getObj.exeSelect(this.getConn(), sb.toString());

			if (!resultList.isEmpty()) {
				this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(),
						(int) resultList.get(2),"9级路的速度限制必须等于15KM/H");
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
				if (rdLink.status() != ObjStatus.DELETE && rdLink.changedFields().containsKey("kind")) {
					kind = (int) rdLink.changedFields().get("kind");
				}
				if (kind == 1 || kind == 2) {
					check3.add(rdLink.getPid());
				}
				if (kind == 9) {
					check2.add(rdLink.getPid());
					check4.add(rdLink.getPid());
				}
				if(kind == 10) {
				    check1.add(rdLink.getPid());
					check2.add(rdLink.getPid());
				}
			} else if (row instanceof RdLinkForm) {
				RdLinkForm rdLinkForm = (RdLinkForm) row;
				int oldFormOfWay = rdLinkForm.getFormOfWay();
				int formOfWay = oldFormOfWay;
				if (rdLinkForm.changedFields().containsKey("formOfWay")) {
				    formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
                }
                if (rdLinkForm.status() == ObjStatus.DELETE && (formOfWay == 12 || formOfWay == 13)) {
				    check3.add(rdLinkForm.getLinkPid());
                }
                if (rdLinkForm.status() == ObjStatus.UPDATE && (oldFormOfWay == 12 || oldFormOfWay == 13) && formOfWay != 12 && formOfWay != 13) {
                    check3.add(rdLinkForm.getLinkPid());
                }
			} else if (row instanceof RdLinkSpeedlimit) {
				RdLinkSpeedlimit rdLinkSpeedlimit = (RdLinkSpeedlimit) row;
				int speedType = rdLinkSpeedlimit.getSpeedType();
				int fromSpeedLimit = rdLinkSpeedlimit.getFromSpeedLimit();
				int toSpeedLimit = rdLinkSpeedlimit.getToSpeedLimit();
				int toLimitSrc = rdLinkSpeedlimit.getToLimitSrc();
				int fromLimitSrc = rdLinkSpeedlimit.getFromLimitSrc();

				if (rdLinkSpeedlimit.status() != ObjStatus.DELETE) {
					if (rdLinkSpeedlimit.changedFields().containsKey("speedType")) {
						speedType = Integer.parseInt(rdLinkSpeedlimit.changedFields().get("speedType").toString());
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("fromSpeedLimit")) {
						fromSpeedLimit = Integer.parseInt(rdLinkSpeedlimit.changedFields().get("fromSpeedLimit").toString());
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("toSpeedLimit")) {
						toSpeedLimit = Integer.parseInt(rdLinkSpeedlimit.changedFields().get("toSpeedLimit").toString());
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("toLimitSrc")) {
						toLimitSrc = Integer.parseInt(rdLinkSpeedlimit.changedFields().get("toLimitSrc").toString());
					}
					if (rdLinkSpeedlimit.changedFields().containsKey("fromLimitSrc")) {
						fromLimitSrc = Integer.parseInt(rdLinkSpeedlimit.changedFields().get("fromLimitSrc").toString());
					}

					if (speedType == 0 && (fromSpeedLimit != 100 || toSpeedLimit != 100)) {
						check1.add(rdLinkSpeedlimit.getLinkPid());
					}
					if (speedType == 0 && (fromLimitSrc != 9 || toLimitSrc != 9)) {
						check2.add(rdLinkSpeedlimit.getLinkPid());
					}
					if (speedType == 0 && (fromLimitSrc == 9 || toLimitSrc == 9)) {
						check3.add(rdLinkSpeedlimit.getLinkPid());
					}
					if (speedType == 0 && (fromSpeedLimit != 150 || toLimitSrc != 150)) {
						check4.add(rdLinkSpeedlimit.getLinkPid());
					}
				}
			}
		}
	}

}
