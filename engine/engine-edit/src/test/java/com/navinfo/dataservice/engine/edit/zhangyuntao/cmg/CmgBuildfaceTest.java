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
    public void testRender() throws Exception {
        CmgBuildfaceSearch search = new CmgBuildfaceSearch(DBConnector.getInstance().getConnectionById(13));
        search.searchDataByTileWithGap(107963, 49743, 17, 10);
    }
}
