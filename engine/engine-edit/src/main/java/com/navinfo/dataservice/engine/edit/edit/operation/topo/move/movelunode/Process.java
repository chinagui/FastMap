package com.navinfo.dataservice.engine.edit.edit.operation.topo.move.movelunode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private LuNode updateNode;
	private List<LuFace> luFaces;

	/*
	 * 加载对应的土地利用线信息
	 */
	public void lockLuLink() throws Exception {

		LuLinkSelector selector = new LuLinkSelector(this.getConn());

		List<LuLink> links = selector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
	}

	/*
	 * 加载对应的土地利用点信息
	 */
	public void lockLuNode() throws Exception {

		LuNodeSelector nodeSelector = new LuNodeSelector(this.getConn());

		this.updateNode = (LuNode) nodeSelector.loadById(this.getCommand()
				.getNodePid(), true);
	}

	/*
	 * 加载对应的土地利用面信息
	 */
	public void lockLuFace() throws Exception {

		LuFaceSelector faceSelector = new LuFaceSelector(this.getConn());

		this.luFaces = faceSelector.loadLuFaceByNodeId(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setFaces(luFaces);

	}

	@Override
	public boolean prepareData() throws Exception {

		lockLuNode();
		lockLuLink();
		lockLuFace();
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateNode, this.getConn())
				.run(this.getResult());
	}

}
