package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyt
 * @Title: RdLinkTest.java
 * @Description: TODO
 * @date: 2016年8月3日 上午10:32:29
 * @version: v1.0
 */
public class RdLinkTest extends InitApplication {

    public RdLinkTest() {
    }

    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void testUpdate() {
        String parameter = "{\"command\":\"REPAIR\",\"dbId\":84,\"objId\":501000051," +
                "\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.65184,39.22191]," +
                "[117.651958912611,39.22178952541354],[117.6521,39.22169]]},\"catchInfos\":[]},\"type\":\"RDLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSPEEDLIMIT\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"pid\":500000001," +
                "\"direct\":3,\"linkPid\":49913063,\"longitude\":116.53538352127916,\"latitude\":39.7420088702255," +
                "\"objStatus\":\"UPDATE\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void repair() {
        String parameter = "{\"command\":\"REPAIR\",\"type\":\"RDLINK\",\"objId\":501000385,\"dbId\":13,\"subtaskId\":64," +
                "\"data\":{\"type\":\"RDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[113.35777,36.50017]," +
                "[113.35765376687048,36.50000001391655]]},\"catchInfos\":[{\"nodePid\":404000309,\"longitude\":113.35765376687048," +
                "\"latitude\":36.50000001391655}]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"dbId\":13,\"subtaskId\":64,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[113.35854828357697,36.49987172513926]," +
                "[113.35866958964716,36.49968102838193],[113.35877518676702,36.4996838374521],[113.35886126245879,36.499709851459535]," +
                "[113.35898548364639,36.499776855990774],[113.35890676655131,36.49980935377677],[113.35874972504348,36.49982479307441]," +
                "[113.35865288972855,36.49981674416965],[113.35854828357697,36.49972295301371],[113.35838600993156,36.49975206062599]," +
                "[113.358306966027,36.499753343393174],[113.35825438068713,36.4997622850147],[113.35818618535995,36.499721874953785]]}," +
                "\"catchLinks\":[{\"linkPid\":410000406,\"lon\":113.35866958964716,\"lat\":36.49968102838193},{\"linkPid\":406000376," +
                "\"lon\":113.35877518676702,\"lat\":36.4996838374521},{\"linkPid\":504000388,\"lon\":113.35886126245879," +
                "\"lat\":36.499709851459535},{\"linkPid\":404000368,\"lon\":113.35890676655131,\"lat\":36.49980935377677}," +
                "{\"linkPid\":508000425,\"lon\":113.35874972504348,\"lat\":36.49982479307441},{\"linkPid\":501000400," +
                "\"lon\":113.358306966027,\"lat\":36.499753343393174},{\"linkPid\":501000400,\"lon\":113.35825438068713," +
                "\"lat\":36.4997622850147}]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void depart() {
        String parameter = "{\"command\":\"DEPART\",\"dbId\":17,\"objId\":302002751,\"data\":{\"catchNodePid\":0," +
                "\"catchLinkPid\":0,\"linkPid\":300003552,\"longitude\":116.3114833831787," +
                "\"latitude\":40.11762904792049},\"type\":\"RDLINK\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void search() {
        String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"RDLINK\"],\"x\":53983,\"y\":24870,\"z\":16}";
        try {
            TestSearch.testSearchGap(parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updownDepart() {
        String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\"," +
                "\"data\":{\"linkPids\":[209000217]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":13,\"distance\":9.9," +
                "\"data\":{\"linkPids\":[406000241]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":304000034,\"data\":{\"longitude\":116.37564,"
                + "\"latitude\":38.51548},\"type\":\"ZONENODE\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void batch() {
        String requester = "{\"command\":\"BATCH\",\"type\":\"RDLINK\",\"dbId\":84," +
                "\"data\":[{\"forms\":[{\"linkPid\":509000294,\"formOfWay\":1,\"extendedForm\":0,\"auxiFlag\":0," +
                "\"kgFlag\":0,\"objStatus\":\"INSERT\"},{\"rowId\":\"F54A520957874FEC9C2CDF18178C69E3\"," +
                "\"linkPid\":509000294,\"objStatus\":\"DELETE\"}],\"pid\":509000294,\"objStatus\":\"UPDATE\"}," +
                "{\"forms\":[{\"linkPid\":409000268,\"formOfWay\":1,\"extendedForm\":0,\"auxiFlag\":0,\"kgFlag\":0," +
                "\"objStatus\":\"INSERT\"},{\"rowId\":\"8BE7CC2C41684D48A27DE298A149EAD1\",\"linkPid\":409000268," +
                "\"objStatus\":\"DELETE\"}],\"pid\":409000268,\"objStatus\":\"UPDATE\"}]}";
        TestUtil.run(requester);
    }

    @Test
    public void checkInter() throws Exception {
        JSONObject json = new JSONObject();
        json.put("type", "LineString");
        json.put("coordinates", "[[116.48002803325653,39.94447904496035],[116.4809238910675,39.944431748640305]," +
                "[116.48018091917038,39.94386419024943],[116.48109555244444,39.94394027624902],[116.48002803325653,"
                + "39.94447904496035]]");
        checkSelfIntersect(json);
    }

    private void checkSelfIntersect(JSONObject geometry) throws Exception {
        Geometry linkGeo = null;
        try {
            linkGeo = GeoTranslator.geojson2Jts(geometry, 100000, 0);
            List<Point> points = new ArrayList<>();
            GeoHelper.isSample(linkGeo, points);
            if (!points.isEmpty())
                throw new Exception("背景面不能自相交");
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    public void testCreateSideRoad() {
        String requester = "{\"command\":\"CREATESIDEROAD\",\"type\":\"RDLINK\",\"dbId\":13,\"subtaskId\":1,\"distance\":4," +
                "\"sideType\":1,\"sNodePid\":409000057,\"data\":{\"linkPids\":[502000077]}}";
        TestUtil.run(requester);
    }

}
