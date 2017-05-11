package com.navinfo.dataservice.dao.glm.search;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CmgBuildlinkSearch
 * @Package: com.navinfo.dataservice.dao.glm.search
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class CmgBuildlinkSearch implements ISearch {

    /**
     * 数据库链接
     */
    private Connection conn;

    public CmgBuildlinkSearch(Connection conn) {
        this.conn = conn;
    }

    /**
     * 通过pid获取数据
     *
     * @param pid
     * @return
     * @throws Exception
     */
    @Override
    public IObj searchDataByPid(int pid) throws Exception {
        return (IObj) new AbstractSelector(CmgBuildlink.class, conn).loadById(pid, false);
    }

    /**
     * 通过pids获取数据(框选功能)
     *
     * @param pidList@return
     * @throws Exception
     */
    @Override
    public List<? extends IObj> searchDataByPids(List<Integer> pidList) throws Exception {
        return null;
    }

    /**
     * 通过范围获取数据
     *
     * @param wkt
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataBySpatial(String wkt) throws Exception {
        return null;
    }

    /**
     * 通过条件获取数据
     *
     * @param condition
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByCondition(String condition) throws Exception {
        return null;
    }

    /**
     * 通过瓦片号+缝隙获取数据
     *
     * @param x
     * @param y
     * @param z
     * @param gap
     * @return
     * @throws Exception
     */
    @Override
    public List<SearchSnapshot> searchDataByTileWithGap(int x, int y, int z, int gap) throws Exception {
        List<SearchSnapshot> list = new ArrayList<>();
        String sql = "select t.link_pid, t.geometry, t.s_node_pid, t.e_node_pid from cmg_buildlink t where sdo_within_distance("
                + "t.geometry, sdo_geometry(:1, 8307), 'DISTANCE=0') = 'TRUE' and t.u_record <> 2";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            String wkt = MercatorProjection.getWktWithGap(x, y, z, gap);
            pstmt.setString(1, wkt);
            resultSet = pstmt.executeQuery();
            double px = MercatorProjection.tileXToPixelX(x);
            double py = MercatorProjection.tileYToPixelY(y);
            while (resultSet.next()) {
                SearchSnapshot snapshot = new SearchSnapshot();
                JSONObject m = new JSONObject();
                m.put("a", resultSet.getInt("s_node_pid"));
                m.put("b", resultSet.getInt("e_node_pid"));
                snapshot.setM(m);
                snapshot.setT(51);
                snapshot.setI(resultSet.getInt("link_pid"));
                STRUCT struct = (STRUCT) resultSet.getObject("geometry");
                JGeometry geo = JGeometry.load(struct);
                if (geo.getType() != 2) {
                    continue;
                }
                JSONObject geojson = Geojson.spatial2Geojson(struct);
                JSONObject jo = Geojson.link2Pixel(geojson, px, py, z);
                snapshot.setG(jo.getJSONArray("coordinates"));
                list.add(snapshot);
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return list;
    }
    
	public List<SearchSnapshot> searchDataByLinkPids(List<Integer> pids)
			throws Exception {

		List<SearchSnapshot> list = new ArrayList<SearchSnapshot>();

		if (null == pids || pids.size() == 0 || pids.size() > 1000) {

			return list;
		}

		String ids = org.apache.commons.lang.StringUtils.join(pids, ",");

		String sql = "SELECT T.LINK_PID, T.GEOMETRY, T.S_NODE_PID, T.E_NODE_PID FROM CMG_BUILDLINK T WHERE LINK_PID IN ("
				+ ids + ") AND T.U_RECORD <> 2";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				SearchSnapshot snapshot = new SearchSnapshot();

				JSONObject m = new JSONObject();

				m.put("a", resultSet.getInt("s_node_pid"));

				m.put("b", resultSet.getInt("e_node_pid"));

				snapshot.setM(m);

				snapshot.setT(51);

				snapshot.setI(resultSet.getInt("link_pid"));

				STRUCT struct = (STRUCT) resultSet.getObject("geometry");

				JSONObject geojson = Geojson.spatial2Geojson(struct);

				snapshot.setG(geojson.getJSONArray("coordinates"));

				list.add(snapshot);
			}
		} catch (Exception e) {

			throw new Exception(e);

		} finally {

			DbUtils.closeQuietly(resultSet);

			DbUtils.closeQuietly(pstmt);
		}

		return list;
	}
}
