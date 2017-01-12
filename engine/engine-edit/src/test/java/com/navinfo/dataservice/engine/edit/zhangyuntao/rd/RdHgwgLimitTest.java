package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/11 0011.
 */
public class RdHgwgLimitTest extends InitApplication{
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void create(){
        String parameter = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":301000064,\"data\":{\"longitude\":116.69297933578491,\"latitude\":40.249570038001465},\"type\":\"ADNODE\"}";
        TestUtil.run(parameter);
    }

    @Test
    public void move(){
        String parameter = "{\"command\":\"MOVE\",\"type\":\"RDHGWGLIMIT\",\"dbId\":17,\"data\":{\"pid\":202000002,\"linkPid\":\"207002431\",\"latitude\":40.01279649539292,\"longitude\":116.47630336774168}}";
        TestUtil.run(parameter);
    }
}
