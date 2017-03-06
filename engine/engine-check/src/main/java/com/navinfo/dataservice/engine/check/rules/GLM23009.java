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
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
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
			logger.debug("检查类型：postCheck， 检查规则：GLM23009-log1， 检查要素：RD_ELECTRONICEYE(" + linkPid.toString() + ")");

			RdElectroniceyeSelector electronicSelector=new RdElectroniceyeSelector(getConn());
			RdElectroniceye elecEye= (RdElectroniceye)electronicSelector.loadById(linkPid, false);
			
			if (elecEye.getSpeedLimit() == 0 || (elecEye.getSpeedLimit() >= 100 && elecEye.getSpeedLimit() <= 1200
					&& (elecEye.getSpeedLimit() % 50 == 0))) {
				continue;
			}
			this.setCheckResult(elecEye.getGeometry(), "[RD_ELECTRONICEYE," + elecEye.getPid() + "]",
					elecEye.getMeshId());

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

				if (eye.changedFields().containsKey("speedLimit")) {
					checkPidSet.add(eye.getPid());
				}
			}
		}
	}
}
