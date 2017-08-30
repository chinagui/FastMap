package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;

/**
 * 检查对象：RD_ELECTRONICEYE表中DIRECT(电子眼作用方向)
 * 检查原则：当link为单方向时，电子眼的作用方向应该与道路的方向一致，否则报LOG；
 * 			 当link为双方向时，DIRECT字段必须不为0，否则报log。
 * @author fhx
 *
 */
public class GLM23005 extends baseRule {
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow row : checkCommand.getGlmList()) {
			// 名称类型编辑
			if (row instanceof RdElectroniceye) {
				RdElectroniceye electronic = (RdElectroniceye) row;
				this.checkElectronicEye(electronic);
			}
		}
	}

	private void checkElectronicEye(RdElectroniceye electroniceye) throws Exception {
		if (electroniceye.status() != ObjStatus.UPDATE) {
			return;
		}

		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		RdLink link = (RdLink) selector.loadById(electroniceye.getLinkPid(), false, true);

		int direct = link.getDirect();
		int elecDirect = electroniceye.getDirect();

		if ((direct == 1 && elecDirect == 0) || ((direct == 2 || direct == 3) && direct != elecDirect)) {
			String target = "[RD_ELECTRONICEYE," + electroniceye.getPid() + "]";

			this.setCheckResult(electroniceye.getGeometry(), target, 0);
		}
	}
}
