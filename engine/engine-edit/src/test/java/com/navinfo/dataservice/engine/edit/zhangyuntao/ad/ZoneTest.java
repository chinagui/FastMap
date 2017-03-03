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
        String paramenter = "{\"command\":\"MOVE\",\"objId\":402000045,\"type\":\"RDNODE\"," +
                "\"data\":{\"longitude\":117.88847208023073,\"latitude\":39.3721027171239},\"dbId\":84}";
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
                "\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.49827644228934," +
                "39.945107260370996],[116.4981034398079,39.94490779489178],[116.4986452460289,39.94483993536939]," +
                "[116.49847492575644,39.9451093167131],[116.49827644228934,39.945107260370996]]}}}";
        TestUtil.run(parameter);
    }
}
