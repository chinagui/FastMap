package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM26044
* @author: zhangpengpeng
* @date: 2016年9月12日
* @Desc: GLM26044.java 路口内Link表中记录的道路必须具有交叉口内link属性，
* 且一定不能具有环岛属性，特殊交通必须为“否”，否则报log
*/
public class GLM26044 extends baseRule{
	public GLM26044(){
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink) obj;
				int rdLinkPid = rdLink.getPid();
				if (isRdCrossLink(rdLink,0)){
					List<Integer> formOfWayList = getForms(rdLinkPid);
					if (!formOfWayList.contains(50)){
						String log1 = "路口内link不含交叉口内link属性";
						this.setCheckResult(rdLink.getGeometry(), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), log1);
					}
					StringBuilder sb1 = new StringBuilder();
			        sb1.append("select L.SPECIAL_TRAFFIC from RD_LINK L where L.LINK_PID= ");
			        sb1.append(rdLinkPid);
			        sb1.append(" and L.U_RECORD <> 2");
					String sql1 = sb1.toString();
					PreparedStatement pstmt1 = this.getConn().prepareStatement(sql1);		
					ResultSet resultSet1 = pstmt1.executeQuery();
					int rdLinkSpecialTraffic = 0;
					while(resultSet1.next()){
						rdLinkSpecialTraffic = resultSet1.getInt("SPECIAL_TRAFFIC");
					}
					
					if (formOfWayList.contains(33) || (rdLinkSpecialTraffic == 1) ){
						String log2 = "路口内link属性不能为环岛或特殊交通类型";
						this.setCheckResult(rdLink.getGeometry(), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId(), log2);
					}
				}
			}else if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				int linkPid = rdLinkForm.getLinkPid();
				if (isRdCrossLink(null,linkPid)){
					List<Integer> formOfWayList = getForms(linkPid);
					if (!formOfWayList.contains(50)){
						String log1 = "路口内link不含交叉口内link属性";
						this.setCheckResult("", "[RD_LINK,"+linkPid+"]",0, log1);
					}
					StringBuilder sb = new StringBuilder();
			        sb.append("select L.SPECIAL_TRAFFIC from RD_LINK L where L.LINK_PID= ");
			        sb.append(linkPid);
			        sb.append(" and L.U_RECORD <> 2");
					String sql = sb.toString();
					PreparedStatement pstmt = this.getConn().prepareStatement(sql);		
					ResultSet resultSet = pstmt.executeQuery();
					int rdLinkSpecialTraffic = 0;
					while(resultSet.next()){
						rdLinkSpecialTraffic = resultSet.getInt("SPECIAL_TRAFFIC");
					}
					if (formOfWayList.contains(33) || (rdLinkSpecialTraffic == 1) ){
						String log2 = "路口内link属性不能为环岛或特殊交通类型";
						this.setCheckResult("", "[RD_LINK,"+linkPid+"]", 0, log2);
					}
				}
				
			}else if (obj instanceof RdCross){
				RdCross rdCross = (RdCross) obj;
				List<IRow> rdCrossLinks = rdCross.getLinks();
				boolean errorFlag = false;
				boolean error1Flag = false;
				for (IRow link: rdCrossLinks){
					RdCrossLink rdCrossLink = (RdCrossLink) link;
					int linkPid = rdCrossLink.getLinkPid();
					StringBuilder sb = new StringBuilder();
			        sb.append("select L.SPECIAL_TRAFFIC,F.FORM_OF_WAY from RD_LINK_FORM F,RD_LINK L where L.LINK_PID = F.LINK_PID and F.LINK_PID= ");
			        sb.append(linkPid);
			        sb.append(" and F.U_RECORD <> 2");
					String sql = sb.toString();
					PreparedStatement pstmt = this.getConn().prepareStatement(sql);		
					ResultSet resultSet = pstmt.executeQuery();
					
					int rdLinkSpecialTraffic = 0;
					List<Integer> formOfWayList = new ArrayList<Integer>();
					
					while(resultSet.next()){
						rdLinkSpecialTraffic = resultSet.getInt("SPECIAL_TRAFFIC");
						formOfWayList.add(resultSet.getInt("FORM_OF_WAY"));
					}
					if (!formOfWayList.contains(50)){
						errorFlag = true;
					}
					if (formOfWayList.contains(33) || (rdLinkSpecialTraffic == 1) ){
						error1Flag = true;
					}
					if (errorFlag && error1Flag) {
						break;
					}
				}
				if (errorFlag){
					String log1 = "路口内link不含交叉口内link属性";
					this.setCheckResult("", "[RD_CROSS,"+rdCross.getPid()+"]", 0, log1);
				}
				if (error1Flag){
					String log2 = "路口内link属性不能为环岛或特殊交通类型";
					this.setCheckResult("", "[RD_CROSS,"+rdCross.getPid()+"]", 0, log2);
				}
			}
			
		}
	}
	
	/**
	 * @param rdLink
	 * @param rdLinkPid
	 * @return
	 * @throws Exception
	 * @desc 判断link是否为路口内link
	 */
	public boolean isRdCrossLink(RdLink rdLink, int rdLinkPid) throws Exception{
		if (rdLink != null && rdLinkPid == 0){
			rdLinkPid = rdLink.getPid();
		}
		StringBuilder sb = new StringBuilder();
        sb.append("select rcl.LINK_PID from RD_CROSS_LINK rcl where RCL.U_RECORD != 2 AND (rcl.LINK_PID= ");
        sb.append(rdLinkPid);
        sb.append(" )");
		String sql = sb.toString();
		
		DatabaseOperator getObj=new DatabaseOperator();
		List<Object> resultList=new ArrayList<Object>();
		resultList=getObj.exeSelect(this.getConn(), sql);
		if (resultList.size() == 0){
			return false;
		}
		return true;
	}
	
	public List<Integer> getForms(int rdLinkPid) throws Exception{
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
	
}
