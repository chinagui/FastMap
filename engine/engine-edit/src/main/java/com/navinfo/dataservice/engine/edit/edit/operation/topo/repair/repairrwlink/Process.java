package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair.repairrwlink;

import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.engine.edit.comm.util.operate.RdGscOperateUtils;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
import com.vividsolutions.jts.geom.Geometry;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		int linkPid = this.getCommand().getLinkPid();

		this.getCommand().setUpdateLink((RwLink) new RwLinkSelector(this.getConn()).loadById(linkPid, true));

		// 查询需要修行的线上是否存在立交
		RdGscSelector gscSelector = new RdGscSelector(this.getConn());

		List<RdGsc> gscList = gscSelector.onlyLoadRdGscLinkByLinkPid(linkPid, "RW_LINK", true);

		this.getCommand().setGscList(gscList);

		return false;
	}

	@Override
	public String preCheck() throws Exception {

		check.checkShapePointDistance(this.getCommand().getLinkGeom());

		Geometry geo = GeoTranslator.geojson2Jts(this.getCommand().getLinkGeom());
		
		RdGscSelector selector = new RdGscSelector(this.getConn());
		
		List<RdGsc> rdGscList = selector.onlyLoadRdGscLinkByLinkPid(this.getCommand().getLinkPid(), "RW_LINK", true);

		boolean flag = RdGscOperateUtils.isMoveGscLink(geo, rdGscList);

		if (flag) {
			throw new Exception("不容许去除有立交关系的形状点");
		}

		return null;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
