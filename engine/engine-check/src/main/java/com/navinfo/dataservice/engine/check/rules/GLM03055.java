package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM03055 
* @author: zhangpengpeng 
* @date: 2016年9月12日 
* @Desc: GLM03055.java 具有障碍物属性的点所挂接的link上具有步行街属性，报err；
* （挂接的多条link具有步行街属性，只报一次）
*/
public class GLM03055 extends baseRule {
	public GLM03055(){
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof RdNode){
				RdNode rdNode = (RdNode) obj;
				List<Integer> nodeForms = getNodeForms(rdNode.getPid());
				boolean isObstacleNode = false;
				if (nodeForms.contains(15)){
					isObstacleNode = true;
				}
				if (isObstacleNode){
					int nodePid = rdNode.getPid();
					StringBuilder sb = new StringBuilder();
			        sb.append("select distinct F.FORM_OF_WAY from RD_LINK R,RD_LINK_FORM F where R.LINK_PID=F.LINK_PID "
			        		+ "AND R.U_RECORD != 2 AND F.U_RECORD != 2 and ( R.E_NODE_PID= ");
			        sb.append(nodePid);
			        sb.append(" or R.S_NODE_PID= ");
			        sb.append(nodePid);
			        sb.append(" )");
					String sql = sb.toString();
					DatabaseOperator getObj=new DatabaseOperator();
					List<Object> resultList=new ArrayList<Object>();
					resultList=getObj.exeSelect(this.getConn(), sql);
					if (resultList.size() > 0){
						for (Object result: resultList){
							int formOfWay = Integer.parseInt(result.toString());
							if (formOfWay == 20){
								this.setCheckResult("", "[RD_NODE,"+nodePid+"]", 0);
								break;
							} 
						}
					}
				}
			}else if (obj instanceof RdNodeForm){
				RdNodeForm rdNodeForm = (RdNodeForm) obj;
				List<Integer> nodeForms = getNodeForms(rdNodeForm.getNodePid());
				boolean isObstacleNode = false;
				if (nodeForms.contains(15)){
					isObstacleNode = true;
				}
				if (isObstacleNode){
					int nodePid = rdNodeForm.getNodePid();
					StringBuilder sb = new StringBuilder();
			        sb.append("select distinct F.FORM_OF_WAY from RD_LINK R,RD_LINK_FORM F "
			        		+ "where R.LINK_PID=F.LINK_PID AND R.U_RECORD != 2 AND F.U_RECORD != 2 and ( R.E_NODE_PID= ");
			        sb.append(nodePid);
			        sb.append(" or R.S_NODE_PID= ");
			        sb.append(nodePid);
			        sb.append(" )");
					String sql = sb.toString();
					DatabaseOperator getObj=new DatabaseOperator();
					List<Object> resultList=new ArrayList<Object>();
					resultList=getObj.exeSelect(this.getConn(), sql);
					if (resultList.size() > 0){
						for (Object result: resultList){
							int formOfWay = Integer.parseInt(result.toString());
							if (formOfWay == 20){
								this.setCheckResult("", "[RD_NODE,"+nodePid+"]", 0);
								break;
							} 
						}
					}
				}
			}else if (obj instanceof RdLink){
				RdLink rdLink = (RdLink) obj;
				int rdLinkPid = rdLink.getPid();
				int sNodePid = rdLink.getsNodePid();
				int eNodePid = rdLink.geteNodePid();
				exeRdLinkCheck(rdLinkPid, sNodePid, eNodePid);
			}else if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				int rdLinkPid = rdLinkForm.getLinkPid();
				StringBuilder sb = new StringBuilder();
		        sb.append("select L.S_NODE_PID,L.E_NODE_PID from RD_LINK L where L.LINK_PID= ");
		        sb.append(rdLinkPid);
		        sb.append(" and L.U_RECORD <> 2");
				String sql = sb.toString();
				PreparedStatement pstmt = this.getConn().prepareStatement(sql);		
				ResultSet resultSet = pstmt.executeQuery();
				int sNodePid = 0;
				int eNodePid = 0;
				while(resultSet.next()){
					sNodePid = resultSet.getInt("S_NODE_PID");
					eNodePid = resultSet.getInt("E_NODE_PID");
				}
				exeRdLinkCheck(rdLinkPid, sNodePid, eNodePid);
			}
		}
	}
	
	public List<Integer> getLinkForms(int rdLinkPid) throws Exception{
		StringBuilder sb = new StringBuilder();
        sb.append("select distinct F.FORM_OF_WAY from RD_LINK_FORM F where F.LINK_PID= ");
        sb.append(rdLinkPid);
        sb.append(" and F.U_RECORD <> 2");
		String sql = sb.toString();
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		List<Integer> formOfWayList = new ArrayList<Integer>();
		for (Object form: resultList){
			formOfWayList.add(Integer.parseInt(form.toString()));
		}
		return formOfWayList;
	}
	
	public List<Integer> getNodeForms(int rdNodePid) throws Exception{
		StringBuilder sb = new StringBuilder();
        sb.append("select distinct F.FORM_OF_WAY from RD_NODE_FORM F where F.NODE_PID= ");
        sb.append(rdNodePid);
        sb.append(" and F.U_RECORD <> 2");
		String sql = sb.toString();
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		List<Integer> formOfWayList = new ArrayList<Integer>();
		for (Object form: resultList){
			formOfWayList.add(Integer.parseInt(form.toString()));
		}
		return formOfWayList;
	}
	
	public void exeRdLinkCheck(int rdLinkPid, int sNodePid, int eNodePid) throws Exception{
		StringBuilder sb1 = new StringBuilder();
        sb1.append("select distinct f.FORM_OF_WAY from RD_NODE_FORM f where F.U_RECORD != 2 AND f.NODE_PID IN (");
        sb1.append(sNodePid);
        sb1.append(",");
        sb1.append(eNodePid);
        sb1.append(" )");
		String sql1 = sb1.toString();
		DatabaseOperator getObj1=new DatabaseOperator();
		List<Object> resultList1=new ArrayList<Object>();
		resultList1=getObj1.exeSelect(this.getConn(), sql1);
		boolean isObstacleNode = false;
		if (resultList1.size() > 0){
			for (Object result: resultList1){
				int nodeForm = Integer.parseInt(result.toString());
				if (nodeForm == 15){
					isObstacleNode = true;
					break;
				}
			}
		}
		if (isObstacleNode){
			List<Integer> rdLinkForms = getLinkForms(rdLinkPid);
			if (rdLinkForms.contains(20)){
				this.setCheckResult("", "[RD_LINK,"+rdLinkPid+"]", 0);
			}
		}
	}
}
