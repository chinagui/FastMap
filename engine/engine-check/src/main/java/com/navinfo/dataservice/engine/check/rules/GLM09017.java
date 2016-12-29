package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.warninginfo.RdWarninginfo;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM09017
 * @author songdongyan
 * @date 2016年12月29日
 * @Description: 警示信息标牌类型“会车让行”不允许制作在单方向的link上，否则报log。
 * 道路方向编辑后检查
 * 标牌类型编辑后检查
 */
public class GLM09017 extends baseRule{

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
			//道路方向修改
			if(obj instanceof RdLink){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink,checkCommand);
			}
			//标牌类型编辑
			else if(obj instanceof RdWarninginfo){
				RdWarninginfo rdWarninginfo=(RdWarninginfo) obj;
				checkRdWarningInfo(rdWarninginfo);
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
			//单方向触发检查
			if(typeCode.equals("20301")){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK RL,RD_WARNINGINFO RW");
				sb.append(" WHERE RW.LINK_PID = " + rdWarninginfo.getPid());
				sb.append(" AND RL.DIRECT IN (2, 3)");
				sb.append(" AND RL.LINK_PID = RW.LINK_PID");
				sb.append(" AND RW.U_RECORD <> 2");
				sb.append(" AND RL.U_RECORD <> 2");

				String sql = sb.toString();
				log.info("RdWarninginfo后检查GLM09017:" + sql);

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

	/**
	 * @param rdLink
	 * @param checkCommand
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, CheckCommand checkCommand) throws Exception {
		//道路方向编辑
		if(rdLink.changedFields().containsKey("direct")){
			int direct = Integer.parseInt(rdLink.changedFields().get("direct").toString());
			//单方向触发检查
			if(direct == 2||direct == 3){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_WARNINGINFO RW ");
				sb.append(" WHERE RW.LINK_PID = " + rdLink.getPid());
				sb.append(" AND RW.TYPE_CODE = '20301'");
				sb.append(" AND RW.U_RECORD <> 2");
				sb.append(" AND F.LINK_PID = " + rdLink.getPid());

				String sql = sb.toString();
				log.info("RdLink后检查GLM09017:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);

				if(resultList.size()>0){
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
		
	}

}
