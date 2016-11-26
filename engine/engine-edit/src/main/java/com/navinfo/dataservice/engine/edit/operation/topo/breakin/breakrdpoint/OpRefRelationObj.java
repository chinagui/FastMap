package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class OpRefRelationObj {

	private Connection conn = null;

	public OpRefRelationObj(Connection conn) {

		this.conn = conn;
	}

	/**
	 * 同一线
	 * 
	 * @param breakLink
	 * @param command
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public String handleSameLink(RdLink breakLink, Command command,
			Result result) throws Exception {
		
		breakLink.setGeometry(GeoTranslator.transform(breakLink.getGeometry(),
				1, 0));

		Map<IRow, Geometry> breakNodeMap = new HashMap<IRow, Geometry>();

		LinkedHashMap<IRow, Geometry> linkMap = new LinkedHashMap<IRow, Geometry>();
		
		Set<Integer> pidFlags = new HashSet<Integer>();

		pidFlags.add(breakLink.geteNodePid());

		pidFlags.add(breakLink.getsNodePid());

		for (RdLink link : command.getNewLinks()) {

			linkMap.put(link, GeoTranslator.transform(link.getGeometry(),
					1, 0));

			int sNodePid = link.getsNodePid();

			int eNodePid = link.geteNodePid();

			if (!pidFlags.contains(sNodePid) ) {
				
				RdNode node = new RdNode();

				node.setPid(sNodePid);

				LineString linkGeo = (LineString) link.getGeometry();

				node.setGeometry(linkGeo.getStartPoint());

				breakNodeMap.put(node, node.getGeometry());
				
				pidFlags.add(sNodePid);
			}

			if (!pidFlags.contains(eNodePid)) {
				
				RdNode node = new RdNode();

				node.setPid(eNodePid);

				LineString linkGeo = (LineString) link.getGeometry();

				node.setGeometry(linkGeo.getEndPoint());

				breakNodeMap.put(node, node.getGeometry());
				
				pidFlags.add(eNodePid);
			}
		}

		// 打断link维护同一线
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
				this.conn);
		operation.breakLink(breakLink, breakNodeMap, linkMap, result);

		return null;
	}

	/**
	 * 交限
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdRestriction(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 车信
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdLaneconnexity(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 语音引导
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdVoiceguide(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 分歧
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdBranch(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

	/**
	 * 顺行
	 * 
	 * @param result
	 * @param oldLink
	 * @param newLinks
	 * @return
	 * @throws Exception
	 */
	public String handleRdDirectroute(Result result, RdLink oldLink,
			List<RdLink> newLinks) throws Exception {

		com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update.Operation(
				conn);

		operation.breakRdLink(oldLink, newLinks, result);

		return null;
	}

}