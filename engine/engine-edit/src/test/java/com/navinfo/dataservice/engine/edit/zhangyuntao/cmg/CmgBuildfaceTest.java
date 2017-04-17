package com.navinfo.dataservice.engine.edit.zhangyuntao.cmg;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.search.CmgBuildfaceSearch;
import com.navinfo.dataservice.dao.glm.search.LcFaceSearch;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import com.navinfo.navicommons.database.sql.DBUtils;
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
        String requester = "{\"command\":\"CREATE\",\"type\":\"CMGBUILDFACE\",\"dbId\":13," +
                "\"data\":{\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[116.5281629562378,39.74002111583855]," +
                "[116.52935385704039,39.740252114686754],[116.52893543243408,39.7395508657908],[116.52809858322144,39.739616865991074]," +
                "[116.5281629562378,39.74002111583855]]}}}";
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
}
