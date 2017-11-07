package com.navinfo.dataservice.engine.man;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;

public class GroupTest extends InitApplication{
	
	@Test
	public void listByType() throws ServiceException
	{
		JSONObject dataJson = JSONObject.fromObject("{\"snapshot\":0,\"groupType\":0,\"condition\":{\"groupSubtype\":3}}");			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}

		List<HashMap<?,?>> data = new ArrayList<HashMap<?,?>>();
		
		int snapshot = 0;
		if(dataJson.containsKey("snapshot")){
			snapshot = dataJson.getInt("snapshot");
		}
		int groupType=-1;
		if(dataJson.containsKey("groupType")){
			groupType=dataJson.getInt("groupType");
		}
		JSONObject conditionJson=null;
		if(dataJson.containsKey("condition")){
			conditionJson= dataJson.getJSONObject("condition");
		}
		//snapshot=1需要返回用户信息
		if(snapshot==1){				
			data = UserGroupService.getInstance().listByTypeWithUserInfo(groupType,conditionJson);

		}else{		
			List<UserGroup> userGroupList = UserGroupService.getInstance().listByType(groupType,conditionJson);

			for(int i = 0;i<userGroupList.size();i++){
				HashMap<String, Comparable> userGroup = new HashMap<String, Comparable>();
				userGroup.put("groupId", userGroupList.get(i).getGroupId());
				userGroup.put("groupName", userGroupList.get(i).getGroupName());
				userGroup.put("groupType", userGroupList.get(i).getGroupType());
				data.add(userGroup);
			}
		}		
		System.out.print(data);
	}

	@Override
	@Before
	public void init() {
		initContext();
	}
	
}
