package com.navinfo.dataservice.engine.limit.test;

import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.operation.Transaction;
import com.navinfo.dataservice.engine.limit.search.RenderParam;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ly on 2017/9/21.
 */
public class operationTest extends ClassPathXmlAppContextInit {


    protected Logger log = Logger.getLogger(this.getClass());

    @Before
    public void before() {
        initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
    }


    @Test
    public void create01() throws Exception {

        String parameter = "{\"command\":\"BREAK\",\"data\":{\"longitude\":116.41681070171616,\"latitude\":39.91769143492466}}";



        Transaction t = new Transaction(parameter);

        String msg = t.run();


    }

    @Test
    public void create02() throws Exception {

        String parameter = "{ \"command\": \"CREATE\", \"type\": \"SCPLATERESINFO\", infos: [{ infoIntelId: \"ABC1528015\", infoCode: \"S0502815854\", adminCode: \"11000\", url: \"WWW.ACBCDEEEEE.COM\", newsTime: \"20170925\", infoContent: \"增加永久限行\", condition: \"S\", complete: 1, memo: \"00000\" }] }";


//        ScPlateresLink
//                ScPlateresFace
        //ScPlateresGeometry
        //ScPlateresInfo
        //ScPlateresManoeuvre
        //ScPlateresRdLink
        //ScPlateresInfo
        Transaction t = new Transaction(parameter);

        String msg = t.run();
    }


    @Test
    public void create03() throws Exception {

        String parameter = "{\"type\":\"SCPLATERESFACE\",\"command\":\"CREATE\",\"dbId\":13,\"data\":{\"groupId\":\"D1100000006\",\"geometryIds\":[\"D1100000006000057\",\"D1100000006000063\",\"D1100000006000064\",\"D1100000006000062\"]}}\n";

        Transaction t = new Transaction(parameter);

        String msg = t.run();
    }


    @Test
    public void delete01() throws Exception {

        String parameter = "{ \"command\": \"DELETE\", \"type\": \"SCPLATERESGROUP\",objIds: [\"S1100000016\"] }";

        Transaction t = new Transaction(parameter);

        String msg = t.run();
    }

    @Test
    public void update01() throws Exception {

        String parameter = "{ \"command\": \"UPDATE\", \"type\": \"SCPLATERESGROUP\",objId: \"S1100000011\", data: { infoIntelId: \"ABC1528010\", groupType: 2 ,principle:\"3\" ,\"objStatus\":\"UPDATE\"} }";
        Transaction t = new Transaction(parameter);

        String msg = t.run();
    }

    @Test
    public void render01() throws Exception {


        String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"SCPLATERESGEOMETRY\"],\"x\":1725427,\"y\":794151,\"z\":21}";
        JSONObject jsonReq = JSONObject.fromObject(parameter);

        try {

            JSONArray type = jsonReq.getJSONArray("types");

            RenderParam param = new RenderParam();

            param.setX(jsonReq.getInt("x"));

            param.setY(jsonReq.getInt("y"));

            param.setZ(jsonReq.getInt("z"));

            if (jsonReq.containsKey("gap")) {
                param.setGap(jsonReq.getInt("gap"));
            }

            List<LimitObjType> types = new ArrayList<>();

            for (int i = 0; i < type.size(); i++) {
                types.add(LimitObjType.valueOf(type.getString(i)));
            }

            JSONObject data = null;

            if (param.getZ() > 13) {
                com.navinfo.dataservice.engine.limit.search.SearchProcess p = new com.navinfo.dataservice.engine.limit.search.SearchProcess();

                data = p.searchDataByTileWithGap(types, param);
            }

        } catch (Exception e) {

        } finally {

        }
    }

    @Test
    public void render02() throws Exception {


        String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"SCPLATERESLINK\"],\"x\":107919,\"y\":49660,\"z\":17}";

        JSONObject jsonReq = JSONObject.fromObject(parameter);

        try {

            JSONArray type = jsonReq.getJSONArray("types");

            RenderParam param = new RenderParam();

            param.setX(jsonReq.getInt("x"));

            param.setY(jsonReq.getInt("y"));

            param.setZ(jsonReq.getInt("z"));

            if (jsonReq.containsKey("gap")) {
                param.setGap(jsonReq.getInt("gap"));
            }

            List<LimitObjType> types = new ArrayList<>();

            for (int i = 0; i < type.size(); i++) {
                types.add(LimitObjType.valueOf(type.getString(i)));
            }

            JSONObject data = null;

            if (param.getZ() > 13) {
                com.navinfo.dataservice.engine.limit.search.SearchProcess p = new com.navinfo.dataservice.engine.limit.search.SearchProcess();

                data = p.searchDataByTileWithGap(types, param);
            }

        } catch (Exception e) {

        } finally {

        }
    }


    @Test
    public void render03() throws Exception {


        String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"SCPLATERESFACE\"],\"x\":107917,\"y\":49661,\"z\":17}";

        JSONObject jsonReq = JSONObject.fromObject(parameter);

        try {

            JSONArray type = jsonReq.getJSONArray("types");

            RenderParam param = new RenderParam();

            param.setX(jsonReq.getInt("x"));

            param.setY(jsonReq.getInt("y"));

            param.setZ(jsonReq.getInt("z"));

            if (jsonReq.containsKey("gap")) {
                param.setGap(jsonReq.getInt("gap"));
            }

            List<LimitObjType> types = new ArrayList<>();

            for (int i = 0; i < type.size(); i++) {
                types.add(LimitObjType.valueOf(type.getString(i)));
            }

            JSONObject data = null;

            if (param.getZ() > 13) {
                com.navinfo.dataservice.engine.limit.search.SearchProcess p = new com.navinfo.dataservice.engine.limit.search.SearchProcess();

                data = p.searchDataByTileWithGap(types, param);
            }

        } catch (Exception e) {

        } finally {

        }
    }
}
