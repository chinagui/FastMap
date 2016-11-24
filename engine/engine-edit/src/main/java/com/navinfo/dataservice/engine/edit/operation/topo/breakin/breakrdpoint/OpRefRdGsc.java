package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class OpRefRdGsc implements IOperation {

	private Command command;

	private Result result;

	private Connection connection;

	public OpRefRdGsc(Command command, Connection connection) {
		this.command = command;

		this.connection = connection;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		List<RdGsc> rdGscList = command.getRdGscs();

		if (CollectionUtils.isNotEmpty(rdGscList)) {
			com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation updateOp = new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Operation("RD_LINK");
			
			updateOp.breakLineForGsc(result, command.getBreakLink(), command.getNewLinks(), rdGscList);
		}

		return null;
	}

}
