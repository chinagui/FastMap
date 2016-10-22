package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;




/** 
 * @ClassName: GLM01017
 * @author songdongyan
 * @date 下午3:28:00
 * @Description: GLM01017.java
 */
public class GLM01017 extends baseRule{
	
	public void preCheck(CheckCommand checkCommand) throws Exception{
		//获取inLinkPid\outLinkPid
		List<Integer> linkPids = new ArrayList<Integer>();
				
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestriction ){
				RdRestriction rdRestriction = (RdRestriction)obj;
				linkPids.add(rdRestriction.getInLinkPid());
						
				for(IRow deObj:rdRestriction.getDetails()){
					if(deObj instanceof RdRestrictionDetail){
						RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)deObj;
						linkPids.add(rdRestrictionDetail.getOutLinkPid());
					}
				}
			}
					
		}
		String sql = "select link_pid from rd_link where kind in (11,13) AND U_RECORD != 2"
				+ "and link_pid in ("+StringUtils.join(linkPids, ",")+") and rownum=1";
		
		PreparedStatement pstmt = getConn().prepareStatement(sql);

		ResultSet resultSet = pstmt.executeQuery();

		boolean flag = false;

		if (resultSet.next()) {
			flag = true;
		}

		resultSet.close();

		pstmt.close();
		
		if (flag) {

			this.setCheckResult("", "", 0);
			return;
		}

		
	}
	
	public void postCheck(CheckCommand checkCommand) throws Exception{

	}
	
	public static void main(String[] args) throws Exception{
		List<IRow> details = new ArrayList<IRow>();
		RdRestrictionDetail rdRestrictionDetail = new RdRestrictionDetail();
		rdRestrictionDetail.setOutLinkPid(197951);
		rdRestrictionDetail.setPid(14076);
		rdRestrictionDetail.setRestricPid(11883);
		details.add(rdRestrictionDetail);
		
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setInLinkPid(197954);
		rdRestriction.setDetails(details);
		rdRestriction.setNodePid(175447);
		rdRestriction.setPid(11883);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(rdRestriction);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDRESTRICTION);
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());
	}

}
