package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String exeOperation() throws Exception {
		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand());
		return new Operation(this.getCommand()).run(this.getResult());
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		lockRelationData();

		return true;
	}

	/**
	 * 获取需要同时删除的要素
	 * 
	 * @throws Exception
	 */
	private void lockRelationData() throws Exception {
		// 先筛选出需要同时删除的RWLink，再筛选被删的RWNode、RDGSC；

		lockRwLink();

		lockRwNode(this.getCommand().getLinks());

		lockRdGsc(this.getCommand().getLinks());
	}

	public void lockRwLink() throws Exception {

		RwLinkSelector rwLinkSelector = new RwLinkSelector(this.getConn());

		List<RwLink> links = rwLinkSelector.loadByNodePid(this.getCommand().getNodePid(), true);

		this.getCommand().setLinks(links);
	}

	public void lockRwNode(List<RwLink> links) throws Exception {
		List<RwNode> nodes = new ArrayList<RwNode>();

		RwNodeSelector selector = new RwNodeSelector(this.getConn());

		IRow nodeRow = selector.loadById(this.getCommand().getNodePid(), true);

		// 添加目标RWNode
		nodes.add((RwNode) nodeRow);

		HashSet<Integer> nodeIds = new HashSet<Integer>();

		// 获取与被删node关联link的另一端node
		for (RwLink link : links) {
			nodeIds.add(link.getsNodePid());

			nodeIds.add(link.geteNodePid());
		}

		nodeIds.remove(this.getCommand().getNodePid());

		Iterator it = nodeIds.iterator();

		while (it.hasNext()) {

			int nodePid = (int) it.next();

			RwNode node = selector.GetRwNodeWithLinkById(nodePid, true);

			// node如果只关联一个link，删除该node
			if (node.getTopoLinks().size() == 1) {
				nodes.add(node);
			}
		}

		this.getCommand().setNodes(nodes);
	}

	/**
	 * 获取需要同时删除的rdGsc
	 * 
	 * @param links
	 * @throws Exception
	 */
	public void lockRdGsc(List<RwLink> links) throws Exception {
		if (links.size() < 1) {
			return;
		}

		List<RdGsc> rdGscs = new ArrayList<RdGsc>();

		RdGscSelector selector = new RdGscSelector(this.getConn());

		for (RwLink link : links) {

			List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(link.getPid(), "RW_LINK", true);
			rdGscs.addAll(rdGscList);
		}

		this.getCommand().setRdGscs(rdGscs);
	}

}
