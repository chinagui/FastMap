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
        String paramenter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":208000047," +
                "\"data\":{\"longitude\":116.875079870224,\"latitude\":40.419205785712236},\"type\":\"ZONENODE\"}";
        //paramenter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":208000047,
        // \"data\":{\"longitude\":116.87521398067473,\"latitude\":40.41819701924083},\"type\":\"ZONENODE\"}";
        TestUtil.run(paramenter);
    }

    @Test
    public void remove() {
        String parameter = "{\"command\":\"DELETE\",\"type\":\"ZONEFACE\",\"dbId\":17,\"objId\":309000042}";
        TestUtil.run(parameter);
    }

    @Test
    public void breakL() {
        String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"objId\":302000561," +
                "\"data\":{\"longitude\":116.61131470075341,\"latitude\":40.25247994052458},\"type\":\"ZONENODE\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"ZONEFACE\",\"dbId\":84," +
                "\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.43982172012329," +
                "39.964967613764365],[116.43920481204988,39.96446601012264],[116.44006311893463,39.96448245620204]," +
                "[116.43982172012329,39.964967613764365]]}}}";
        TestUtil.run(parameter);
    }
}
