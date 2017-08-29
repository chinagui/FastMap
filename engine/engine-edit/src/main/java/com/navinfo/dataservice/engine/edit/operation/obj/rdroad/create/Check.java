package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

public class Check {

	/**
	 * 编辑制作CRFR时，已经参与制作了CRFI的link，实时控制不允许再制作CRFR
	 * 
	 * @param command
	 * @param conn
	 * @throws Exception
	 */
	public void hasMakedCRFI(Command command, Connection conn) throws Exception {
		List<Integer> linkPids = command.getLinkPids();

		RdInterSelector selector = new RdInterSelector(conn);

		for (int linkpid : linkPids) {
			RdInterLink inter = selector.loadByLinkPid(linkpid, true);

			if (inter != null) {
				throw new Exception("已参与制作了CRFI的link:[" + linkpid + "]不允许再制作CRFR");
			}
		}
	}
}
