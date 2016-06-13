package com.navinfo.dataservice.jobframework.test;

import static org.junit.Assert.*;
import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.jobframework.commit.CommitDay2MonthRoadJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoadJobRequestTest.java
 */
public class CommitDay2MonthRoadJobRequestTest {

	@Before
	public void setUp() throws Exception {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] { "applicationContext.xml" }); 
	}

	@Test
	public void test() {
		String param = "{\"gridSet\":[123,3435,343]}";
		JSONObject json=JSONObject.fromObject(param);
		CommitDay2MonthRoadJobRequest request = new CommitDay2MonthRoadJobRequest(json);
		Assert.assertTrue(request.getGridList().size()==3);
	}

}

