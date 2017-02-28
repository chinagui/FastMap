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
        parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":84," +
                "\"data\":{\"longitude\":116.47620588541031,\"latitude\":40.0136488132571," +
                "\"x_guide\":116.47608141958843,\"y_guide\":40.013645261848154,\"linkPid\":29833502,\"name\":\"酒店\"," +
                "\"kindCode\":\"120101\"}}";
        TestUtil.run(parameter);
    }

    @Test
    public void test() throws Exception {
        MetadataApi apiService = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        System.out.println(Arrays.toString(apiService.pyConvert("锦业一路")));
    }

}
