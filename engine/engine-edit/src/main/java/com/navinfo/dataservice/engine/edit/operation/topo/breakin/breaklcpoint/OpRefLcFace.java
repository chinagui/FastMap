package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;

public class OpRefLcFace implements IOperation {

	private Command command;

	private Result result;

	private Connection conn;

	public OpRefLcFace(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.result = result;
		if (command.getFaces() != null && command.getFaces().size() > 0) {
			this.handleLcFaceTopo();
		}
		return null;
	}

	/*
	 * @param List 修改LcFace 和LcLink topo 关系
	 */
	private void handleLcFaceTopo() throws Exception {
		List<LcLink> links;
		// 1.获取打断点涉及的面信息
		// 2.删除打断线对应面的topo关系
		// 3.重新获取组成面的link关系，重新计算面的形状
		for (LcFace face : command.getFaces()) {
			links = new ArrayList<LcLink>();
			for (IRow iRow : face.getTopos()) {
				LcFaceTopo obj = (LcFaceTopo) iRow;
				if (obj.getLinkPid() != command.getLinkPid()) {
					links.add((LcLink) new LcLinkSelector(conn).loadById(obj.getLinkPid(), true));
				}
				result.insertObject(obj, ObjStatus.DELETE, face.getPid());
			}
			links.addAll(command.getNewLinks());
			com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation opFace = new com.navinfo.dataservice.engine.edit.operation.obj.lcface.create.Operation(
					result, face);
			opFace.reCaleFaceGeometry(links);
		}

	}

}