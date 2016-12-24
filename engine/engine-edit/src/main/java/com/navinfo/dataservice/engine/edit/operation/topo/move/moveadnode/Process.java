package com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhaokk 移动行政区划点具体执行类
 */
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

	private AdNode updateNode;
	private List<AdFace> adFaces;

	/*
	 * 移动行政区划点加载对应的行政区划线信息
	 */
	public void lockAdLink() throws Exception {

		AdLinkSelector selector = new AdLinkSelector(this.getConn());

		List<AdLink> links = selector.loadByNodePid(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setLinks(links);
	}

	/*
	 * 移动行政区划点加载对应的行政区点线信息
	 */
	public void lockAdNode() throws Exception {

		if (this.getCommand().getNode() == null) {
			AdNodeSelector nodeSelector = new AdNodeSelector(this.getConn());

			this.updateNode = (AdNode) nodeSelector.loadById(this.getCommand()
					.getNodePid(), true);
		} else {
			this.updateNode = this.getCommand().getNode();
		}

	}

	/*
	 * 移动行政区划点加载对应的行政区点面信息
	 */
	public void lockAdFace() throws Exception {

		AdFaceSelector faceSelector = new AdFaceSelector(this.getConn());

		this.adFaces = faceSelector.loadAdFaceByNodeId(this.getCommand()
				.getNodePid(), true);
		this.getCommand().setFaces(adFaces);

	}

	@Override
	public boolean prepareData() throws Exception {

		lockAdNode();
		if (CollectionUtils.isEmpty(this.getCommand().getLinks())) {
			lockAdLink();
		}

		lockAdFace();
		return false;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(), updateNode, this.getConn())
				.run(this.getResult());
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			IOperation operation = new Operation(this.getCommand(), updateNode,
					this.getConn());

			msg = operation.run(this.getResult());

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}
}
