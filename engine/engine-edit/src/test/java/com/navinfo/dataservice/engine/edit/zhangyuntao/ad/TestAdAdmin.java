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
    public void delete() {
        String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"ADNODE\",\"objId\":203846}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String requester = "{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"ADADMIN\",\"objId\":401000005,\"data\":{\"adminType\":9," +
                "\"rowId\":\"AADCB0B50BC140B0A94598547FE4AC2F\",\"pid\":401000005,\"linkPid\":408000220,\"objStatus\":\"UPDATE\"}," +
                "\"subtaskId\":65}";
        TestUtil.run(requester);
    }
}
