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
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM23009 电子眼限速值（SPEED_LIMIT）的值域范围必须为：0或100~1200之间且必须为50的倍数，否则报LOG
 * 
 * @ClassName: GLM23009
 * @author Zhang Xiaolong
 * @date 2017年2月6日 上午11:30:45
 * @Description: 电子眼限速值（SPEED_LIMIT）的值域范围必须为：0或100~1200之间且必须为50的倍数，否则报LOG
 */

public class GLM23009 extends baseRule {

	private static Logger logger = Logger.getLogger(GLM23009.class);

	/**
	 * 需要检查的电子眼pid
	 */
	private Set<Integer> checkPidSet = new HashSet<>();

	public GLM23009() {
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		prepareData(checkCommand);
		for (Integer linkPid : checkPidSet) {
			logger.debug("检查类型：postCheck， 检查规则：GLM23007-log1， 检查要素：RDLINK(" + linkPid + ")");
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT K.GEOMETRY, '[RD_ELECTRONICEYE,' || K.PID || ']' TARGET, K.MESH_ID FROM RD_ELECTRONICEYE K WHERE K.PID =");
			sb.append(linkPid);
			sb.append(" AND K.U_RECORD <> 2 AND (K.SPEED_LIMIT <> 0 OR ((K.SPEED_LIMIT > 100 AND K.SPEED_LIMIT < 1200) AND MOD(K.SPEED_LIMIT, 50) = 0)) ");

			log.info("GLM23009后检查SQL："+sb.toString());
			
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
				if (row instanceof RdElectroniceye && row.status() == ObjStatus.UPDATE) {
				RdElectroniceye eye = (RdElectroniceye) row;
				if(eye.changedFields().containsKey("speedLimit"))
				{
					checkPidSet.add(eye.getPid());
				}
			}
		}
	}
}
