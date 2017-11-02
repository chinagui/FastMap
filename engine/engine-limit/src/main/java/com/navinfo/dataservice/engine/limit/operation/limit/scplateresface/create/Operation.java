package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.limit.Utils.PidApply;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;
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

		if (this.command.getGeometryIds()!=null)
		{
			line2Face( result,this.command.getGeometryIds());
			return null;
		}

		JSONArray arrayrd = this.command.getRdLinks();
		
		JSONArray arrayad = this.command.getAdLinks();

		Geometry geo = this.command.getGeo();
		
		if(arrayrd == null && arrayad == null && geo == null){
			throw new Exception("没有要素选中，请确定需要复制的要素");
		}
		
		if(arrayrd == null && arrayad == null && geo == null){
			throw new Exception("没有要素选中，请确定需要复制的要素");
		}
		
		if (arrayrd != null && arrayrd.size() != 0) {

			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(arrayrd, Integer.class, JsonUtils.getJsonConfig());

			createFaceByRdLinks(pidList, result);
		}
		if (arrayad != null && arrayad.size() != 0) {
			@SuppressWarnings("unchecked")
			List<Integer> pidList = JSONArray.toList(arrayad, Integer.class, JsonUtils.getJsonConfig());

			createFaceByAdLinks(pidList, result);
		}
		if (geo != null) {
			ScPlateresFace face = new ScPlateresFace();

			String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
					LimitObjType.SCPLATERESFACE, 0);

			face.setGeometryId(geomId);

			face.setGroupId(this.command.getGroupId());

			face.setGeometry(geo);

			result.insertObject(face, ObjStatus.INSERT, geomId);
		}

		return null;
	}
	
	private void createFaceByRdLinks(List<Integer> pidList, Result result) throws Exception {
		Connection regionConn = null;

		try {
			regionConn = DBConnector.getInstance().getConnectionById(this.command.getDbId());

			RdLinkSelector selector = new RdLinkSelector(regionConn);

			List<IRow> links = selector.loadByIds(pidList, true, false);

			Set<Integer> handlePids = getHandleLinkPids(this.command.getGroupId(),"RD_LINK");

			int index = 0;

			for (IRow row : links) {

				RdLink rdLink = (RdLink) row;

				if (handlePids.contains(rdLink.pid())) {

					continue;
				}

				handlePids.add(rdLink.pid());

				ScPlateresFace face = new ScPlateresFace();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESFACE,index++);


				face.setGeometryId(geomId);

				face.setGroupId(this.command.getGroupId());

				face.setGeometry(rdLink.getGeometry());

				face.setLinkPid(rdLink.getPid());

				face.setLinkType(rdLink.tableName().toUpperCase());

				result.insertObject(face, ObjStatus.INSERT, geomId);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			regionConn.close();
		}
	}
	
	private void createFaceByAdLinks(List<Integer> pidList, Result result) throws Exception {
		Connection regionConn = null;

		try {
			regionConn = DBConnector.getInstance().getConnectionById(this.command.getDbId());

			AdLinkSelector selector = new AdLinkSelector(regionConn);

			List<IRow> links = selector.loadByIds(pidList, true, false);

			Set<Integer> handlePids = getHandleLinkPids(this.command.getGroupId(),"AD_LINK");

			int index = 0;

			for (IRow row : links) {

				AdLink adlink = (AdLink) row;

				if (handlePids.contains(adlink.pid())) {

					continue;
				}

				handlePids.add(adlink.pid());

				ScPlateresFace face = new ScPlateresFace();

				String geomId = PidApply.getInstance(this.conn).pidForInsertGeometry(this.command.getGroupId(),
						LimitObjType.SCPLATERESFACE, index);

				face.setGeometryId(geomId);

				face.setGroupId(this.command.getGroupId());

				face.setGeometry(adlink.getGeometry());

				face.setLinkPid(adlink.getPid());

				face.setLinkType(adlink.tableName().toUpperCase());

				result.insertObject(face, ObjStatus.INSERT, geomId);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			regionConn.close();
		}
	}

	private void line2Face(Result result, List<String> geometryIds) throws Exception {

		ScPlateresFaceSearch search = new ScPlateresFaceSearch(this.conn);

		List<ScPlateresFace> allFaces = search.loadByGeometryIds(geometryIds);

		List<ScPlateresFace> faces = new ArrayList<>();

		for (ScPlateresFace face : allFaces) {

			if (face.getGeometry().getGeometryType().equals("LineString")) {

				faces.add(face);

			} else {
				throw new Exception(face.getGeometryId() + ":不是LineString类型");
			}
		}

		Line2Face line2Face = new Line2Face();

		line2Face.createFace(faces, result);
	}


	private Set<Integer> getHandleLinkPids(String groupId,String linkType)  throws Exception
	{
		Set<Integer> handlePids = new HashSet<>();

		ScPlateresFaceSearch linkSearch = new ScPlateresFaceSearch(this.conn);
		List<Integer> pids = linkSearch.getLinkPidByGroupId(this.command.getGroupId(),linkType);

		handlePids.addAll(pids);

		return handlePids;

	}
}
