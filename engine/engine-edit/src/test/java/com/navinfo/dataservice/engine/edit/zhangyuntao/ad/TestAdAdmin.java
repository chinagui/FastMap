package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/22 0022.
 */
public class TestAdAdmin extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create() {
        String requester = "{\"command\":\"CREATE\",\"type\":\"ADLINK\",\"dbId\":13,\"subtaskId\":394,\"data\":{\"sNodePid\":0," +
                "\"eNodePid\":0,\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[117.125,39.89719],[117.125,39.89702]]}," +
                "\"catchLinks\":[]}}";
        TestUtil.run(requester);
    }
    @Test
    public void delete() {
        String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"ADNODE\",\"objId\":203846}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String requester = "{\"command\":\"CREATE\",\"type\":\"ADNODE\",\"dbId\":18,\"subtaskId\":24,\"objId\":409000004," +
                "\"data\":{\"longitude\":119.99941,\"latitude\":40.00405053972857}}";
        TestUtil.run(requester);
    }
}
