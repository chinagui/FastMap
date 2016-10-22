package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
* @ClassName: GLM26050_1 
* @author: zhangpengpeng 
* @date: 2016年9月12日
* @Desc: GLM26050_1.java 路口中的线必须是交叉口内LINK，否则报LOG" 检查时机：修改路口、新增路口、道路属性编辑"
*/
public class GLM26050_1 extends baseRule{
	public GLM26050_1(){	
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj: checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink) obj;
				int rdLinkPid = rdLink.getPid();
				if (isRdCrossLink(rdLink,0)){
					List<Integer> formOfWayList = getForms(rdLinkPid);
					if (!formOfWayList.contains(50)){
						this.setCheckResult(rdLink.getGeometry(), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}else if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				int linkPid = rdLinkForm.getLinkPid();
				if (isRdCrossLink(null,linkPid)){
					List<Integer> formOfWayList = getForms(linkPid);
					if (!formOfWayList.contains(50)){
						this.setCheckResult("", "[RD_LINK,"+linkPid+"]",0);
					}
				}
			}else if (obj instanceof RdCross){
				RdCross rdCross = (RdCross) obj;
				List<IRow> links = rdCross.getLinks();
				boolean errorFlag = false;
				for (IRow link: links){
					RdCrossLink rdCrossLink = (RdCrossLink) link;
					int linkPid = rdCrossLink.getLinkPid();
					StringBuilder sb = new StringBuilder();
			        sb.append("select F.FORM_OF_WAY from RD_LINK_FORM F where F.LINK_PID= ");
			        sb.append(linkPid);
			        sb.append(" and F.U_RECORD <> 2");
					String sql = sb.toString();
					
					DatabaseOperator getObj=new DatabaseOperator();
					List<Object> resultList=new ArrayList<Object>();
					resultList=getObj.exeSelect(this.getConn(), sql);
					
					List<Integer> formOfWayList = new ArrayList<Integer>();
					for (Object form: resultList){
						formOfWayList.add(Integer.parseInt(form.toString()));
					}
					if (!formOfWayList.contains(50)){
						errorFlag = true;
						break;
					}
				}
				if (errorFlag){
					this.setCheckResult("", "[RD_CROSS,"+rdCross.getPid()+"]", 0);
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
