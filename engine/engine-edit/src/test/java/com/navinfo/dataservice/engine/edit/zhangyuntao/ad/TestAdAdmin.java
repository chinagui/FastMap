package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/22 0022.
 */
public class TestAdAdmin extends InitApplication{
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void move(){
        String requester = "{\"command\":\"MOVE\",\"type\":\"ADADMIN\",\"dbId\":17,\"objId\":210000004,\"data\":{\"longitude\":116.5823221206665,\"latitude\":40.29422437136136,\"linkPid\":19607557}}";
        TestUtil.run(requester);
    }
}
