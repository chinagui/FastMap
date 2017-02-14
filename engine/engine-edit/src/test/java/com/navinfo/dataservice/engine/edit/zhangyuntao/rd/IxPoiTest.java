package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by chaixin on 2016/9/26 0026.
 */
public class IxPoiTest extends InitApplication {


    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void creat() {
        String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":19,\"data\":{\"longitude\":116.47318840026855,\"latitude\":40.01432055968962,\"x_guide\":116.47318840026855,\"y_guide\":40.01422195512273,\"linkPid\":204000506,\"name\":\"测试\",\"kindCode\":\"230227\"}}";
        parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":19," +
                "\"data\":{\"longitude\":116.38623118400574,\"latitude\":40.03860991826774," +
                "\"x_guide\":116.38628165715588,\"y_guide\":40.03846802657173,\"linkPid\":305003426," +
                "\"name\":\"cccccc\",\"kindCode\":\"210201\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void test() throws Exception {
        MetadataApi apiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        System.out.println(Arrays.toString(apiService.pyConvert("锦业一路")));
    }

}
