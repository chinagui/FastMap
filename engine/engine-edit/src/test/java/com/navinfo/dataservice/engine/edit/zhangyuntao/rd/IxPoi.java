package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/9/26 0026.
 */
public class IxPoi extends InitApplication{


    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void creat(){
        String parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"IXSAMEPOI\",\"poiPids\":[307000011,308000014]}";
        TestUtil.run(parameter);
    }
}
