package com.navinfo.dataservice.engine.limit.operation.limit.scplateresrdface.create;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;

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

		for (int i = 0; i < array.size(); i++) {

			ScPlateresFace face = new ScPlateresFace();

			String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
					LimitObjType.SCPLATERESFACE, i);

			face.setGeometryId(geomId);
			face.setGroupId(this.command.getGroupId());
			face.setGeometry(this.command.getGeo());

			result.insertObject(face, ObjStatus.INSERT, geomId);
		}

		return null;
	}
}
