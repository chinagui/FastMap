package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM19013
 * @author songdongyan
 * @date 2017年1月19日
 * @Description: 环岛和特殊交通类型不可以作为车信的进入线
 */
public class GLM19013 extends baseRule{

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
			if(obj instanceof RdLink ){
				RdLink rdLink=(RdLink) obj;
				checkRdLink(rdLink);
			}
			else if(obj instanceof RdLinkForm){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
		}
		
	}

	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				//交叉口内道路修改为非交叉口内道路
				if(rdLinkForm.getFormOfWay()==50){
					checkFlg = true;
				}
				//环岛
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWay==33){
					checkFlg = true;
				}
			}
		}else if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			int formOfWay = rdLinkForm.getFormOfWay();
			if(formOfWay==33){
				checkFlg = true;
			}
		}

		if(checkFlg){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_LANE_CONNEXITY C,RD_LINK_FORM F");
			sb2.append(" WHERE C.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND C.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = C.IN_LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 33");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_TOPOLOGY L,RD_LINK_FORM F");
			sb2.append(" WHERE L.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND L.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = L.OUT_LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 33");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_LANE_VIA V,RD_LINK_FORM F");
			sb2.append(" WHERE V.LINK_PID = " + rdLinkForm.getLinkPid());
			sb2.append(" AND V.U_RECORD <> 2");
			sb2.append(" AND F.LINK_PID = V.LINK_PID");
			sb2.append(" AND F.FORM_OF_WAY = 33");
			sb2.append(" AND F.U_RECORD <> 2");
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM FF");
			sb2.append(" WHERE FF.LINK_PID = V.LINK_PID");
			sb2.append(" AND FF.FORM_OF_WAY = 50");
			sb2.append(" AND FF.U_RECORD <> 2)");


			String sql2 = sb2.toString();
			log.info("RdLinkForm后检查GLM19013:" + sql2);

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
		if(rdLink.changedFields().isEmpty()){
			return;
		}else{
			if(rdLink.changedFields().containsKey("specialTraffic")){
				int specialTraffic = Integer.parseInt(rdLink.changedFields().get("specialTraffic").toString());
				//特殊交通
				if(specialTraffic == 1){
					StringBuilder sb2 = new StringBuilder();

					sb2.append("SELECT 1 FROM RD_LANE_CONNEXITY C");
					sb2.append(" WHERE C.IN_LINK_PID = " + rdLink.getPid());
					sb2.append(" AND C.U_RECORD <> 2");
					sb2.append(" UNION");
					sb2.append(" SELECT 1 FROM FROM RD_LANE_TOPOLOGY L");
					sb2.append(" WHERE L.OUT_LINK_PID = " + rdLink.getPid());
					sb2.append(" AND L.U_RECORD <> 2");
					sb2.append(" UNION");
					sb2.append(" SELECT 1 FROM RD_LANE_VIA V");
					sb2.append(" WHERE V.LINK_PID = " + rdLink.getPid());
					sb2.append(" AND V.U_RECORD <> 2");
					sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F");
					sb2.append(" WHERE F.LINK_PID = V.LINK_PID");
					sb2.append(" AND F.FORM_OF_WAY = 50");
					sb2.append(" AND F.U_RECORD <> 2)");


					String sql2 = sb2.toString();
					log.info("RdLink后检查GLM19013:" + sql2);

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
	}

}
