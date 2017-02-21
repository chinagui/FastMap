package com.navinfo.dataservice.engine.edit.zhangyuntao.lc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author zhangyt
 * @Title: LcLinkTest.java
 * @Description: TODO
 * @date: 2016年8月8日 下午1:35:39
 * @version: v1.0
 */
public class LcLinkTest extends InitApplication {

    public LcLinkTest() {
    }

    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"LCLINK\",\"data\":{\"eNodePid\":306000089," +
                "\"sNodePid\":310000082,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49999976158142,"
                + "39.93725055412644],[116.49999976158142,39.937550813896]]},\"catchLinks\":[{\"nodePid\":310000082,"
                + "\"seqNum\":0,\"lon\":116.49999976158142,\"lat\":39.93725055412644},{\"nodePid\":306000089," +
                "\"seqNum\":1,\"lon\":116.49999976158142,\"lat\":39.937550813896}]},\"dbId\":17}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"UPDATE\",\"dbId\":1003,\"type\":\"LCLINK\",\"objId\":130588," +
                "\"data\":{\"kinds\":[{\"kind\":3,\"rowId\":\"3B1913AE1DD1F1F1E050A8C08304C345\"," +
                "\"objStatus\":\"UPDATE\",\"form\":2}],\"pid\":130588}}";
        parameter = "{\"command\":\"UPDATE\",\"dbId\":1003,\"type\":\"LCLINK\",\"objId\":62662500," +
                "\"data\":{\"kinds\":[{\"kind\":2,\"rowId\":\"3B1913AE1EF0F1F1E050A8C08304C345\"," +
                "\"objStatus\":\"UPDATE\",\"form\":4}],\"pid\":62662500}}";
        TestUtil.run(parameter);
    }

    @Test
    public void testSearchByGap() {
        String parameter = "{\"projectId\":11,\"gap\":80,\"types\":[\"ADNODE\"],\"z\":17,\"x\":107945,\"y\":49615}";
        parameter = "{\"dbId\":43,\"gap\":80,\"types\":[\"LCLINK\"],\"z\":19,\"x\":431743,\"y\":198519}";
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        JSONArray type = jsonReq.getJSONArray("types");

        int dbId = jsonReq.getInt("dbId");

        int x = jsonReq.getInt("x");

        int y = jsonReq.getInt("y");

        int z = jsonReq.getInt("z");

        int gap = jsonReq.getInt("gap");

        List<ObjType> types = new ArrayList<ObjType>();

        for (int i = 0; i < type.size(); i++) {
            types.add(ObjType.valueOf(type.getString(i)));
        }

        try {
            SearchProcess p = new SearchProcess(DBConnector.getInstance().getConnectionById(dbId));
            JSONObject data = p.searchDataByTileWithGap(types, x, y, z, gap);

            System.out.println(ResponseUtils.assembleRegularResult(data));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void breakLcLink() {
        String parameter = "{\"command\":\"BREAK\",\"dbId\":42,\"objId\":100035081," +
                "\"data\":{\"longitude\":116.47200963815628,\"latitude\":40.07116277263092},\"type\":\"LCLINK\"}";
        parameter = "{\"command\":\"BREAK\",\"dbId\":17,\"objId\":204000032," +
                "\"data\":{\"longitude\":116.4714851975441,\"latitude\":39.875605676514986},\"type\":\"LCLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void print() throws Exception {
        String sql = "select t1.link_pid from lc_link t1 where not exists (select t2.link_pid from lc_link_kind t2 " +
                "where t1.link_pid = t2.link_pid)";
        sql = "select * from lu_link t1 where not exists (select t2.link_pid from lu_link_kind t2 where t1.link_pid =" +
                " t2.link_pid)";
        Connection conn = DBConnector.getInstance().getConnectionById(42);
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet resultSet = pstmt.executeQuery();
        while (resultSet.next()) {
            sql = "insert into lu_link_kind(link_pid, kind ,u_record , row_id) values(" + resultSet.getInt("link_pid") + ",0 ,0 ,'" + UUID.randomUUID().toString().replaceAll("-", "") + "');";
            System.out.println(sql);
        }

    }
}
