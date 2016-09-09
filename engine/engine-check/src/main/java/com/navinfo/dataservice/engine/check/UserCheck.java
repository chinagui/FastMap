package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONArray;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.selector.SelectorFactory;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.vividsolutions.jts.io.ParseException;


public class UserCheck {

	public UserCheck() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 按照grids,通过坐标wkt进行检查
	 * @param grids
	 * @throws Exception 
	 */
	public void checkWithGrids(Connection conn,JSONArray ruleCodeArray,List<Integer> grids) throws Exception{
		JSONArray gridArray=new JSONArray();
		gridArray.addAll(grids);
		String wkt = GridUtils.grids2Wkt(gridArray);
		CheckCommand checkCommand = new CheckCommand();
		checkCommand.setWkt(wkt);
		CheckEngine checkEngine = new CheckEngine(checkCommand, conn);
		checkEngine.checkByRules(ruleCodeArray, "POST");
	}
	
	/**
	 * SELECT TB_NM,OLD,NEW,FD_LST,OP_TP,TB_ROW_ID FROM LOG_DETAIL WHERE OP_TP!=2
	 * 自定义检查，查询grids范围内变化的数据
	 * @param conn
	 * @param ruleCodeArray 规则列表["rule1","rule2"]
	 * @param gridsList grid列表[12,34]
	 * @throws Exception
	 */
	public void checkWithLogs(Connection conn,JSONArray ruleCodeArray,JSONArray gridsList) throws Exception{
		String logSql="SELECT TB_NM, OLD, NEW, FD_LST, OP_TP, TB_ROW_ID"
				+ "  FROM LOG_DETAIL D"
				+ " WHERE OP_TP != 2"
				+ "   AND EXISTS (SELECT 1"
				+ "          FROM LOG_DETAIL_GRID G"
				+ "         WHERE G.LOG_ROW_ID = D.ROW_ID"
				+ "           AND G.GRID_ID IN ("+gridsList.toString().replace("[", "").replace("]", "")+"))";
		CheckCommand checkCommand = new CheckCommand();
		checkCommand.setLogSql(logSql);
		CheckEngine checkEngine = new CheckEngine(checkCommand, conn);
		checkEngine.checkByRules(ruleCodeArray, "POST");
	}

}
