package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM01295_1 一组线线的所有link中不能有步行街属性的link
 * 
 * @ClassName: GLM01307
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 一组线线的所有link中不能有步行街属性的link
 */

public class GLM01295_1 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01295_1.class);

	private Set<Integer> checkLinkSet = new HashSet<>();

	public GLM01295_1() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (int linkPid : checkLinkSet) {
			logger.info("检查类型：postCheck， 检查规则：GLM01295_1， 检查要素：RDLINK(" + linkPid + ")");
			StringBuilder sb = new StringBuilder();

			sb.append(
					"WITH T AS ( /*分歧*/ SELECT B.IN_LINK_PID AS LINK_PID FROM RD_BRANCH B WHERE B.RELATIONSHIP_TYPE = 2 AND B.u_record!=2 UNION SELECT V.LINK_PID FROM RD_BRANCH B, RD_BRANCH_VIA V WHERE B.RELATIONSHIP_TYPE = 2 AND B.BRANCH_PID = V.BRANCH_PID AND B.u_record!=2 AND V.u_record!=2 UNION SELECT B.OUT_LINK_PID FROM RD_BRANCH B WHERE B.RELATIONSHIP_TYPE = 2 AND B.u_record!=2  UNION ");

			sb.append(
					"/*交限，目前数据中没有线线交限*/ SELECT R.IN_LINK_PID FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL D WHERE R.PID = D.RESTRIC_PID AND D.RELATIONSHIP_TYPE = 2 AND R.u_record!=2 AND D.u_record!=2 UNION SELECT V.LINK_PID FROM RD_RESTRICTION_VIA V WHERE V.u_record!=2 UNION SELECT D.OUT_LINK_PID FROM RD_RESTRICTION_DETAIL D WHERE D.RELATIONSHIP_TYPE = 2 AND D.u_record!=2 ");

			sb.append(
					"/*车信*/ UNION SELECT C.IN_LINK_PID FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T WHERE C.PID = T.CONNEXITY_PID AND T.RELATIONSHIP_TYPE = 2 AND C.u_record!=2 AND T.u_record!=2 UNION SELECT V.LINK_PID FROM RD_LANE_TOPOLOGY T, RD_LANE_VIA V WHERE T.TOPOLOGY_ID = V.TOPOLOGY_ID AND T.RELATIONSHIP_TYPE = 2 AND T.u_record!=2 AND V.u_record!=2 UNION SELECT T.OUT_LINK_PID FROM RD_LANE_TOPOLOGY T WHERE T.RELATIONSHIP_TYPE = 2 AND T.u_record !=2 UNION ");

			sb.append(
					"/*语音引导*/ SELECT RV.IN_LINK_PID FROM RD_VOICEGUIDE RV, RD_VOICEGUIDE_DETAIL VD WHERE VD.VOICEGUIDE_PID = RV.PID AND VD.RELATIONSHIP_TYPE = 2 AND RV.u_record!=2 AND VD.u_record!=2 UNION SELECT VV.LINK_PID FROM RD_VOICEGUIDE_DETAIL VD, RD_VOICEGUIDE_VIA VV WHERE VD.RELATIONSHIP_TYPE = 2 AND VD.DETAIL_ID = VV.DETAIL_ID AND VD.u_record!=2 AND VV.u_record!=2 UNION SELECT VD.OUT_LINK_PID FROM RD_VOICEGUIDE_DETAIL VD WHERE VD.RELATIONSHIP_TYPE = 2 AND VD.u_record!=2 UNION ");

			sb.append(
					"/*顺行*/ SELECT RD.IN_LINK_PID FROM RD_DIRECTROUTE RD WHERE RD.RELATIONSHIP_TYPE = 2 AND RD.u_record!=2 UNION SELECT RDV.LINK_PID FROM RD_DIRECTROUTE RD, RD_DIRECTROUTE_VIA RDV WHERE RD.RELATIONSHIP_TYPE = 2 AND RD.PID = RDV.PID AND RD.u_record!=2 AND RDV.u_record!=2 UNION SELECT RD.OUT_LINK_PID FROM RD_DIRECTROUTE RD WHERE RD.RELATIONSHIP_TYPE = 2 AND RD.u_record!=2)  ");

			sb.append(
					" SELECT L.GEOMETRY, '[RD_LINK,' || L.LINK_PID || ']' TARGET, L.MESH_ID FROM T LL, RD_LINK_FORM F, RD_LINK L WHERE F.LINK_PID = LL.LINK_PID AND F.FORM_OF_WAY = 20 AND F.LINK_PID = L.LINK_PID AND L.LINK_PID =");

			sb.append(linkPid);

			sb.append(" AND F.u_record!=2 AND L.u_record!=2");

			log.info("RdLink后检查GLM01295_1 SQL:" + sb.toString());

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
			if (row instanceof RdLinkForm) {
				RdLinkForm form = (RdLinkForm) row;

				int formOfWay = form.getFormOfWay();

				if (form.status() != ObjStatus.DELETE) {
					if (form.changedFields().containsKey("formOfWay")) {
						formOfWay = (int) form.changedFields().get("formOfWay");
					}

					if (formOfWay == 20) {
						checkLinkSet.add(form.getLinkPid());
					}
				}
			}
		}
	}
}
