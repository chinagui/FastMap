package com.navinfo.dataservice.job.statics;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.job.statics.job.ExportQualityTipsReportJob;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * Created by zhangjunfang on 2017/11/7.
 */
public class TestExportQualityTipReportJob {
    @Before
    public void before() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-consumer-datahub-test.xml" });
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    @Test
    public void testJob() throws Exception{

        ExportQualityTipsReportJob job = new ExportQualityTipsReportJob(null);
        String result = job.stat();
        System.out.println(result);

    }
}
