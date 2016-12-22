package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelclink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	public void lockLcLink() throws Exception {
		LcLinkSelector selector = new LcLinkSelector(this.getConn());
		LcLink link = (LcLink) selector.loadById(this.getCommand().getLinkPid(), true);
		this.getCommand().setLink(link);
	}

	// 锁定盲端节点
	public void lockLcNode() throws Exception {
		LcNodeSelector selector = new LcNodeSelector(this.getConn());
		List<LcNode> nodes = selector.loadEndLcNodeByLinkPid(this.getCommand().getLinkPid(), false);
		List<Integer> nodePids = new ArrayList<Integer>();
		for (LcNode node : nodes) {
			nodePids.add(node.getPid());
		}
		this.getCommand().setNodes(nodes);
		this.getCommand().setNodePids(nodePids);
	}

	// 锁定盲端节点
	public void lockLcFace() throws Exception {
		LcFaceSelector selector = new LcFaceSelector(this.getConn());
		List<LcFace> faces = selector.loadLcFaceByLinkId(this.getCommand().getLinkPid(), true);
		this.getCommand().setFaces(faces);
	}

	@Override
	public boolean prepareData() throws Exception {
		// 获取该link对象
		lockLcLink();
		if (this.getCommand().getLink() == null) {
			throw new Exception("指定删除的LINK不存在！");
		}
		lockLcNode();
		lockLcFace();
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// 删除土地覆盖线有关土地覆盖点、线具体操作
		IOperation op = new OpTopo(this.getCommand());
		op.run(this.getResult());
		// 立交
		IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
		opRefRdGsc.run(this.getResult());
		// 删除土地覆盖线有关土地覆盖面具体操作
		IOperation opLcFace = new OpRefLcFace(this.getCommand());
		return opLcFace.run(this.getResult());
	}

}
