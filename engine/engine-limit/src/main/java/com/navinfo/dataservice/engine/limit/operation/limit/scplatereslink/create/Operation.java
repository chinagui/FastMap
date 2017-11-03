package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.create;

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
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;
import net.sf.json.JSONArray;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		if (this.command.getLinks() == null || this.command.getLinks().size() == 0) {
			throw new Exception("没有要素选中，请确定需要复制的要素");
		}

		Connection regionConn = null;

		if(array == null || array.size() == 0){
			throw new Exception("没有要素选中，请确定需要复制的要素");
		}
		
		try {
			regionConn = DBConnector.getInstance().getConnectionById(this.command.getDbId());

			RdLinkSelector selector = new RdLinkSelector(regionConn);

			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(this.command.getLinks(), Integer.class, JsonUtils.getJsonConfig());

			List<IRow> links = selector.loadByIds(pidList, true, false);

			Set<Integer> handlePids = getHandleLinkPids(this.command.getGroupId());

			int index = 0;

			for (IRow row : links) {

				RdLink rdLink = (RdLink) row;

				if (handlePids.contains(rdLink.pid())) {

					continue;
				}

				handlePids.add(rdLink.pid());

				ScPlateresLink link = new ScPlateresLink();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESLINK, index++);

				link.setGeometryId(geomId);

				link.setGroupId(this.command.getGroupId());

				link.setGeometry(rdLink.getGeometry());

				link.setLinkPid(rdLink.getPid());

				result.insertObject(link, ObjStatus.INSERT, geomId);
			}


		} catch (Exception e) {
			throw e;
		} finally {
			regionConn.close();
		}

		return null;
	}

	private Set<Integer> getHandleLinkPids(String groupId)  throws Exception
	{
		Set<Integer> handlePids = new HashSet<>();

		ScPlateresLinkSearch linkSearch = new ScPlateresLinkSearch(this.conn);

		List<Integer> pids = linkSearch.getLinkPidByGroupId(this.command.getGroupId());

		handlePids.addAll(pids);

		return handlePids;

	}

}
