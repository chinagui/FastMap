package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
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
			
			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(array, Integer.class, JsonUtils.getJsonConfig());

			List<IRow> links = selector.loadByIds(pidList, true, false);

			for (int i = 0; i < pidList.size(); i++) {

				ScPlateresLink link = new ScPlateresLink();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESLINK, i);

				RdLink currentLink = null;

				for (IRow row : links) {
					RdLink link2 = (RdLink)row;
					if (link2.getPid() == pidList.get(i)) {
						currentLink = link2;
						break;
					}
				}

				if (currentLink == null)
					continue;

				link.setGeometryId(geomId);

				link.setGroupId(this.command.getGroupId());

				link.setGeometry(currentLink.getGeometry());

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
