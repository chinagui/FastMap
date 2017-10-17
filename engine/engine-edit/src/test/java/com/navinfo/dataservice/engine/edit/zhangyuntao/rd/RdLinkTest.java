package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;

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
        String parameter = "{\"command\":\"DELETE\",\"type\":\"RDNODE\",\"objId\":709000131,\"infect\":0,\"dbId\":65,\"subtaskId\":986}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"DELETE\",\"type\":\"RDINTER\",\"objId\":703000004,\"infect\":0,\"dbId\":65,\"subtaskId\":672}";
        TestUtil.run(parameter);
    }

    @Test
    public void repair() {
        String parameter = "{\"command\":\"DELETE\",\"type\":\"RDINTER\",\"objId\":505000013,\"infect\":0,\"dbId\":65,\"subtaskId\":1018}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDINTER\",\"data\":{\"links\":[],\"nodes\":[807000128,808000143]}," +
                "\"dbId\":65,\"subtaskId\":1018}";
        TestUtil.run(parameter);
    }

    @Test
    public void depart() throws Exception {
        String requester = "{\"command\":\"TOPOBREAK\",\"type\":\"RDLINK\",\"dbId\":249,\"subtaskId\":505,\"objId\":[14680372," +
                "408001028],\"data\":{\"longitude\":116.20895085842606,\"latitude\":39.80757077990377,\"nodePid\":0}}";
        TestUtil.run(requester);
    }
/*
    @Test
    public void search() {
        String parameter = "{\"dbId\":13,\"gap\":10,\"types\":[\"RDNODE\"],\"x\":07588,\"y\":24870,\"z\":16}";
        try {
            TestSearch.testSearchGap(parameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Test
    public void updownDepart() {
        String parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":17,\"distance\":\"6.6\"," +
                "\"data\":{\"linkPids\":[209000217]}}";
        parameter = "{\"command\":\"UPDOWNDEPART\",\"type\":\"RDLINK\",\"dbId\":13,\"distance\":9.9," +
                "\"data\":{\"linkPids\":[402000709,502000789]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String parameter = "{\"command\":\"MOVE\",\"type\":\"IXPOI\",\"dbId\":13,\"subtaskId\":363," +
                "\"data\":{\"longitude\":116.7500004172325,\"latitude\":39.94688288877364,\"x_guide\":116.7500004172325," +
                "\"y_guide\":39.946950746269664,\"linkPid\":409000447},\"objId\":501000119}";
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
    public void testCreateSideRoad() throws Exception {
        String parameter = "{\"command\":\"MOVE\",\"type\":\"RWNODE\",\"objId\":406000024,\"data\":{\"longitude\":119.49937865138052," +
                "\"latitude\":38.53433910815049},\"dbId\":13,\"subtaskId\":61}";
        TestUtil.run(parameter);
    }

   /* @Test
    public void delete() throws Exception {
        Connection conn = DBConnector.getInstance().getConnectionById(13);
        ZoneFaceSelector selector = new ZoneFaceSelector(conn);
        selector.load(GeoTranslator.transform(((RdLink)new RdLinkSelector(conn).loadById(506000466, false)).getGeometry(), 0.00001,5));
    }

    public static void main(String[] args) throws Exception {
        Connection conn = DBConnector.getInstance().getConnectionById(13);
        ZoneFaceSelector selector = new ZoneFaceSelector(conn);
        selector.load(GeoTranslator.transform(((RdLink)new RdLinkSelector(conn).loadById(506000466, false)).getGeometry(), 0.00001,5));
    }*/
}
