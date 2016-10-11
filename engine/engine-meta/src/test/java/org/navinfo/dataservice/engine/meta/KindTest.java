package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.kind.KindSelector;
import com.navinfo.dataservice.engine.meta.kindcode.KindCodeSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;

public class KindTest {

    @Before
    public void before() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"dubbo-consumer-datahub-test.xml"});
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    @Test
    public void testQueryKind() {
        try {
            KindCodeSelector selector = new KindCodeSelector();
            JSONObject jsonObject = selector.searchkindLevel("150104");
            System.out.println(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
