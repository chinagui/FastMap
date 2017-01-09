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
        String paramter = "{\"command\":\"CREATE\",\"type\":\"ADADMIN\",\"dbId\":17," +
                "\"data\":{\"longitude\":116.42069220542908,\"latitude\":40.04956256234405,\"linkPid\":54013167}}";
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
        String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":209000065," +
                "\"data\":{\"longitude\":119.00177121162415,\"latitude\":37.59854112439639},\"type\":\"ADNODE\"}";
        TestUtil.run(parameter);
    }
}
