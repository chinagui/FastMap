package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM09012
 * @author songdongyan
 * @date 2016年12月29日
 * @Description: 在一组警示信息的线点关系中，如果线具有交叉口link属性，则其警示信息类型只能是“停车让行”或者“减速让行”,否则报log
 * 道路属性编辑服务端后检查
 * 标牌类型编辑服务端后检查
 */
public class GLM09012 extends baseRule{

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
			//LINK属性修改
			if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdRdLinkForm(rdLinkForm);
			}
			//标牌类型编辑
			else if(obj instanceof RdWarninginfo){
				RdWarninginfo rdWarninginfo=(RdWarninginfo) obj;
				checkRdWarningInfo(rdWarninginfo);
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		//道路属性编辑
		if(rdLinkForm.changedFields().containsKey("formOfWay")){
			int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
			if(formOfWay == 50){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_WARNINGINFO W ");
				sb.append(" WHERE W.U_RECORD <> 2");
				sb.append(" AND (W.TYPE_CODE NOT IN ('20101', '20201'))");
				sb.append(" AND W.LINK_PID = " + rdLinkForm.getLinkPid());

				String sql = sb.toString();
				log.info("RdLinkForm前后检查GLM09012:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLinkForm.getLinkPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}
	
	/**
	 * @param rdWarninginfo
	 * @throws Exception 
	 */
	private void checkRdWarningInfo(RdWarninginfo rdWarninginfo) throws Exception {
		//标牌类型编辑
		if(rdWarninginfo.changedFields().containsKey("typeCode")){
			String typeCode = rdWarninginfo.changedFields().get("typeCode").toString();
			//非”停车让行”或者“减速让行标牌触发检查
			if(!typeCode.equals("20101")&&!typeCode.equals("20201")){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_WARNINGINFO R, RD_LINK L, RD_LINK_FORM F");
				sb.append(" WHERE R.PID = " + rdWarninginfo.getPid());
				sb.append(" AND R.U_RECORD != 2");
				sb.append(" AND L.U_RECORD != 2");
				sb.append(" AND F.U_RECORD != 2");
				sb.append(" AND R.LINK_PID = L.LINK_PID");
				sb.append(" AND L.LINK_PID = F.LINK_PID");
				sb.append(" AND F.FORM_OF_WAY = 50");

				String sql = sb.toString();
				log.info("RdWarninginfo后检查GLM09012:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_WARNINGINFO," + rdWarninginfo.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}
}
