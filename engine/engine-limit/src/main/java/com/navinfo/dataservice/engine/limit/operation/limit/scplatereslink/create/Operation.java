package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.create;

import java.sql.Connection;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;

import net.sf.json.JSONArray;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONArray array = this.command.getLinks();

		Connection regionConn = null;

		try {
			regionConn = DBConnector.getInstance().getConnectionById(this.command.getDbId());

			RdLinkSelector selector = new RdLinkSelector(regionConn);

			for (int i = 0; i < array.size(); i++) {

				ScPlateresLink link = new ScPlateresLink();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESLINK, i);

				int id = array.getInt(i);
				RdLink oldlink = (RdLink) selector.loadById(id, true);

				link.setGeometryId(geomId);
				link.setGroupId(this.command.getGroupId());
				link.setGeometry(oldlink.getGeometry());

				result.insertObject(link, ObjStatus.INSERT, geomId);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			regionConn.close();
		}

		return null;
	}

}
