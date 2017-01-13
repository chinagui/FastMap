package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

//GLM56004	修形中产生自相交，要提示立交或打断	修形中产生自相交，要提示立交或打断	
//1	LC_LINK、RW_LINK、RD_LINK、CMG_BUILDLINK、ADAS_LINK、AD_LINK、ZONE_LINK、LU_LINK	新增link

public class GLM56004 extends baseRule {

	public GLM56004() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			ObjType objType = obj.objType();
			Geometry geo = null;
			String tableName = null;
			int linkPid = 0;
			switch (objType) {
			case RDLINK:
				RdLink link = (RdLink) obj;
				geo = link.getGeometry();
				tableName = link.tableName();
				linkPid = link.getPid();
				break;
			case RWLINK:
				RwLink rwLink = (RwLink) obj;
				geo = rwLink.getGeometry();
				tableName = rwLink.tableName();
				linkPid = rwLink.getPid();
				break;
			case LCLINK:
				LcLink lcLink = (LcLink) obj;
				geo = lcLink.getGeometry();
				tableName = lcLink.tableName();
				linkPid = lcLink.getPid();
				break;
			default:
				break;
			}
			
			if(geo != null && tableName != null)
			{
				List<Point> inters = new ArrayList<Point>();
				boolean isSampleLine = GeoHelper.isSample(geo, inters);
				if (isSampleLine) {
					return;
				}

				// 自相交的点是否建立立交
				String sql = "SELECT G.*" + "  FROM RD_GSC G, RD_GSC_LINK L" + " WHERE G.PID = L.PID"
						+ "   AND L.TABLE_NAME = '"+tableName.toUpperCase()+"' AND G.U_RECORD != 2 AND L.U_RECORD != 2 " + "   AND L.LINK_PID="
						+ linkPid;
				RdGscSelector gscSelector = new RdGscSelector(getConn());
				List<RdGsc> gscList = gscSelector.loadBySql(sql, false);

				List<Point> intersError = new ArrayList<Point>();
				intersError.addAll(inters);

				if (gscList.size() > 0) {
					for (int i = 0; i < inters.size(); i++) {
						Point pptmp = inters.get(i);
						for (int j = 0; j < gscList.size(); j++) {
							RdGsc gscTmp = gscList.get(j);
							Point gscPoint = (Point) gscTmp.getGeometry();
							if (GeoHelper.isPointEquals(pptmp.getX(), pptmp.getY(), gscPoint.getX(), gscPoint.getY())) {
								intersError.remove(pptmp);
							}
						}
					}
				}
				if (intersError.size() > 0) {
					this.setCheckResult(intersError.get(0), "["+tableName.toUpperCase()+"," + linkPid + "]", obj.mesh());
				}
			}
		}
	}
}
