package com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}

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

		if (this.getCommand().getNode() == null) {
			LuNodeSelector nodeSelector = new LuNodeSelector(this.getConn());

			this.updateNode = (LuNode) nodeSelector.loadById(this.getCommand()
					.getNodePid(), true);
		} else {
			this.updateNode = this.getCommand().getNode();
		}

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
		if (CollectionUtils.isEmpty(this.getCommand().getLinks())) {
			lockLuLink();
		}
		lockLuFace();
		return false;
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(this.getCommand(), updateNode,
					this.getConn());

			msg = operation.run(this.getResult());

			//this.postCheck();

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateNode, this.getConn())
				.run(this.getResult());
	}

}
