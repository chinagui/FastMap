package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
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

	}

	// 锁定铁路线
	public void lockRwLink() throws Exception {

		RwLinkSelector selector = new RwLinkSelector(this.getConn());

		RwLink link = (RwLink) selector.loadById(this.getCommand().getLinkPid(), true);

		this.getCommand().setLink(link);
	}

	public void lockRdGsc() throws Exception {

		RdGscSelector selector = new RdGscSelector(this.getConn());

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RW_LINK", true);

		this.getCommand().setRdGscs(rdGscList);
	}

	// 锁定盲端节点
	public void lockRwNode() throws Exception {

		RwNodeSelector selector = new RwNodeSelector(this.getConn());

		List<RwNode> nodes = selector.loadEndRdNodeByLinkPid(this.getCommand().getLinkPid(), true);

		List<Integer> nodePids = new ArrayList<Integer>();

		for (RwNode node : nodes) {
			nodePids.add(node.getPid());
		}
		this.getCommand().setNodes(nodes);
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该link对象
		lockRwLink();

		// 获取gsc对象
		lockRdGsc();

		if (this.getCommand().getLink() == null) {

			throw new Exception("指定删除的RWLINK不存在！");
		}

		lockRwNode();
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		// 删除铁路线有铁路点、线具体操作
		IOperation op = new OpTopo(this.getCommand());
		op.run(this.getResult());
		IOperation opRwLink = new OpRefRwGsc(this.getCommand());
		return opRwLink.run(this.getResult());
	}

}
