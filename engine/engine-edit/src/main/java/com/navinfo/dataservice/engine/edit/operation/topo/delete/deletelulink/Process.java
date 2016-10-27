package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该link对象
		lockLuLink();

		if (this.getCommand().getLink() == null) {

			throw new Exception("指定删除的LINK不存在！");
		}

		lockLuNode();
		lockLuFace();
		return true;
	}

	private void lockLuNode() throws Exception {
		LuNodeSelector selector = new LuNodeSelector(this.getConn());
		List<LuNode> nodes = selector.loadEndLuNodeByLinkPid(this.getCommand()
				.getLinkPid(), false);
		this.getCommand().setNodes(nodes);

		List<Integer> nodePids = new ArrayList<Integer>();
		for (LuNode node : nodes) {
			nodePids.add(node.getPid());
		}
		this.getCommand().setNodePids(nodePids);
	}

	private void lockLuLink() throws Exception {
		LuLinkSelector selector = new LuLinkSelector(this.getConn());
		LuLink link = (LuLink) selector.loadById(
				this.getCommand().getLinkPid(), true);
		this.getCommand().setLink(link);
	}

	private void lockLuFace() throws Exception {
		LuFaceSelector selector = new LuFaceSelector(this.getConn());
		List<LuFace> faces = selector.loadLuFaceByLinkId(this.getCommand()
				.getLinkPid(), true);
		this.getCommand().setFaces(faces);
	}

	@Override
	public String exeOperation() throws Exception {
		
		new OpTopo(this.getCommand()).run(this.getResult());
		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());

		opRefRdSameNode.run(getResult(), this.getCommand().getNodePids());

		updataRelationObj();

		return new OpRefLuFace(this.getCommand()).run(this.getResult());
	}

	/**
	 * 维护关联要素
	 * 
	 * @throws Exception
	 */
	private void updataRelationObj() throws Exception {
		OpRefRelationObj opRefRelationObj = new OpRefRelationObj(getConn());

		opRefRelationObj.handleSameLink(this.getResult(), this.getCommand());
	}
}
