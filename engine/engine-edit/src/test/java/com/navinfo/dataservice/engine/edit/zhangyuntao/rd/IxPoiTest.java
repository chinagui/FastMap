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
        parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":17,\"data\":{\"longitude\":116.68065190315247,\"latitude\":40.3034298925537,\"x_guide\":116.6807246917264,\"y_guide\":40.30373368691118,\"linkPid\":279972,\"name\":\"123123\",\"kindCode\":\"210105\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void test() throws Exception {
        MetadataApi apiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        System.out.println(Arrays.toString(apiService.pyConvert("锦业一路")));
    }

}
