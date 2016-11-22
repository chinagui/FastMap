package com.navinfo.dataservice.engine.edit.operation.topo.move.movelcnode;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.Result;
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
	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}

	private LcNode updateNode;
	private List<LcFace> lcFaces;

	public void lockLcLink() throws Exception {
		LcLinkSelector selector = new LcLinkSelector(this.getConn());
		List<LcLink> links = selector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
	}

	public void lockNode() throws Exception {

		if (this.getCommand().getNode() == null) {
			LcNodeSelector nodeSelector = new LcNodeSelector(this.getConn());

			this.updateNode = (LcNode) nodeSelector.loadById(this.getCommand()
					.getNodePid(), true);
		} else {
			this.updateNode = this.getCommand().getNode();
		}
	}

	public void lockLcFace() throws Exception {
		LcFaceSelector faceSelector = new LcFaceSelector(this.getConn());
		this.lcFaces = faceSelector.loadLcFaceByNodeId(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setFaces(lcFaces);

	}

	@Override
	public boolean prepareData() throws Exception {
		lockNode();
		if (CollectionUtils.isEmpty(this.getCommand().getLinks())) {
			lockLcLink();
		}

		lockLcFace();
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateNode, this.getConn())
				.run(this.getResult());
	}

}
