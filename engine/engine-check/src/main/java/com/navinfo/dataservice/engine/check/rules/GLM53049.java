package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkRtic;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.ChainLoader;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM53049
 * @author songdongyan
 * @date 2017年1月11日
 * @Description: 检查对象：RD_LINK_RTIC表
 * 检查原则：环岛/特殊交通类型（按整个环岛/特殊交通类型来考虑），所有该环岛/特殊交通类型的挂接LINK的Mesh_ID、CODE、RANK都相同，则报log。
 * 车厂RTIC图面编辑
 * 车厂RTIC代码编辑
 * 道路形态编辑
 * 道路属性编辑
 */
public class GLM53049 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//道路形态编辑
			if(obj instanceof RdLink ){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink);
			}
			//道路形态编辑
			else if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
			//车厂RTIC代码编辑/车厂RTIC图面编辑
			else if(obj instanceof RdLinkRtic ){
				RdLinkRtic rdLinkRtic=(RdLinkRtic) obj;
				checkRdLinkRtic(rdLinkRtic);
			}
		}
		
	}

	/**
	 * @param rdLinkRtic
	 * @throws Exception 
	 */
	private void checkRdLinkRtic(RdLinkRtic rdLinkRtic) throws Exception {
		boolean checkFlag = false;
		if(rdLinkRtic.status().equals(ObjStatus.INSERT)){
			checkFlag = true;
		}
		else if(rdLinkRtic.status().equals(ObjStatus.UPDATE)){
			if((rdLinkRtic.changedFields().containsKey("code"))||(rdLinkRtic.changedFields().containsKey("rank"))){
				checkFlag = true;
			}
		}
		if(checkFlag){
			//查其是否挂接在环岛或者特殊交通上
			StringBuilder sb = new StringBuilder();
			
			sb.append("SELECT RL.LINK_PID,RL.S_NODE_PID,RL.E_NODE_PID FROM RD_LINK RL,");
			sb.append(" (SELECT L.S_NODE_PID,L.E_NODE_PID FROM RD_LINK L WHERE L.U_RECORD <> 2 AND L.LINK_PID = " + rdLinkRtic.getLinkPid() +")T");
			sb.append(" WHERE RL.U_RECORD <> 2");
			sb.append(" AND (RL.SPECIAL_TRAFFIC = 1 OR EXISTS(SELECT 1 FROM RD_LINK_FORM RLF WHERE RLF.U_RECORD <> 2 AND RLF.LINK_PID = RL.LINK_PID))");
			sb.append(" AND (RL.S_NODE_PID = T.S_NODE_PID OR RL.S_NODE_PID = T.E_NODE_PID OR RL.E_NODE_PID = T.S_NODE_PID OR RL.E_NODE_PID = T.E_NODE_PID)");
			sb.append(" AND RL.LINK_PID <> " + rdLinkRtic.getLinkPid());
			
			String sql = sb.toString();
			log.info("RdLinkRtic后检查GLM53049,判断link是否挂接在环岛或者特殊交通上:" + sql);

			PreparedStatement pstmt = this.getConn().prepareStatement(sql);		
			ResultSet resultSet = pstmt.executeQuery();
			HashSetRdLinkAndPid hashSetRdLinkAndPid = new HashSetRdLinkAndPid();

			while (resultSet.next()){
				RdLink rdLink = new RdLink();
				rdLink.setPid(resultSet.getInt("LINK_PID"));
				rdLink.seteNodePid(resultSet.getInt("E_NODE_PID"));
				rdLink.setsNodePid(resultSet.getInt("S_NODE_PID"));
				hashSetRdLinkAndPid.add(rdLink);
			} 
			resultSet.close();
			pstmt.close();
			//如果link没有挂接在环岛或者特殊交通上
			if(hashSetRdLinkAndPid.size()==0){
				return;
			}

			//link挂接在环岛或者特殊交通上，加载环岛/特殊交通link
			ChainLoader chainLoader = new ChainLoader();
			for(RdLink rdLink:hashSetRdLinkAndPid.getRdLinkSet()){
				HashSetRdLinkAndPid huanDaoAndChain = chainLoader.loadHandaoChain(this.getConn(), rdLink);
				if(huanDaoAndChain.size()==0){
					continue;
				}
				check(huanDaoAndChain.getRdLinkPidSet(),rdLinkRtic.getLinkPid());
				
				HashSetRdLinkAndPid huanSpecTrafficChain = chainLoader.loadSpecTrafficChain(this.getConn(), rdLink);
				if(huanSpecTrafficChain.size()==0){
					continue;
				}
				check(huanSpecTrafficChain.getRdLinkPidSet(),rdLinkRtic.getLinkPid());
			}
		}
		
	}

	/**
	 * @param rdLinkPidSet
	 * @param linkPid 
	 * @throws Exception 
	 */
	private void check(Set<Integer> rdLinkPidSet, int linkPid) throws Exception {
		//检查”所有该环岛/特殊交通类型的挂接LINK的Mesh_ID、CODE、RANK都相同，则报log“
		StringBuilder sb2 = new StringBuilder();
		
		sb2.append("WITH TEMP AS(");
		sb2.append(" SELECT L.LINK_PID,L.MESH_ID,R.CODE,R.RANK FROM RD_LINK L,RD_LINK_RTIC R,");
		sb2.append(" (SELECT RL.S_NODE_PID,RL.E_NODE_PID FROM RD_LINK RL WHERE RL.U_RECORD <> 2 AND RL.LINK_PID IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + "))T");
		sb2.append(" WHERE (L.S_NODE_PID = T.S_NODE_PID OR L.S_NODE_PID = T.E_NODE_PID OR L.E_NODE_PID = T.S_NODE_PID OR L.E_NODE_PID = T.E_NODE_PID)");
		sb2.append(" AND L.U_RECORD <> 2");
		sb2.append(" AND L.LINK_PID NOT IN (" + StringUtils.join(rdLinkPidSet.toArray(),",") + ")");
		sb2.append(" AND L.SPECIAL_TRAFFIC = 0");
		sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.U_RECORD <> 2 AND F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 33)");
		sb2.append(" AND L.LINK_PID = R.LINK_PID");
		sb2.append(" AND R.U_RECORD <> 2)");
		sb2.append(" SELECT 1 FROM TEMP T1,TEMP T2");
		sb2.append(" WHERE T1.LINK_PID <> T2.LINK_PID");
		sb2.append(" AND T1.MESH_ID = T2.MESH_ID");
		sb2.append(" AND T1.CODE = T2.CODE");
		sb2.append(" AND T1.RANK = T2.RANK ");
		
		String sql = sb2.toString();
		log.info("RdLinkRtic后检查GLM53049:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_LINK," + linkPid + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlag = false;
		if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			if(rdLinkForm.getFormOfWay() == 33){
				checkFlag = true;
			}
		}
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if((formOfWay == 33)){
					checkFlag = true;
				}
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("WITH TEMP AS(");
			sb2.append(" SELECT L.LINK_PID,L.MESH_ID,R.CODE,R.RANK FROM RD_LINK L,RD_LINK_RTIC R,");
			sb2.append(" (SELECT RL.S_NODE_PID,RL.E_NODE_PID FROM RD_LINK RL WHERE RL.U_RECORD <> 2 AND RL.LINK_PID = " + rdLinkForm.getLinkPid() + ")T");
			sb2.append(" WHERE (L.S_NODE_PID = T.S_NODE_PID OR L.S_NODE_PID = T.E_NODE_PID OR L.E_NODE_PID = T.S_NODE_PID OR L.E_NODE_PID = T.E_NODE_PID)");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND L.LINK_PID <> " + rdLinkForm.getLinkPid());
			sb2.append(" AND L.SPECIAL_TRAFFIC = 0");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.U_RECORD <> 2 AND F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 33)");
			sb2.append(" AND L.LINK_PID = R.LINK_PID");
			sb2.append(" AND R.U_RECORD <> 2)");
			sb2.append(" SELECT 1 FROM TEMP T1,TEMP T2");
			sb2.append(" WHERE T1.LINK_PID <> T2.LINK_PID");
			sb2.append(" AND T1.MESH_ID = T2.MESH_ID");
			sb2.append(" AND T1.CODE = T2.CODE");
			sb2.append(" AND T1.RANK = T2.RANK ");

			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM53049:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}


	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		boolean checkFlag = false;
		if(rdLink.changedFields().containsKey("specialTraffic")){
			int specialTraffic = Integer.parseInt(rdLink.changedFields().get("specialTraffic").toString());
			if((specialTraffic == 1)){
				checkFlag = true;
			}
		}
		if(checkFlag){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("WITH TEMP AS(");
			sb2.append(" SELECT L.LINK_PID,L.MESH_ID,R.CODE,R.RANK FROM RD_LINK L,RD_LINK_RTIC R,");
			sb2.append(" (SELECT RL.S_NODE_PID,RL.E_NODE_PID FROM RD_LINK RL WHERE RL.U_RECORD <> 2 AND RL.LINK_PID = " + rdLink.getPid() + ")T");
			sb2.append(" WHERE (L.S_NODE_PID = T.S_NODE_PID OR L.S_NODE_PID = T.E_NODE_PID OR L.E_NODE_PID = T.S_NODE_PID OR L.E_NODE_PID = T.E_NODE_PID)");
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND L.LINK_PID <> " + rdLink.getPid());
			sb2.append(" AND L.SPECIAL_TRAFFIC = 0");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.U_RECORD <> 2 AND F.LINK_PID = L.LINK_PID AND F.FORM_OF_WAY = 33)");
			sb2.append(" AND L.LINK_PID = R.LINK_PID");
			sb2.append(" AND R.U_RECORD <> 2)");
			sb2.append(" SELECT 1 FROM TEMP T1,TEMP T2");
			sb2.append(" WHERE T1.LINK_PID <> T2.LINK_PID");
			sb2.append(" AND T1.MESH_ID = T2.MESH_ID");
			sb2.append(" AND T1.CODE = T2.CODE");
			sb2.append(" AND T1.RANK = T2.RANK ");

			String sql2 = sb2.toString();
			log.info("RdLink后检查GLM53049:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLink.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}

}
