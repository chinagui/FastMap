package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
* @Desc: 路口内Link表中记录的道路必须具有交叉口内link属性，且一定不能具有环岛属性，特殊交通必须为“否”，否则报log
* 新增路口后检查：RdCross
* 修改路口后检查:RdCross,RdCrossLink
* 道路属性编辑后检查:RdLink,RdLinkForm
*/
public class GLM26044 extends baseRule{
	protected Logger log = Logger.getLogger(this.getClass());
			
	public GLM26044(){
	}
	
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj: checkCommand.getGlmList()){
			//道路属性编辑RdLink
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink);
			}
			//道路属性编辑RdLinkForm
			else if (obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm = (RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
			//新增/修改路口RdCross
			else if (obj instanceof RdCross){
				RdCross rdCross = (RdCross) obj;
				checkRdCross(rdCross);
			}
			//修改路口RdCrossLink
			else if (obj instanceof RdCrossLink){
				RdCrossLink rdCrossLink = (RdCrossLink) obj;
				checkRdCrossLink(rdCrossLink);
			}
		}
	}
	
	/**
	 * @param rdCrossLink
	 * @throws Exception 
	 */
	private void checkRdCrossLink(RdCrossLink rdCrossLink) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL, RD_LINK RL");
		sb.append(" WHERE RCL.LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.SPECIAL_TRAFFIC <> 0");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RCL.LINK_PID = " + rdCrossLink.getLinkPid());
		sb.append(" UNION");
		sb.append(" SELECT '路口内link不含交叉口内link属性' FROM RD_CROSS_LINK RCL");
		sb.append(" WHERE NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
		sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 50");
		sb.append(" AND RLF.U_RECORD <> 2)");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RCL.LINK_PID = " + rdCrossLink.getLinkPid());
		sb.append(" UNION");
		sb.append(" SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL");
		sb.append(" WHERE NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
		sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 33");
		sb.append(" AND RLF.U_RECORD <> 2)");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RCL.LINK_PID = " + rdCrossLink.getLinkPid());

		String sql = sb.toString();
		log.info("RdCrossLink后检查GLM26044:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_CROSS," + rdCrossLink.getPid() + "]";
			this.setCheckResult("", target, 0,resultList.get(0).toString());
		}
		
	}

	/**
	 * @param rdCross
	 * @throws Exception 
	 */
	private void checkRdCross(RdCross rdCross) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL, RD_LINK RL");
		sb.append(" WHERE RCL.LINK_PID = RL.LINK_PID");
		sb.append(" AND RL.SPECIAL_TRAFFIC <> 0");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RL.U_RECORD <> 2");
		sb.append(" AND RCL.PID = " + rdCross.getPid());
		sb.append(" UNION");
		sb.append(" SELECT '路口内link不含交叉口内link属性' FROM RD_CROSS_LINK RCL");
		sb.append(" WHERE NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
		sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 50");
		sb.append(" AND RLF.U_RECORD <> 2)");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RCL.PID = " + rdCross.getPid());
		sb.append(" UNION");
		sb.append(" SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL,RD_LINK_FORM RLF");
		sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND RLF.FORM_OF_WAY = 33");
		sb.append(" AND RLF.U_RECORD <> 2");
		sb.append(" AND RCL.U_RECORD <> 2");
		sb.append(" AND RCL.PID = " + rdCross.getPid());

		String sql = sb.toString();
		log.info("RdCross后检查GLM26044:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_CROSS," + rdCross.getPid() + "]";
			this.setCheckResult("", target, 0,resultList.get(0).toString());
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//link属性为路口内link或环岛
		if(rdLinkForm.changedFields().containsKey("formOfWay")){
			int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString()) ;
			if(formOfWay==33){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL,RD_LINK_FORM RLF");
				sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
				sb.append(" AND RLF.U_RECORD <> 2");
				sb.append(" AND RCL.U_RECORD <> 2");
				sb.append(" AND RLF.LINK_PID = " + rdLinkForm.getLinkPid());

				String sql = sb.toString();
				log.info("RdLinkForm后检查GLM26044:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
					this.setCheckResult("", target, 0,resultList.get(0).toString());
				}
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception { 
		if(rdLink.changedFields().containsKey("specialTraffic")){
			int specialTraffic = Integer.parseInt(rdLink.changedFields().get("specialTraffic").toString());
			if(specialTraffic==1){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL, RD_LINK RL");
				sb.append(" WHERE RCL.LINK_PID = RL.LINK_PID");
				sb.append(" AND RL.SPECIAL_TRAFFIC <> 0");
				sb.append(" AND RCL.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");
				sb.append(" AND RL.LINK_PID = " + rdLink.getPid());
				sb.append(" UNION");
				sb.append(" SELECT '路口内link不含交叉口内link属性' FROM RD_CROSS_LINK RCL, RD_LINK RL");
				sb.append(" WHERE RCL.LINK_PID = RL.LINK_PID");
				sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
				sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
				sb.append(" AND RLF.FORM_OF_WAY = 50");
				sb.append(" AND RLF.U_RECORD <> 2)");
				sb.append(" AND RCL.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");
				sb.append(" AND RL.LINK_PID = " + rdLink.getPid());
				sb.append(" UNION");
				sb.append(" SELECT '路口内link属性不能为环岛或特殊交通类型' FROM RD_CROSS_LINK RCL, RD_LINK RL");
				sb.append(" WHERE RCL.LINK_PID = RL.LINK_PID");
				sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
				sb.append(" WHERE RCL.LINK_PID = RLF.LINK_PID");
				sb.append(" AND RLF.FORM_OF_WAY = 33");
				sb.append(" AND RLF.U_RECORD <> 2)");
				sb.append(" AND RCL.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");
				sb.append(" AND RL.LINK_PID = " + rdLink.getPid());
				
				String sql = sb.toString();
				log.info("RdLink后检查GLM26044:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0,resultList.get(0).toString());
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
