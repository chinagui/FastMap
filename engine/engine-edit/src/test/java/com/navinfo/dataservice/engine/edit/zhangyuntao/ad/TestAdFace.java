package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import org.junit.Test;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

/**
 * @author zhangyt
 * @Title: AdFace.java
 * @Description: TODO
 * @date: 2016年8月18日 下午4:02:40
 * @version: v1.0
 */
public class TestAdFace extends InitApplication {

    @Test
    public void create() {
        String paramter = "{\"command\":\"CREATE\",\"type\":\"ZONELINK\",\"dbId\":13,\"subtaskId\":65,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.82273790240286,39.91667242392563]," +
                "[116.82295650243759,39.91667036672904]]},\"catchLinks\":[]}}";
        TestUtil.run(paramter);
    }

    @Override
    public void init() {
        super.initContext();
    }

    public static void main(String[] args) {
        String wkt = MercatorProjection.getWktWithGap(107940, 49581, 17, 80);
        System.out.println(wkt);
        wkt = MercatorProjection.getWktWithGap(107940, 49582, 17, 80);
        System.out.println(wkt);
        wkt = MercatorProjection.getWktWithGap(107939, 49581, 17, 80);
        System.out.println(wkt);
        wkt = MercatorProjection.getWktWithGap(107939, 49582, 17, 80);
        System.out.println(wkt);

    }

    @Test
    public void relation() throws Exception {
        RdLinkSelector selector = new RdLinkSelector(DBConnector.getInstance().getConnectionById(17));
        RdLink link = (RdLink) selector.loadById(203003140, false);
        System.out.println(GeoTranslator.jts2Wkt(link.getGeometry(), 0.00001, 5));
    }

    @Test
    public void move() {
        String parameter = "{\"command\":\"REPAIR\",\"type\":\"ZONELINK\",\"objId\":504000030,\"data\":{\"type\":\"ZONELINK\"," +
                "\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.80701,39.95251],[116.80712,39.9526],[116.80736,39.95262]," +
                "[116.80751,39.95252],[116.8075,39.95226],[116.80696,39.95235],[116.80701,39.95251]]},\"catchInfos\":[]},\"dbId\":13," +
                "\"subtaskId\":61}";
        TestUtil.run(parameter);
    }
}
