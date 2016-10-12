package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/9/30 0030.
 */
public class ZoneTest extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void move() {
        String paramenter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":207000002,\"data\":{\"longitude\":116.12569212913512,\"latitude\":40.58962069905525},\"type\":\"ZONENODE\"}";
        TestUtil.run(paramenter);
    }
}
