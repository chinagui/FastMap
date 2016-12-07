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
        String requester = "{\"command\":\"MOVE\",\"dbId\":17,\"objId\":206000040,\"data\":{\"longitude\":116.60364031791686,\"latitude\":40.25},\"type\":\"ADNODE\"}";
        TestUtil.run(requester);
    }
}
