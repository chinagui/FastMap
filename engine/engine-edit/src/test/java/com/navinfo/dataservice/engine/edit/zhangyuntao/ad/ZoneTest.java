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
        String paramenter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":307000046,\"data\":{\"longitude\":116.6181457042694,\"latitude\":40.25},\"type\":\"ZONENODE\"}";
        TestUtil.run(paramenter);
    }

    @Test
    public void remove(){
        String parameter = "{\"command\":\"DELETE\",\"type\":\"ZONENODE\",\"dbId\":17,\"objId\":304000015}";
        TestUtil.run(parameter);
    }

    @Test
    public void breakL(){
        String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":302000561,\"data\":{\"longitude\":116.61131470075341,\"latitude\":40.25247994052458},\"type\":\"ZONENODE\"}";
        TestUtil.run(parameter);
    }
}
