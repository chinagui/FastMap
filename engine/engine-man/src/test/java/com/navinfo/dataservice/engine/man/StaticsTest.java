package com.navinfo.dataservice.engine.man;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
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
import com.navinfo.dataservice.commons.util.TimestampUtils;
import com.navinfo.dataservice.engine.man.grid.GridService;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.service.ManApiImpl;
import com.navinfo.dataservice.engine.man.statics.StaticsService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.DataBaseUtils;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import com.vividsolutions.jts.io.ParseException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class StaticsTest extends InitApplication{

	@Override
	@Before
	public void init() {
		initContext();
	}
	
	@Test
	public void taskOverviewDetail()throws Exception{
		Map<String,Object> data = StaticsService.getInstance().taskOverviewDetail(135);
		System.out.println(data);
	}
	
	@Test
	public void subtaskOverviewDetail()throws Exception{
		Map<String,Object> data = StaticsService.getInstance().subtaskOverviewDetail(279);
		System.out.println(data);
	}
	
}
