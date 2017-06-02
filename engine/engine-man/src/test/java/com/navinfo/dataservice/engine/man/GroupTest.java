package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.api.man.model.UserGroup;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.engine.man.userGroup.UserGroupService;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
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
