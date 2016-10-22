package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelcnode;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.selector.lc.LcFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lc.LcNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.OperatorFactory;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	protected Logger log = Logger.getLogger(this.getClass());

	public void lockLcLink() throws Exception {
		LcLinkSelector selector = new LcLinkSelector(this.getConn());
		List<LcLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		List<Integer> linkPids = new ArrayList<Integer>();
		for (LcLink link : links) {
			linkPids.add(link.getPid());
		}
		this.getCommand().setLinks(links);
		this.getCommand().setLinkPids(linkPids);
	}

	public void lockLcNode() throws Exception {
		LcNodeSelector selector = new LcNodeSelector(this.getConn());
		LcNode node = (LcNode) selector.loadById(this.getCommand().getNodePid(), true);
		this.getCommand().setNode(node);
	}

	public void lockEndLcNode() throws Exception {
		LcNodeSelector selector = new LcNodeSelector(this.getConn());
		List<Integer> nodePids = new ArrayList<Integer>();
		nodePids.add(this.getCommand().getNodePid());
		List<LcNode> nodes = new ArrayList<LcNode>();
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			List<LcNode> list = selector.loadEndLcNodeByLinkPid(linkPid, true);
			for (LcNode node : list) {
				int nodePid = node.getPid();
				if (nodePids.contains(nodePid)) {
					continue;
				}
				nodePids.add(node.getPid());
				nodes.add(node);
			}
		}
		this.getCommand().setNodes(nodes);
		this.getCommand().setNodePids(nodePids);
	}

	public void lockLcFace() throws Exception {
		LcFaceSelector selector = new LcFaceSelector(this.getConn());
		List<LcFace> faces = new ArrayList<LcFace>();
		for (Integer linkPid : this.getCommand().getLinkPids()) {
			List<LcFace> list = selector.loadLcFaceByLinkId(linkPid, true);
			for (LcFace face : list) {
				faces.add(face);
			}
		}
		this.getCommand().setFaces(faces);
	}

	@Override
	public boolean prepareData() throws Exception {
		// 检查是否可以删除
		String msg = preCheck();
		if (null != msg) {
			throw new Exception(msg);
		}
		// 获取该adnode对象
		lockLcNode();
		if (this.getCommand().getNode() == null) {
			throw new Exception("指定删除的RDNODE不存在！");
		}
		lockLcLink();
		lockEndLcNode();
		lockLcFace();
		return true;
	}

	@Override
	public boolean recordData() throws Exception {
		LogWriter lw = new LogWriter(this.getConn());
		lw.generateLog(this.getCommand(), this.getResult());
		OperatorFactory.recordData(this.getConn(), this.getResult());
		lw.recordLog(this.getCommand(), this.getResult());
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// 删除行政区划点有关行政区划点、线具体操作
		IOperation op = new OpTopo(this.getCommand());
		op.run(this.getResult());
		// 立交
		IOperation opRefRdGsc = new OpRefRdGsc(this.getCommand(), this.getConn());
		opRefRdGsc.run(this.getResult());
		// 删除行政区划点有关行政区划面具体操作
		IOperation opLcFace = new OpRefLcFace(this.getCommand());
		return opLcFace.run(this.getResult());
	}

}
