package com.navinfo.dataservice.engine.edit.operation.topo.topobreakin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.operation.topo.topobreakin.Command;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private static final GeometryFactory geoFactory = new GeometryFactory();
	private Check check = new Check(this.getConn());

	public Process(Command command, Result result, Connection conn) throws Exception {
		super();
		this.setCommand(command);
		this.setResult(result);
		this.setConn(conn);
		this.initCheckCommand();
	}

	@Override
	public String exeOperation() throws Exception {
		//执行打断前的检查
		check.checkHasGSCinLine(this.getCommand());
		check.checkLineRelationWithPassLine(this.getCommand());
		check.checkLineRelationWithInOutLine(this.getCommand());
		
		return new Operation(this.getCommand(), this.getConn()).run(this.getResult());
	}

	@Override
	public boolean prepareData() throws Exception {
		RdLinkSelector selector = new RdLinkSelector(this.getConn());

		// 如果nodepid是某条link的起终点，从需打断link中排除,其余线需根据node修正几何
		int nodePid = this.getCommand().getBreakNodePid();
		List<RdLink> needBreakLink = new ArrayList<>();

		if (nodePid == 0) {
			return true;
		}
		for (int pid : this.getCommand().getLinkPids()) {
			RdLink link = (RdLink) selector.loadById(pid, false);
			if (link.getsNodePid() == nodePid || link.geteNodePid() == nodePid) {
				this.getCommand().getLinkPids().remove(pid);
				continue;
			}
			needBreakLink.add(link);
		}

		modifyLinkGeo(nodePid, needBreakLink);

		return true;
	}

	public String innerRun() throws Exception {
		String msg;
		try {
			this.prepareData();

			IOperation operation = new Operation(this.getCommand(), this.getConn());

			msg = operation.run(this.getResult());

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	/**
	 * 根据node点位信息，更新link的几何
	 * 
	 * @param nodePid
	 * @param rdLinks
	 * @throws Exception
	 */
	private void modifyLinkGeo(int nodePid, List<RdLink> rdLinks) throws Exception {
		if (nodePid == 0 || rdLinks.size() == 0) {
			return;
		}

		Coordinate breakPoint = new Coordinate(this.getCommand().getBreakPoint().getX(),
				this.getCommand().getBreakPoint().getY());
		
		for (RdLink rdLink : rdLinks) {
			Coordinate pedalCoor = GeometryUtils.getLinkPedalPointOnLine(breakPoint, rdLink.getGeometry());
			Point pedalPoint = (Point) GeoTranslator.transform(geoFactory.createPoint(pedalCoor), 1, 5);

			/*Set<Point> points = new HashSet<>();
			points.add(pedalPoint);
			LineString geo = GeoTranslator
					.getReformLineString(geoFactory.createLineString(rdLink.getGeometry().getCoordinates()), points);*/
			LineString geo = this.reformGeomtryByNode(rdLink, breakPoint, pedalCoor);
			
			rdLink.setGeometry(geo);
		}
	}

	private LineString reformGeomtryByNode(RdLink link, Coordinate breakPoint, Coordinate pedalCoor) throws Exception {
		Coordinate[] coordinates = link.getGeometry().getCoordinates();
		List<Coordinate> coors = new ArrayList<Coordinate>();
		Collections.addAll(coors, coordinates);

		for (int i = 0; i < coors.size() - 1; i++) {
			Coordinate pointS = coors.get(i);
			Coordinate pointE = coors.get(i + 1);

			// 是否在形状点上
			if ((Math.abs(pedalCoor.x - pointE.x) < 0.0000001 && Math.abs(pedalCoor.y - pointE.y) < 0.0000001)
					|| (GeoTranslator.isIntersection(new double[] { pointS.x, pointS.y },
							new double[] { pointE.x, pointE.y }, new double[] { pedalCoor.x, pedalCoor.y }))) {
				coors.add(i + 1, breakPoint);
				break;
			}
		}
		Coordinate[] c = (Coordinate[]) coors.toArray(new Coordinate[coors.size()]);
		return (LineString) GeoTranslator.transform(geoFactory.createLineString(c), 0.00001, 5);
	}
	
	@Override
	public String preCheck() throws Exception {
		return super.preCheck();
	}
}
