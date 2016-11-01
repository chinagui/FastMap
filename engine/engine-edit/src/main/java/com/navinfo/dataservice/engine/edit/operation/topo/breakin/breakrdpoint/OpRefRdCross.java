package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;

/**
 * 如果原始link为路口内组成link，分割link新生成的NODE应加入路口子点中，
 * 如果此路口的信号灯字段为“无路口红绿灯”或“有行人红绿灯”，则不影响信号灯记录； 如果此路口的信号灯字段为“有路口红绿灯”，则程序自动维护信号灯记录，
 * 具体维护原则见“修改道路路口节点”中“增加节点导致增加组成link”部分对信号灯的维护；
 * 
 * @ClassName: OpRefRdCross
 * @author Zhang Xiaolong
 * @date 2016年10月31日 上午10:01:58
 * @Description: TODO
 */
public class OpRefRdCross implements IOperation {

	private Command command;

	private Connection conn;

	public OpRefRdCross(Command command, Connection connection) {
		this.command = command;

		this.conn = connection;
	}

	@Override
	public String run(Result result) throws Exception {

		boolean isCrossLink = false;

		RdLink breakLink = this.command.getBreakLink();

		for (IRow row : breakLink.getForms()) {
			RdLinkForm form = (RdLinkForm) row;

			if (form.getFormOfWay() == 50) {
				isCrossLink = true;
				break;
			}
		}

		if (isCrossLink) {
			RdCrossSelector crossSelector = new RdCrossSelector(conn);

			List<Integer> linkPid = new ArrayList<>();

			linkPid.add(breakLink.getPid());

			List<RdCross> crossList = crossSelector.loadRdCrossByNodeOrLink(null, linkPid, true);

			if (CollectionUtils.isNotEmpty(crossList)) {
				// rd_cross_node
				RdCross cross = crossList.get(0);

				RdCrossNode crossNode = new RdCrossNode();

				crossNode.setPid(cross.getPid());

				crossNode.setNodePid(command.getBreakNode().getPid());

				result.insertObject(crossNode, ObjStatus.INSERT, crossNode.getPid());

				// rd_cross_link
				RdCrossLink crossLink1 = new RdCrossLink();

				crossLink1.setPid(cross.getPid());

				crossLink1.setLinkPid(command.getLink1().getPid());

				result.insertObject(crossLink1, ObjStatus.INSERT, crossLink1.getPid());

				RdCrossLink crossLink2 = new RdCrossLink();

				crossLink2.setPid(cross.getPid());

				crossLink2.setLinkPid(command.getLink2().getPid());

				result.insertObject(crossLink2, ObjStatus.INSERT, crossLink2.getPid());
				
			}
		}

		return null;
	}
}
