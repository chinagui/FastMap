package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.level.LevelSelector;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;

public class LevelTest {

    @Before
    public void before() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"dubbo-consumer-datahub-test.xml"});
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    @Test
    public void testQueryLevel() {
        try {
        	
            Connection conn = null;
            JSONObject jsonReq = new JSONObject();
            jsonReq.put("dbId", 19);
            jsonReq.put("pid", 308);
            jsonReq.put("poi_num", "0335100531LS100266");
            jsonReq.put("kindCode", "200200");
            jsonReq.put("chainCode", "");
            jsonReq.put("name", "自行车租赁点");
            jsonReq.put("rating", 0);
            jsonReq.put("level", "B2");

            conn = DBConnector.getInstance().getMetaConnection();
            LevelSelector selector = new LevelSelector(conn);

			JSONObject res = selector.getLevel(jsonReq);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
