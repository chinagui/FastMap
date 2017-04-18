package com.navinfo.dataservice.dao.glm.search;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ISearch;
import com.navinfo.dataservice.dao.glm.iface.SearchSnapshot;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildnode;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CmgBuildnodeSearch
 * @Package: com.navinfo.dataservice.dao.glm.search
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/13
 * @Version: V1.0
 */
public class CmgBuildnodeSearch implements ISearch {

    /**
     * 数据库链接
     */
    private Connection conn;

    public CmgBuildnodeSearch(Connection conn) {
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
        return (IObj) new AbstractSelector(CmgBuildnode.class, conn).loadById(pid, false);
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
        String sql = "with tmp1 as (select node_pid, geometry from cmg_buildnode where sdo_relate(geometry, sdo_geometry(:1, 8307), "
                + "'mask=anyinteract') = 'TRUE' and u_record <> 2), tmp2 as (select /*+ index(a) */ b.node_pid, listagg(a.link_pid, ',') "
                + "within group(order by b.node_pid) linkpids from cmg_buildlink a, tmp1 b where a.u_record <> 2 and (a.s_node_pid = b"
                + ".node_pid or a.e_node_pid = b.node_pid) group by b.node_pid) select a.node_pid, a.geometry, b.linkpids from tmp1 a, "
                + "tmp2 b where a.node_pid = b.node_pid";
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
                m.put("a", resultSet.getString("linkpids"));
                snapshot.setM(m);
                snapshot.setT(52);
                snapshot.setI(resultSet.getInt("node_pid"));
                STRUCT struct = (STRUCT) resultSet.getObject("geometry");
                JSONObject geojson = Geojson.spatial2Geojson(struct);
                Geojson.point2Pixel(geojson, z, px, py);
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
