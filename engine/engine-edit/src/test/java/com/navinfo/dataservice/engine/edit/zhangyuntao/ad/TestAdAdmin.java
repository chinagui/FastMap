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
        String requester = "{\"command\":\"UPDATE\",\"dbId\":13,\"type\":\"ZONELINK\",\"objId\":410000014," +
                "\"data\":{\"kinds\":[{\"kind\":0,\"rowId\":\"F6D05295A8134D53A40D5C653BA84D32\",\"linkPid\":410000014," +
                "\"objStatus\":\"UPDATE\"}],\"rowId\":\"C5C0A5FBBCC04DC193DF9B42EB5953DA\",\"pid\":410000014,\"objStatus\":\"UPDATE\"}," +
                "\"subtaskId\":65}";
        TestUtil.run(requester);
    }
}
