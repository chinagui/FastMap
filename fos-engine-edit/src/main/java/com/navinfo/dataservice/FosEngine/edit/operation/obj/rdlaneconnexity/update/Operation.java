package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlaneconnexity.update;

import java.sql.Connection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.service.PidService;

public class Operation implements IOperation {

	private Command command;

	private RdLaneConnexity lane;

	private Connection conn;

	public Operation(Command command, RdLaneConnexity lane, Connection conn) {
		this.command = command;

		this.lane = lane;

		this.conn = conn;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(lane, ObjStatus.DELETE);

				return null;
			} else {

				boolean isChanged = lane.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(lane, ObjStatus.UPDATE);
				}
			}
		}

		if (content.containsKey("topos")) {
			JSONArray topos = content.getJSONArray("topos");

			for (int i = 0; i < topos.size(); i++) {

				JSONObject json = topos.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							json.getString("objStatus"))) {

						RdLaneTopology topo = lane.topologyMap.get(json
								.getInt("pid"));

						if (topo == null) {
							throw new Exception("pid=" + json.getInt("pid")
									+ "的rd_lane_topology不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								json.getString("objStatus"))) {
							result.insertObject(topo, ObjStatus.DELETE);

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								json.getString("objStatus"))) {

							boolean isChanged = topo.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(topo, ObjStatus.UPDATE);
							}
						}
					} else {
						RdLaneTopology topo = new RdLaneTopology();

						topo.Unserialize(json);

						topo.setPid(PidService.getInstance()
								.applyLaneTopologyPid());

						topo.setConnexityPid(lane.getPid());
						
						topo.setMesh(lane.mesh());

						result.insertObject(topo, ObjStatus.INSERT);

						continue;
					}
				}

				if (json.containsKey("vias")) {
					JSONArray vias = content.getJSONArray("vias");

					for (int j = 0; j < vias.size(); j++) {

						JSONObject viajson = vias.getJSONObject(i);

						if (viajson.containsKey("objStatus")) {

							if (!ObjStatus.INSERT.toString().equals(
									viajson.getString("objStatus"))) {

								RdLaneVia via = lane.viaMap.get(viajson
										.getString("rowId"));

								if (via == null) {
									throw new Exception("rowId="
											+ viajson.getString("rowId")
											+ "的rd_lane_via不存在");
								}

								if (ObjStatus.DELETE.toString().equals(
										viajson.getString("objStatus"))) {
									result.insertObject(via, ObjStatus.DELETE);

									continue;
								} else if (ObjStatus.UPDATE.toString().equals(
										viajson.getString("objStatus"))) {

									boolean isChanged = via
											.fillChangeFields(viajson);

									if (isChanged) {
										result.insertObject(via,
												ObjStatus.UPDATE);
									}
								}
							} else {
								RdLaneVia via = new RdLaneVia();

								via.Unserialize(viajson);

								via.setTopologyId(json.getInt("pid"));
								
								via.setMesh(lane.mesh());

								result.insertObject(via, ObjStatus.INSERT);

								continue;
							}
						}

					}
				}
			}
		}

		return null;

	}

}
