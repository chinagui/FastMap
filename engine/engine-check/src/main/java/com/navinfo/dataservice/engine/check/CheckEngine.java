package com.navinfo.dataservice.engine.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.check.core.CheckRule;
import com.navinfo.dataservice.engine.check.core.CheckSuitLoader;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.dao.DBConnector;
import com.navinfo.dataservice.commons.db.ConfigLoader;
import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.check.NiValExceptionOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.vividsolutions.jts.geom.Geometry;

public class CheckEngine {
	private CheckCommand checkCommand = null;
	private Connection conn;

	public CheckEngine(CheckCommand checkCommand) throws Exception{
		this.checkCommand=checkCommand;
		this.conn = DBOraclePoolManager.getConnection(this.checkCommand.getProjectId());
	}

	//获取本次要执行的检查规则
	private List<CheckRule> getRules(ObjType objType, OperType operType,String checkType) throws Exception{
		String suitCode = objType.toString()+"_"+operType.toString()+"_"+checkType;
		List<CheckRule> myCheckSuit = CheckSuitLoader.getInstance().getCheckSuit(suitCode);
		return myCheckSuit;
	}
	
	//对后检查需要保存检查结果，调用此方法将检查结果插入到Ni_val_exception中
	private void saveCheckResult(List<NiValException> checkResultList) throws Exception{
		if (checkResultList==null || checkResultList.size()==0) {return;}
		for(int i=0;i<checkResultList.size();i++){
			NiValExceptionOperator check = new NiValExceptionOperator(this.conn);
			check.insertCheckLog(checkResultList.get(i).getRuleId(), checkResultList.get(i).getLoc(), checkResultList.get(i).getTargets(), checkResultList.get(i).getMeshId(), "TEST");
		}
	}
	//前检查
	public String preCheck() throws Exception{
		//获取前检查需要执行规则列表
		List<CheckRule> rulesList=getRules(checkCommand.getObjType(),checkCommand.getOperType(),new String("PRE"));
		
		for (int i=0;i<rulesList.size();i++){
			CheckRule rule=rulesList.get(i);
			baseRule obj = (baseRule) rule.getRuleClass().newInstance();
			obj.setRuleDetail(rule);
			
			obj.setConn(this.conn);
			
			obj.preCheck(this.checkCommand);
			if(obj.getCheckResultList().size()!=0){
				return obj.getCheckResultList().get(0).getInformation();
				}
		}		
		return null;
	}
	
	//后检查
	public void postCheck() throws Exception{
		//获取后检查需要执行规则列表
		List<CheckRule> rulesList=getRules(this.checkCommand.getObjType(),this.checkCommand.getOperType(),new String("POST"));
		List<NiValException> checkResultList = new ArrayList<NiValException>();
		
		for (int i=0;i<rulesList.size();i++){
			CheckRule rule=rulesList.get(i);
			baseRule obj = (baseRule) rule.getRuleClass().newInstance();
			obj.setRuleDetail(rule);
			
			obj.setConn(this.conn);
			
			obj.postCheck(this.checkCommand);
			//调用规则的后检查
			checkResultList.addAll(obj.getCheckResultList());
		}
		saveCheckResult(checkResultList);
	}
	
	public static void main(String[] args) throws Exception{
		RdLink link=new RdLink();
		String str= "{ \"type\": \"LineString\",\"coordinates\": [ [116.17659, 39.97508], [116.16144, 39.94844],[116.20427, 39.94322],[116.20427, 39.94322], [116.17659, 39.97508] ]}";
		JSONObject geometry = JSONObject.fromObject(str);
		Geometry geometry2=GeoTranslator.geojson2Jts(geometry, 1, 5);
		link.setGeometry(geometry2);
		link.setPid(1);
		link.setsNodePid(2);
		link.seteNodePid(2);
		List<IRow> objList=new ArrayList<IRow>();
		objList.add(link);
		
		ConfigLoader.initDBConn("D:/workfiles/0_svn/fastmap-hithub/web/edit-web/target/classes/config.properties");
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(11);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.UPDATE);
		checkCommand.setObjType(link.objType());
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());		
	}
	
}
