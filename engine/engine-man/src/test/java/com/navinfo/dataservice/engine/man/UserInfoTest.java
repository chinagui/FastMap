package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.dataservice.engine.man.userInfo.UserInfoOperation;

public class UserInfoTest extends InitApplication {
	
	@Test
	public void getUserInfoByUserId() throws Exception{
		Connection conn = null;
		conn = DBConnector.getInstance().getManConnection();
		Set<Long> userSet=new HashSet<Long>();
		userSet.add((long) 2);
		userSet.add((long) 1664);
		Map<Long, UserInfo> result = UserInfoOperation.getUserInfoByUserId(conn,userSet);
		System.out.println(result);
	}
	
	@Test
	public void getGroupByGroupIds() throws Exception{
		Connection conn = null;
		conn = DBConnector.getInstance().getManConnection();
		Set<Long> userSet=new HashSet<Long>();
		userSet.add((long) 2);
		userSet.add((long) 1);
		Map<Long, UserGroup> result = UserGroupService.getInstance().getGroupByGroupIds(conn,userSet);
		System.out.println(result);
	}

	@Override
	@Before
	public void init() {
		// TODO Auto-generated method stub
		initContext();
	}

}
