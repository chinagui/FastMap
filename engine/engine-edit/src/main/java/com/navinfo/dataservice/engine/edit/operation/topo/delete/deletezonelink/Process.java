package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 
 * 删除ZONE线执行类
 * 
 * @author zhaokk
 * 
 */
public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}

	public void lockZoneLink() throws Exception {

		ZoneLinkSelector selector = new ZoneLinkSelector(this.getConn());

		ZoneLink link = (ZoneLink) selector.loadById(this.getCommand()
				.getLinkPid(), true);

		this.getCommand().setLink(link);
	}

	// 锁定盲端节点
	public void lockZoneNode() throws Exception {

		ZoneNodeSelector selector = new ZoneNodeSelector(this.getConn());

		List<ZoneNode> nodes = selector.loadEndZoneNodeByLinkPid(this
				.getCommand().getLinkPid(), false);

		List<Integer> nodePids = new ArrayList<Integer>();

		for (ZoneNode node : nodes) {
			nodePids.add(node.getPid());
		}
		this.getCommand().setNodes(nodes);

		this.getCommand().setNodePids(nodePids);
	}

	// 锁定盲端节点
	public void lockZoneFace() throws Exception {
		ZoneFaceSelector selector = new ZoneFaceSelector(this.getConn());
		List<ZoneFace> faces = selector.loadZoneFaceByLinkId(this.getCommand()
				.getLinkPid(), true);
		this.getCommand().setFaces(faces);
	}

	@Override
	public boolean prepareData() throws Exception {

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}
		// 获取该link对象
		this.lockZoneLink();
		this.lockZoneNode();
		this.lockZoneFace();
		if (this.getCommand().getLink() == null) {

			throw new Exception("指定删除的LINK不存在！");
		}
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		
		// 删除行政区划线有关行政区划点、线具体操作
		IOperation op = new OpTopo(this.getCommand());
		op.run(this.getResult());
		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand().getNodePids());

		updataRelationObj();
		// 删除行政区划线有关行政区划面具体操作
		IOperation opAdFace = new OpRefAdFace(this.getCommand());
		return opAdFace.run(this.getResult());
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
