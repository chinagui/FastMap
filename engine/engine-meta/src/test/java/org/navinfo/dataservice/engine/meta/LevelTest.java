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
        	

            
            String parameter = "{\"dbId\":19,\"pid\":4656744,\"poi_num\":\"0010110510LW502182\",\"kindCode\":\"130403\",\"chainCode\":null,\"name\":\"报刊亭\",\"rating\":0,\"level\":\"C\"}";
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
