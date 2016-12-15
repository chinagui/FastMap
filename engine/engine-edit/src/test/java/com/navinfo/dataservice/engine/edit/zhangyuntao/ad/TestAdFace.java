package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.utils.batch.ZoneIDBatchUtils;
import org.junit.Test;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;

import java.sql.Connection;

/**
 * @author zhangyt
 * @Title: AdFace.java
 * @Description: TODO
 * @date: 2016年8月18日 下午4:02:40
 * @version: v1.0
 */
public class TestAdFace extends InitApplication {

    public TestAdFace() {
    }

    @Test
    public void create() {
        String paramter = "{\"command\":\"CREATE\",\"type\":\"ADADMIN\",\"dbId\":17,\"data\":{\"longitude\":116.42069220542908,\"latitude\":40.04956256234405,\"linkPid\":54013167}}";
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
    public void repair() throws Exception {
        Connection conn = DBConnector.getInstance().getConnectionById(17);
        ZoneFaceSelector selector = new ZoneFaceSelector(conn);
        ZoneFace face = (ZoneFace) selector.loadById(305000039, true);
        ZoneIDBatchUtils.updateZoneID(face, null, conn, new Result());
    }
}
