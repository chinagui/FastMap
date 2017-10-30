package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.level.LevelSelector;
import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
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
    	Connection conn = null;
        try {
        	

            
            String parameter = "{\"dbId\":13,\"pid\":3529,\"poi_num\":\"0010060903LK101042\",\"kindCode\":\"200200\",\"chainCode\":\"\",\"name\":\"自租点\",\"rating\":0,\"level\":\"B4\"}";
            JSONObject jsonReq = JSONObject.fromObject(parameter);

            conn = DBConnector.getInstance().getMetaConnection();
            LevelSelector selector = new LevelSelector(conn);

			JSONObject res = selector.getLevel(jsonReq);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        	DbUtils.closeQuietly(conn);
		}
    }
}
