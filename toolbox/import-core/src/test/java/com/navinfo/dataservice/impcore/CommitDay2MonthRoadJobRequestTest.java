package com.navinfo.dataservice.impcore;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.impcore.commit.CommitDay2MonthRoadJobRequest;

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
		String param = "{\"gridSet\":[123,3435,343]}";
		JSONObject json=JSONObject.fromObject(param);
		CommitDay2MonthRoadJobRequest request = new CommitDay2MonthRoadJobRequest(json);
		Assert.assertTrue(request.getGridList().size()==3);
	}

}

