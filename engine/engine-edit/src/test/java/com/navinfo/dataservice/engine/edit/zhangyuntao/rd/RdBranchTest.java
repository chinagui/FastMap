package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/9/23 0023.
 */
public class RdBranchTest extends InitApplication{
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void delete(){
        String parameter = "{\"command\":\"DELETE\",\"dbId\":2025,\"type\":\"RDBRANCH\",\"detailId\":304000013,\"rowId\":\"\",\"branchType\":0}";
        TestUtil.run(parameter);
    }

    @Test
    public void update(){
        String parameter = "{\"type\":\"RDBRANCH\",\"command\":\"UPDATE\",\"dbId\":19,\"data\":{\"relationshipType\":2,\"pid\":23415,\"objStatus\":\"UPDATE\"}}";
        TestUtil.run(parameter);
    }
}
