package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/22 0022.
 */
public class TestRestriction extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void update() {
        String requester = "{\"command\":\"UPDATE\",\"type\":\"RDRESTRICTION\",\"dbId\":19,\"data\":{\"restricInfo\":\"1,1\",\"pid\":304000033,\"objStatus\":\"UPDATE\",\"details\":[{\"pid\":0,\"restricPid\":0,\"outLinkPid\":277214,\"flag\":1,\"restricInfo\":1,\"type\":1,\"relationshipType\":1,\"conditions\":[{\"timeDomain\":\"\",\"vehicle\":0,\"resTrailer\":0,\"resWeigh\":0,\"resAxleLoad\":0,\"resAxleCount\":0,\"resOut\":0,\"rowId\":\"\",\"objStatus\":\"INSERT\"}],\"vias\":[],\"objStatus\":\"INSERT\"},{\"conditions\":[{\"timeDomain\":\"\",\"rowId\":\"BF048AA310014FE6B7973CC322EA9E7B\",\"objStatus\":\"UPDATE\"}],\"pid\":308000051}]}}";
        TestUtil.run(requester);
    }
}
