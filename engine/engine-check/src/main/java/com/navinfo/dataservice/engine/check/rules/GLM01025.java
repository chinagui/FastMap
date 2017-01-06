package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @author songdongyan
 * @ClassName: GLM01025
 * @date 下午2:32:17
 * @Description:link的起点应该与该link的第一个形状点坐标一致，否则报err；link的终点应该与该link的最后一个形状点坐标一致，否则报err；
 */
public class GLM01025 extends baseRule {

    /* (non-Javadoc)
     * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
     */
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
     */
    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

        String sql = "select a.node_pid from rd_node a where a.node_pid = :1 "
                + "and a.geometry.sdo_point.x = :2 "
                + "and a.geometry.sdo_point.y = :3 "
                + "AND A.U_RECORD != 2 "
                + "union all "
                + "select a.node_pid from rd_node a where a.node_pid = :4 "
                + "and a.geometry.sdo_point.x = :5 and a.geometry.sdo_point.y = :6 "
                + "AND A.U_RECORD != 2";

        List<IRow> objList = checkCommand.getGlmList();

        for (int i = 0; i < objList.size(); i++) {
            IRow obj = objList.get(i);
            if (obj instanceof RdLink) {
                RdLink rdLink = (RdLink) obj;
                Geometry geo = GeoTranslator.transform(rdLink.getGeometry(), 0.00001, 5);
                Coordinate[] coords = geo.getCoordinates();

                double sx, sy, ex, ey;
                //逆方向
                if (rdLink.getDirect() == 3) {
                    sx = coords[coords.length - 1].x;
                    sy = coords[coords.length - 1].y;
                    ex = coords[0].x;
                    ey = coords[0].y;
                }
                //顺方向
                else {
                    ex = coords[coords.length - 1].x;
                    ey = coords[coords.length - 1].y;
                    sx = coords[0].x;
                    sy = coords[0].y;
                }
                PreparedStatement pstmt = getConn().prepareStatement(sql);
                pstmt.setInt(1, rdLink.getsNodePid());
                pstmt.setDouble(2, sx);
                pstmt.setDouble(3, sy);
                pstmt.setInt(4, rdLink.geteNodePid());
                pstmt.setDouble(5, ex);
                pstmt.setDouble(6, ey);

                ResultSet resultSet = pstmt.executeQuery();

                boolean hasEnode = false;
                boolean hasSnode = false;

                while (resultSet.next()) {
                    int nodePid = resultSet.getInt("node_pid");

                    if (nodePid == rdLink.getsNodePid()) {
                        hasSnode = true;
                    } else if (nodePid == rdLink.geteNodePid()) {
                        hasEnode = true;
                    }
                }

                resultSet.close();
                pstmt.close();
                if (!hasEnode || !hasSnode) {
                    this.setCheckResult(rdLink.getGeometry(), "[RD_LINK," + rdLink.getPid() + "]", rdLink.getMeshId());
                    return;
                }
            }

        }
    }


    public static void main(String[] args) throws Exception {
        RdLink link = new RdLink();
        String str = "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844]," +
                "[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
        JSONObject geometry = JSONObject.fromObject(str);
        Geometry geometry2 = GeoTranslator.geojson2Jts(geometry, 1, 5);
        link.setGeometry(geometry2);
        link.setPid(1);
        link.setsNodePid(2);
        link.seteNodePid(2);
        List<IRow> objList = new ArrayList<IRow>();
        objList.add(link);

        //检查调用
        CheckCommand checkCommand = new CheckCommand();
        checkCommand.setGlmList(objList);
        checkCommand.setOperType(OperType.CREATE);
        checkCommand.setObjType(link.objType());

        CheckEngine checkEngine = new CheckEngine(checkCommand);
        checkEngine.postCheck();

//		Connection conn = GlmDbPoolManager.getInstance().getConnection(checkCommand.getProjectId());
//		GLM01025 glm=new GLM01025();
//		glm.setConn(conn);
//		glm.postCheck(checkCommand);	
//		List<NiValException> checkResultList=glm.getCheckResultList();
//		for(NiValException ni:checkResultList){
//			System.out.println(ni.getRuleId());
//			System.out.println(ni.getLoc());
//		}
    }

}
