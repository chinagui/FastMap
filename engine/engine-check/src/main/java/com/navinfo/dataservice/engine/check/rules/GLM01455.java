package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * @ClassName: GLM01455
 * @author zhangxiaolong
 * @date 2017年2月7日
 * @Description: 同一路口挂接的所有道路（交叉口内LINK除外）都有POI连接路形态，路口中任意一条交叉口内LINK没有POI连接路形态时，程序报LOG
 */
public class GLM01455 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM01455.class);

	private Set<Integer> resultLinkPidSet = new HashSet<>();

	public GLM01455() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : resultLinkPidSet) {
			StringBuilder sb = new StringBuilder();

			sb.append(
					"SELECT R.GEOMETRY, '[RD_LINK,' || R.LINK_PID || ']' TARGET, R.MESH_ID FROM RD_LINK R, RD_CROSS_LINK RCL WHERE R.LINK_PID =");

			sb.append(linkPid);

			sb.append(
					" AND R.LINK_PID = RCL.LINK_PID AND R.u_record！=2 AND RCL.u_record！=2 AND 50 NOT IN (SELECT RLF.FORM_OF_WAY FROM RD_LINK_FORM RLF WHERE R.LINK_PID = RLF.LINK_PID AND RLF.u_record！=2) AND 36 NOT IN (SELECT RLF.FORM_OF_WAY FROM RD_LINK_FORM RLF WHERE R.LINK_PID = RLF.LINK_PID AND RLF.u_record！=2) ");

			log.info("RdLink后检查GLM01455 SQL:" + sb.toString());

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
				if (row instanceof RdLinkForm) {
					RdLinkForm form = (RdLinkForm) row;
					resultLinkPidSet.add(form.getLinkPid());
				}
			}
		}
	}

}
