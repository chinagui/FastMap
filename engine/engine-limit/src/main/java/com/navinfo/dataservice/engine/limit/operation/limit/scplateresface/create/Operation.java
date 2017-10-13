package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.vividsolutions.jts.geom.Geometry;

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

		Geometry geo = this.command.getGeo();

		int seq = 0;
		
		if (array != null && array.size() != 0) {
			seq = array.size();
			
			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(array, Integer.class, JsonUtils.getJsonConfig());

			createFaceByLinks(pidList, result);
		}
		if (geo != null) {
			ScPlateresFace face = new ScPlateresFace();

			String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
					LimitObjType.SCPLATERESFACE, seq);

			face.setGeometryId(geomId);

			face.setGroupId(this.command.getGroupId());

			face.setGeometry(geo);

			result.insertObject(face, ObjStatus.INSERT, geomId);
		}

		return null;
	}
	
	private void createFaceByLinks(List<Integer> pidList, Result result) throws Exception {
		Connection regionConn = null;

		try {
			regionConn = DBConnector.getInstance().getConnectionById(this.command.getDbId());

			RdLinkSelector selector = new RdLinkSelector(regionConn);

			List<RdLink> links = selector.loadByPids(pidList, true);

			for (int i = 0; i < pidList.size(); i++) {

				ScPlateresFace face = new ScPlateresFace();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESFACE, i);

				RdLink currentLink = null;

				for (RdLink link : links) {
					if (link.getPid() == pidList.get(i)) {
						currentLink = link;
						break;
					}
				}

				if (currentLink == null)
					continue;

				face.setGeometryId(geomId);

				face.setGroupId(this.command.getGroupId());

				face.setGeometry(currentLink.getGeometry());

				result.insertObject(face, ObjStatus.INSERT, geomId);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			regionConn.close();
		}
	}
}
