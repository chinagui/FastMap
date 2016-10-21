package com.navinfo.dataservice.engine.edit.operation.obj.rdnode.depart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

public class Operation {

	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	/**
	 * NODE形态：原线组中的NODE，上下线分离后新生成两个node，新生成的node形态都修改为“无属性”（继承原本PID和新生成的PIDnode形态值都维护为“无属性”）
	 * @param sNodePid
	 * @param eNodePid
	 * @param targetLinks
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String updownDepart(int sNodePid, int eNodePid,
			List<RdLink> targetLinks, Result result) throws Exception {
		
		String msg = "";
		
		// 目标link之间的挂接node
		List<Integer> nodePids = new ArrayList<Integer>();

		for (RdLink link : targetLinks) {

			if (!nodePids.contains(link.getsNodePid())) {

				nodePids.add(link.getsNodePid());
			}

			if (!nodePids.contains(link.geteNodePid())) {

				nodePids.add(link.geteNodePid());
			}

			// 过滤端点
			if (nodePids.contains(sNodePid)) {

				nodePids.remove((Integer) sNodePid);
			}

			if (nodePids.contains(eNodePid)) {

				nodePids.remove((Integer) eNodePid);
			}
		}

		if (nodePids.size() == 0) {
			return msg;
		}

		RdNodeSelector nodeSelector = new RdNodeSelector(this.conn);

		List<IRow> nodeRows = nodeSelector.loadByIds(nodePids, true, true);

		for (IRow nodeRow : nodeRows) {

			RdNode node = (RdNode) nodeRow;

			for (IRow formRow : node.getForms()) {

				RdNodeForm form = (RdNodeForm) formRow;

				result.insertObject(form, ObjStatus.DELETE, form.getNodePid());
			}

			RdNodeForm newForm = new RdNodeForm();

			newForm.setNodePid(node.getPid());

			result.insertObject(newForm, ObjStatus.INSERT, newForm.getNodePid());
		}

		return msg;
	}

}
