package com.navinfo.dataservice.engine.edit.zhangyuntao.cmg;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.search.CmgBuildfaceSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * @Title: CmgBuildfaceTest
 * @Package: com.navinfo.dataservice.engine.edit.zhangyuntao.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/17
 * @Version: V1.0
 */
public class CmgBuildfaceTest extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void testCreateCmgBuildface(){
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDFACE\",\"dbId\":13,\"subtaskId\":1," +
                "\"data\":{\"linkType\":\"CMGBUILDLINK\",\"linkPids\":[403000036,507000047]}}";
        TestUtil.run(requester);
    }

    @Test
    public void testRender() throws Exception {
        CmgBuildfaceSearch search = new CmgBuildfaceSearch(DBConnector.getInstance().getConnectionById(13));
        search.searchDataByTileWithGap(107963, 49743, 17, 10);
    }

    @Test
    public void testGetByPid() throws Exception {
        CmgBuildfaceSearch search = new CmgBuildfaceSearch(DBConnector.getInstance().getConnectionById(13));
        System.out.println(search.searchDataByPid(508000002));
    }

    @Test
    public void testDeleteCmgBuildface() {
        String requester = "{\"command\":\"DELETE\",\"dbId\":13,\"type\":\"CMGBUILDFACE\",\"objId\":509000006}";
        TestUtil.run(requester);
    }
}
