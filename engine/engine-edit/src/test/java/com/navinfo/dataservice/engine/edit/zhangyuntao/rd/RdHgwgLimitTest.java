package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/11 0011.
 */
public class RdHgwgLimitTest extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create() {
        String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":301000064," +
                "\"data\":{\"longitude\":116.69297933578491,\"latitude\":40.249570038001465},\"type\":\"ADNODE\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String parameter = "{\"command\":\"MOVE\",\"type\":\"RDHGWGLIMIT\",\"dbId\":84,\"data\":{\"pid\":404000001,"
                + "\"linkPid\":402000054,\"latitude\":39.211299637998685,\"longitude\":117.62428634293836," +
                "\"direct\":2}}";
        TestUtil.run(parameter);
    }
}
