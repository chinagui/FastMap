package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrdlink;

import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
import com.vividsolutions.jts.geom.Geometry;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private RdLink updateLink;

	private RdNode snode;

	private RdNode enode;

	private Check check = new Check();

	@Override
	public boolean prepareData() throws Exception {

		int linkPid = this.getCommand().getLinkPid();

		this.updateLink = (RdLink) new RdLinkSelector(this.getConn()).loadById(linkPid, true);

		RdNodeSelector nodeSelector = new RdNodeSelector(this.getConn());

		this.snode = (RdNode) nodeSelector.loadById(updateLink.getsNodePid(), true);

		this.enode = (RdNode) nodeSelector.loadById(updateLink.geteNodePid(), true);

		// 查询需要修行的线上是否存在立交
		RdGscSelector gscSelector = new RdGscSelector(this.getConn());

		List<RdGsc> gscList = gscSelector.onlyLoadRdGscLinkByLinkPid(linkPid, "RW_LINK", true);

		this.getCommand().setGscList(gscList);

		return false;
	}

	@Override
	public String preCheck() throws Exception {
		super.preCheck();
		check.checkIsVia(this.getConn(), this.getCommand().getLinkPid());

		check.checkShapePointDistance(this.getCommand().getLinkGeom());

		Geometry geo = GeoTranslator.geojson2Jts(this.getCommand().getLinkGeom());

		boolean flag = check.checkIsGscPoint(this.getCommand().getLinkPid(), geo, this.getConn());

		if (flag) {
			throw new Exception("不容许去除有立交关系的形状点");
		}

		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getConn(), this.getCommand(), updateLink, snode, enode, check).run(this.getResult());
	}

}
