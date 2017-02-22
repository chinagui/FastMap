package com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.eleceye.RdElectroniceyeSelector;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.search.SearchProcess;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EleceyeTest extends InitApplication {

    @Override
    @Before
    public void init() {
        super.initContext();
    }

    @Test
    public void createEleceye() {
        // String requester = "{'dbId':42,'command':'CREATE','type':'RDELECTRONICEYE','data':{'direct':3,
        // 'longitude':116.50291868782932,'latitude':40.01112911418436,'linkPid':85518434}}";
        String requester = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"RDELECTRONICEYE\"," +
                "\"data\":{\"linkPid\":304001064,\"direct\":2,\"longitude\":116.17265,\"latitude\":40.56482}}";
        TestUtil.run(requester);
    }

    @Test
    public void moveEleceye() {
        String requester = "{\"command\":\"MOVE\",\"type\":\"RDELECTRONICEYE\",\"dbId\":17," +
                "\"data\":{\"linkPid\":\"201003497\",\"pid\":\"303000038\",\"latitude\":\"40.12799\"," +
                "\"longitude\":\"116.30923\"}}";
        TestUtil.run(requester);
    }

    @Test
    public void updateEleceye() {
        String requester = "{\"command\":\"UPDATE\",\"type\":\"RDELECTRONICEYE\",\"dbId\":42," +
                "\"data\":{\"location\":4,\"rowId\":\"3524E60474A46E1AE050A8C08304BA17\",\"pid\":32943645," +
                "\"objStatus\":\"UPDATE\"}}";
        TestUtil.run(requester);
    }

    @Test
    public void deleteEleceye() {
        String requester = "{'dbId':43,'command':'DELETE','type':'RDELECTRONICEYE','objId':100281916}";
        TestUtil.run(requester);
    }

    @Test
    public void getEleceye() {
        try {
            Connection conn = DBConnector.getInstance().getConnectionById(42);
            RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
            RdElectroniceye eleceye = (RdElectroniceye) selector.loadById(46800247, false);
            eleceye.getPairs();
            eleceye.getParts();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getEleceyes() {
        try {
            Connection conn = DBConnector.getInstance().getConnectionById(43);
            RdElectroniceyeSelector selector = new RdElectroniceyeSelector(conn);
            List<RdElectroniceye> list = selector.loadListByRdLinkId(13677569, false);
            for (RdElectroniceye eleceye : list) {
                System.out.println(eleceye.pid());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSearch() throws Exception {
        String parameter = "{\"dbId\":42,\"type\":\"RDELECTRONICEYE\",\"pid\":100281928}";
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        String objType = jsonReq.getString("type");

        int dbId = jsonReq.getInt("dbId");

        int pid = jsonReq.getInt("pid");
        SearchProcess p = new SearchProcess(DBConnector.getInstance().getConnectionById(dbId));
        List<ObjType> list = new ArrayList<ObjType>();
        list.add(ObjType.valueOf(objType));

        JSONObject json = p.searchDataByTileWithGap(list, 107947, 49592, 17, 80);
        System.out.println(json);

        IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);
        // System.out.println(obj.Serialize(ObjLevel.FULL));
    }

    @Test
    public void testSearchById() throws Exception {
        String parameter = "{\"dbId\":42,\"type\":\"RDELECTRONICEYE\",\"pid\":100281943}";
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        String objType = jsonReq.getString("type");

        int dbId = jsonReq.getInt("dbId");

        Connection conn = DBConnector.getInstance().getConnectionById(dbId);

        int pid = jsonReq.getInt("pid");

        SearchProcess p = new SearchProcess(conn);

        IObj obj = p.searchDataByPid(ObjType.valueOf(objType), pid);

        obj.Serialize(ObjLevel.FULL);
    }

    @Test
    public void testSerialize() {
        RdElectroniceye eye = new RdElectroniceye();
        eye.setPid(1);
        eye.setCreationDate(null);
        try {
            System.out.println(eye.Serialize(ObjLevel.FULL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchGap() throws Exception {
        String parameter = "{\"dbId\":42,\"gap\":80,\"types\":[\"RDELECTRONICEYE\"],\"z\":17,\"x\":107945,\"y\":49586}";

        Connection conn = null;

        JSONObject jsonReq = JSONObject.fromObject(parameter);

        JSONArray type = jsonReq.getJSONArray("types");

        int dbId = jsonReq.getInt("dbId");

        int x = jsonReq.getInt("x");

        int y = jsonReq.getInt("y");

        int z = jsonReq.getInt("z");

        int gap = 0;

        if (jsonReq.containsKey("gap")) {
            gap = jsonReq.getInt("gap");
        }

        List<ObjType> types = new ArrayList<ObjType>();

        for (int i = 0; i < type.size(); i++) {
            types.add(ObjType.valueOf(type.getString(i)));
        }

        JSONObject data = null;

        if (z <= 16) {

            List<ObjType> tileTypes = new ArrayList<ObjType>();

            List<ObjType> gdbTypes = new ArrayList<ObjType>();

            for (ObjType t : types) {
                if (t == ObjType.RDLINK || t == ObjType.ADLINK || t == ObjType.RWLINK) {
                    tileTypes.add(t);
                } else {
                    gdbTypes.add(t);
                }
            }

            if (!gdbTypes.isEmpty()) {

                conn = DBConnector.getInstance().getConnectionById(dbId);

                SearchProcess p = new SearchProcess(conn);

                JSONObject jo = p.searchDataByTileWithGap(gdbTypes, x, y, z, gap);

                if (data == null) {
                    data = new JSONObject();
                }

                data.putAll(jo);
            }

            if (!tileTypes.isEmpty()) {
                JSONObject jo = new JSONObject();

                if (data == null) {
                    data = new JSONObject();
                }

                data.putAll(jo);
            }

        } else {
            conn = DBConnector.getInstance().getConnectionById(dbId);

            SearchProcess p = new SearchProcess(conn);

            data = p.searchDataByTileWithGap(types, x, y, z, gap);

        }

        System.out.println(ResponseUtils.assembleRegularResult(data));
    }

}
