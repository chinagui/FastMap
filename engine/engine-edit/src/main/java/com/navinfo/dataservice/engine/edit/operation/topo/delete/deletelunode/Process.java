package com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.OperatorFactory;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	protected Logger log = Logger.getLogger(this.getClass());

	/*
	 * 加载土地利用点对应土地利用线
	 */
	public void lockLuLink() throws Exception {

		LuLinkSelector selector = new LuLinkSelector(this.getConn());
		List<LuLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		List<Integer> linkPids = new ArrayList<Integer>();
		for (LuLink link : links) {
			linkPids.add(link.getPid());
		}
		this.getCommand().setLinks(links);

		this.getCommand().setLinkPids(linkPids);
	}

	/*
	 * 加载土地利用点信息
	 */
	public void lockLuNode() throws Exception {

		LuNodeSelector selector = new LuNodeSelector(this.getConn());

		LuNode node = (LuNode) selector.loadById(this.getCommand().getNodePid(), true);

		this.getCommand().setNode(node);

	}

	/*
	 * 加载土地利用点对应的行政区盲端节点
	 */
	public void lockEndLuNode() throws Exception {

		LuNodeSelector selector = new LuNodeSelector(this.getConn());

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.add(this.getCommand().getNodePid());

		List<LuNode> nodes = new ArrayList<LuNode>();

		for (Integer linkPid : this.getCommand().getLinkPids()) {

			List<LuNode> list = selector.loadEndLuNodeByLinkPid(linkPid, true);

			for (LuNode node : list) {
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

	/*
	 * 加载土地利用点对应的土地利用线
	 */
	public void lockLuFace() throws Exception {

		LuFaceSelector selector = new LuFaceSelector(this.getConn());

		List<LuFace> faces = new ArrayList<LuFace>();

		for (Integer linkPid : this.getCommand().getLinkPids()) {

			List<LuFace> list = selector.loadLuFaceByLinkId(linkPid, true);

			for (LuFace face : list) {
				faces.add(face);

			}
		}
		this.getCommand().setFaces(faces);
	}

	@Override
	public boolean prepareData() throws Exception {

		lockLuNode();

		if (this.getCommand().getNode() == null) {

			throw new Exception("指定删除的LU_NODE不存在！");
		}

		lockLuLink();

		lockEndLuNode();
		lockLuFace();
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
		// 删除土地利用点有关土地利用点、线具体操作
		new OpTopo(this.getCommand()).run(this.getResult());
		// 同一点关系
		OpRefRdSameNode opRefRdSameNode = new OpRefRdSameNode(getConn());
		opRefRdSameNode.run(getResult(), this.getCommand());
		
		updataRelationObj() ;
		// 删除土地利用点有关土地利用面具体操作
		return new OpRefLuFace(this.getCommand(), getConn()).run(this.getResult());
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
