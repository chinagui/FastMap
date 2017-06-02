package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
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
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.token.AccessTokenFactory;
import com.navinfo.dataservice.commons.token.AccessTokenUtil;
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SubtaskTest extends InitApplication{


	@Test
	public void testUpdate() throws Exception {
		// TODO Auto-generated constructor stub
		String parameter = "{\"name\":\"20171010_526_\",\"subtaskId\":350,\"descp\":null,\"planStartDate\":\"20171011\",\"planEndDate\":\"20171011\",\"exeUserId\":4574,\"gridIds\":[51572532],\"workKind\":3,\"hasQuality\":0}";
		if (StringUtils.isEmpty(parameter)){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}		
		JSONObject dataJson = JSONObject.fromObject(parameter);			
		if(dataJson==null){
			throw new IllegalArgumentException("parameter参数不能为空。");
		}
		SubtaskService service = SubtaskService.getInstance();
		service.update(dataJson,0);			
	}
	
	@Test
	public void testListByUser() throws Exception {
		AccessToken tokenObj=AccessTokenFactory.validate("00000457J3IIA2L1D2F0330FDCAA27180F845D3AAF67B5F3");
		JSONObject dataJson = JSONObject.fromObject("{\"platForm\":1,\"snapshot\":1,\"status\":1,\"pageSize\":1000,\"type\":0,\"stage\":0}");
		int curPageNum= 1;//默认为第一页
		if(dataJson.containsKey("pageNum")){
			curPageNum = dataJson.getInt("pageNum");
			dataJson.remove("pageNum");
		}
		
		int pageSize = 20;//默认页容量为10
		if(dataJson.containsKey("pageSize")){
			pageSize = dataJson.getInt("pageSize");
			dataJson.remove("pageSize");
		}
		
		int snapshot = dataJson.getInt("snapshot");
		dataJson.remove("snapshot");
		
		//增加平台参数。0：采集端，1：编辑平台2管理平台
		int platForm = 0;//默认采集端
		if(dataJson.containsKey("platForm")){
			platForm = dataJson.getInt("platForm");
			dataJson.remove("platForm");
		}
		if(!dataJson.containsKey("exeUserId")||dataJson.getInt("exeUserId")==0){
			dataJson.put("exeUserId", (int)tokenObj.getUserId());
		}
        Page page = SubtaskService.getInstance().listByUserPage(dataJson,snapshot,platForm,pageSize,curPageNum);
        			
	}
		@Override
	@Before
	public void init() {
		initContext();
	}	
}
