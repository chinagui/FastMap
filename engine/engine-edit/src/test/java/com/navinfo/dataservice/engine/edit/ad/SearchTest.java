package com.navinfo.dataservice.engine.edit.ad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.xiaolong.InitApplication;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

public class SearchTest extends InitApplication{
	
	@Override
	@Before
	public void init() {
		initContext();
	}
	
	private Connection conn;
	private RdLinkSelector linkSelector;
	public SearchTest() throws Exception{
		this.conn = DBConnector.getInstance().getConnectionById(11);
		 linkSelector = new RdLinkSelector(conn);
	}
	
	@Test
	public List<RdLink> getNextTrackLinks(int cuurentLinkPid,int cruuentNodePidDir) throws Exception{
		List<RdLink> tracks = new ArrayList<RdLink>();
		tracks.add((RdLink) linkSelector.loadById(cuurentLinkPid, true));
		List<RdLink> nextLinks = linkSelector.loadTrackLink(cuurentLinkPid, cruuentNodePidDir, true);
		while (nextLinks.size() > 0 && tracks.size() <= 999 ){
			RdLink currentLink = (RdLink) linkSelector.loadById(cuurentLinkPid, true);
			Geometry currentGeometry = currentLink.getGeometry();
			LineSegment currentLinklineSegment = null;
			if(currentLink.getsNodePid() == cruuentNodePidDir){
				currentLinklineSegment = new LineSegment(currentGeometry.getCoordinates()[currentGeometry.getCoordinates().length-2], 
						currentGeometry.getCoordinates()[currentGeometry.getCoordinates().length-1]);
			}if(currentLink.geteNodePid() == cruuentNodePidDir){
				currentLinklineSegment = new LineSegment(currentGeometry.getCoordinates()[1], 
						currentGeometry.getCoordinates()[0]);
			} 
		
		    Map<Double,RdLink>  map = new HashMap<Double,RdLink>();
			for(RdLink ad: nextLinks){
				LineSegment nextLinklineSegment = null;
				Geometry nextgeometry = ad.getGeometry();
				nextLinklineSegment = new LineSegment(nextgeometry.getCoordinates()[0], 
						nextgeometry.getCoordinates()[1]) ;
				double minAngle =  AngleCalculator.getAngle(currentLinklineSegment, nextLinklineSegment);
				if(map.size()  > 0){
					if(map.keySet().iterator().next() < minAngle){
						map.clear();
						map.put(minAngle, ad);
					}
						
				}else{
					map.put(minAngle, ad);
				}
			}
			RdLink link = map.values().iterator().next();
			cuurentLinkPid = link.getPid();
			if (link.getDirect()  == 2){
				cruuentNodePidDir = link.geteNodePid();
			}if(link.getDirect()  == 3){
				cruuentNodePidDir = link.getsNodePid();
			}if (link.getDirect()  == 1){
				cruuentNodePidDir =(cruuentNodePidDir == link.getsNodePid())? link.geteNodePid():link.getsNodePid();
			}
			tracks.add(link);
			nextLinks = linkSelector.loadTrackLink(cuurentLinkPid, cruuentNodePidDir, true);
		}
		return tracks;
	}

}
