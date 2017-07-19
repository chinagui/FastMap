package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.search.RdLinkSearch;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.DbMeshInfoUtil;
import com.navinfo.dataservice.engine.edit.utils.GeoRelationUtils;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import com.navinfo.navicommons.geo.computation.GeometryRelationUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Method;
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
        String parameter = "{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"RDLINK\",\"objId\":401000503,\"data\":{\"kind\":9," +
                "\"routeAdopt\":0,\"speedlimits\":[{\"fromSpeedLimit\":15,\"toSpeedLimit\":15,\"speedClass\":7," +
                "\"rowId\":\"5F330AFA59CC4088BD524D779563404E\",\"linkPid\":401000503,\"objStatus\":\"UPDATE\"}]," +
                "\"rowId\":\"1FC94788D96A4DE18083C83D366F94BE\",\"pid\":401000503,\"objStatus\":\"UPDATE\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void update() {
        String parameter = "{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"RDLINK\",\"objId\":400000505,\"data\":{\"kind\":2," +
                "\"routeAdopt\":5,\"rowId\":\"5DAF09FEDCDE4A569418F7E93FF20D22\",\"pid\":400000505,\"objStatus\":\"UPDATE\"}}";
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
        String parameter = "{\"command\":\"CREATE\",\"type\":\"RDLINK\",\"dbId\":13,\"subtaskId\":393,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.85005083680153,39.96127128417874]," +
                "[116.85031503438948,39.96148303704254]]},\"catchLinks\":[]}}";
        TestUtil.run(parameter);
    }

    @Test
    public void depart() throws Exception {
        String requester = "{\"command\":\"TOPOBREAK\",\"type\":\"RDLINK\",\"dbId\":249,\"subtaskId\":505,\"objId\":[14680372," +
                "408001028],\"data\":{\"longitude\":116.20895085842606,\"latitude\":39.80757077990377,\"nodePid\":0}}";
        TestUtil.run(requester);
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
        ZoneFace face1 = (ZoneFace) new AbstractSelector(ZoneFace.class, DBConnector.getInstance().getConnectionById(13)).loadById(401000016, false);
        Geometry geometry1 = GeoTranslator.transform(face1.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);

        ZoneFace face2 = (ZoneFace) new AbstractSelector(ZoneFace.class, DBConnector.getInstance().getConnectionById(13)).loadById(510000026, false);
        Geometry geometry2 = GeoTranslator.transform(face2.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);

        RdLink link = (RdLink) new AbstractSelector(RdLink.class, DBConnector.getInstance().getConnectionById(13)).loadById(505000501, false);
        Geometry geometry = GeoTranslator.transform(link.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION);

        System.out.println(GeoRelationUtils.IsLinkOnLeftOfRing(geometry, geometry1));
        System.out.println(GeoRelationUtils.IsLinkOnLeftOfRing(geometry, geometry2));

        System.out.println(GeometryRelationUtils.IsLinkOnLeftOfRing(geometry1, geometry));
        System.out.println(GeometryRelationUtils.IsLinkOnLeftOfRing(geometry2, geometry));
    }

    @Test
    public void delete() throws Exception {
        MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        metadataApi.scPointSpecKindCodeType14();
    }

    public static void main(String[] args) throws Exception {
        String parameter = "{\"command\":\"UPDATE\",\"type\":\"RDSPEEDLIMIT\",\"dbId\":13,\"subtaskId\":1,\"data\":{\"pid\":500000001," +
                "\"direct\":3,\"linkPid\":49913063,\"longitude\":116.53538352127916,\"latitude\":39.7420088702255," +
                "\"objStatus\":\"UPDATE\"}}";
        com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process process = new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(JSONObject.fromObject(parameter), parameter));
        Method[] methods = process.getClass().getMethods();
        methods.getClass().getDeclaredMethods();
    }
}
