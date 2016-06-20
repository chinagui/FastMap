package com.navinfo.dataservice.impcore;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.impcore.commit.day.road.Day2MonthRoadJobRequest;

/*
 * @author mayunfei
 * 2016年6月6日
 * 描述：job-frameworkCommitDay2MonthRoadJobRequestTest.java
 */
public class CommitDay2MonthRoadJobRequestTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		String param = "{\"gridSet\":[23432,4343243,3434]}";
		JSONObject json=JSONObject.fromObject(param);
		Day2MonthRoadJobRequest request = new Day2MonthRoadJobRequest();
		request.parseByJsonConfig(json);
		Assert.assertTrue(request.getGridList().size()==3);
	}

}

