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
        String parameter = "{\"command\":\"MOVE\",\"type\":\"RDHGWGLIMIT\",\"dbId\":17,\"data\":{\"pid\":205000000,\"linkPid\":202002945,\"latitude\":40.309144299907366,\"longitude\":116.71946953683,\"direct\":2}}";
        TestUtil.run(parameter);
    }
}
