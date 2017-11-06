package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;

public class Command extends AbstractCommand{
	private String requester;

	private int limitDir = 0;

	public int getLimitDir() {
		return limitDir;
	}

	private List<ScPlateresRdLink> links;

	public List<ScPlateresRdLink> getLinks() {
		return links;
	}

	public void setLinks(List<ScPlateresRdLink> rdlinks) {
		this.links = rdlinks;
	}

	private Map<Integer, Set<String>> mapping = new HashMap<>();

	public Map<Integer, Set<String>>  getMapping() {
		return mapping;
	}

	public Command(JSONObject json, String requester) {

		this.requester = requester;

		JSONArray data = json.getJSONArray("data");

		for (int i = 0; i < data.size(); i++) {

			JSONObject obj = data.getJSONObject(i);

			if (!obj.containsKey("linkPid") || !obj.containsKey("geometryId")) {
				continue;
			}

			String geometryId = obj.getString("geometryId");

			int linkPid = obj.getInt("linkPid");

			if (!mapping.containsKey(linkPid)) {
				mapping.put(linkPid, new HashSet<String>());
			}

			mapping.get(linkPid).add(geometryId);
		}

		this.limitDir = json.getInt("limitDir");
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public DbType getDbType() {
		return DbType.METADB;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		return LimitObjType.SCPLATERESLINK;
	}
}
