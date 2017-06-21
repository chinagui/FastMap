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
        String parameter = "{\"command\":\"REPAIR\",\"type\":\"RDLINK\",\"objId\":404000297,\"dbId\":13,\"subtaskId\":1," +
                "\"data\":{\"type\":\"RDLINK\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.54062,39.74981]," +
                "[116.54075324535368,39.74999770430387],[116.541,39.74987]]},\"catchInfos\":[]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.54057890176773,39.74988840790541]," +
                "[116.54075726866722,39.74999873540115],[116.54089137911797,39.74990902988064]]},\"catchLinks\":[]}}";
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
