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
		String sql = "select link_pid from rd_link where kind in (11,13) and link_pid in ("+StringUtils.join(linkPids, ",")+") and rownum=1";
		
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
		RdRestrictionDetail rdRestrictionDetail1 = new RdRestrictionDetail();
		rdRestrictionDetail1.setOutLinkPid(18667201);
		rdRestrictionDetail1.setPid(14144);
		rdRestrictionDetail1.setRestricPid(11958);
		rdRestrictionDetail1.setOutNodePid(176425);
		details.add(rdRestrictionDetail1);
		
		RdRestrictionDetail rdRestrictionDetail2 = new RdRestrictionDetail();
		rdRestrictionDetail2.setOutLinkPid(86755886);
		rdRestrictionDetail2.setPid(14677);
		rdRestrictionDetail2.setRestricPid(11958);
		rdRestrictionDetail2.setOutNodePid(176425);
		details.add(rdRestrictionDetail2);
		
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setInLinkPid(199135);
		rdRestriction.setDetails(details);
		rdRestriction.setNodePid(176425);
		rdRestriction.setPid(11958);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(rdRestriction);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setProjectId(12);
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDRESTRICTION);
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());
	}

}
