package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkName;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/*
 * CHECK_CODE_KIND
 * 主从CODE编辑
 * 新增道路名
 * Link种别编辑
 */

public class CheckCodeKind extends baseRule {

    public CheckCodeKind() {

    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        // TODO Auto-generated method stub

    }
    
    public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//道路种别编辑
			if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
			}
			//名称类型编辑
			else if (row instanceof RdLinkName){
				RdLinkName rdLinkName = (RdLinkName) row;
				this.checkRdLinkName(rdLinkName);
			}
		}
	}

	/**
	 * @param rdLinkName
	 * @throws Exception 
	 */
	private void checkRdLinkName(RdLinkName rdLinkName) throws Exception {
		boolean checkFlg = false;
		if(rdLinkName.status().equals(ObjStatus.UPDATE)){
			if(rdLinkName.changedFields().containsKey("code")){
				checkFlg = true;
			}
		}
		else if(rdLinkName.status().equals(ObjStatus.INSERT)){
			checkFlg = true;
		}
		
		if(checkFlg){
			StringBuilder sb = new StringBuilder();
			
			sb.append(" SELECT 1 FROM RD_LINK R,RD_LINK_NAME N  ");
			sb.append(" WHERE R.LINK_PID = N.LINK_PID           ");
			sb.append(" AND R.KIND IN (1,2,3)                     ");
			sb.append(" AND N.CODE <> 1                         ");
			sb.append(" AND R.LINK_PID = " + rdLinkName.getLinkPid());
			sb.append(" AND R.U_RECORD <> 2                     ");
			sb.append(" AND N.U_RECORD <> 2                     ");
			sb.append(" UNION                                   ");
			sb.append(" SELECT 1 FROM RD_LINK R,RD_LINK_NAME N  ");
			sb.append(" WHERE R.LINK_PID = N.LINK_PID           ");
			sb.append(" AND R.KIND NOT IN (1,2,3)                 ");
			sb.append(" AND N.CODE <> 0                         ");
			sb.append(" AND R.LINK_PID = " + rdLinkName.getLinkPid());
			sb.append(" AND R.U_RECORD <> 2                     ");
			sb.append(" AND N.U_RECORD <> 2                     ");
			String sql = sb.toString();
			log.info("CHECK_CODE_KIND RDLINK--sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			if (!resultList.isEmpty()) {
				String target = "[RD_LINK," + rdLinkName.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			//道路种别编辑
			if(rdLink.changedFields().containsKey("kind")){
//				int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
				StringBuilder sb = new StringBuilder();
				
				sb.append(" SELECT 1 FROM RD_LINK R,RD_LINK_NAME N  ");
				sb.append(" WHERE R.LINK_PID = N.LINK_PID           ");
				sb.append(" AND R.KIND IN (1,2)                     ");
				sb.append(" AND N.CODE <> 1                         ");
				sb.append(" AND R.LINK_PID = " + rdLink.getPid());
				sb.append(" AND R.U_RECORD <> 2                     ");
				sb.append(" AND N.U_RECORD <> 2                     ");
				sb.append(" UNION                                   ");
				sb.append(" SELECT 1 FROM RD_LINK R,RD_LINK_NAME N  ");
				sb.append(" WHERE R.LINK_PID = N.LINK_PID           ");
				sb.append(" AND R.KIND NOT IN (1,2)                 ");
				sb.append(" AND N.CODE <> 0                         ");
				sb.append(" AND R.LINK_PID = " + rdLink.getPid());
				sb.append(" AND R.U_RECORD <> 2                     ");
				sb.append(" AND N.U_RECORD <> 2                     ");
				String sql = sb.toString();
				log.info("CHECK_CODE_KIND RDLINK--sql:" + sql);

				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				if (!resultList.isEmpty()) {
					String target = "[RD_LINK," + rdLink.getPid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}

//    @Override
//    public void postCheck(CheckCommand checkCommand) throws Exception {
//        List<Integer> linkPidList = new ArrayList<Integer>();
//        for (IRow obj : checkCommand.getGlmList()) {
//            if (obj instanceof RdLink) {
//                RdLink rdLink = (RdLink) obj;
//
//                if (linkPidList.contains(rdLink.getPid())) {
//                    continue;
//                } else {
//                    linkPidList.add(rdLink.getPid());
//                }
//
//                // rd_link_name
//                List<IRow> names = new AbstractSelector(RdLinkName.class, getConn()).loadRowsByParentId(rdLink.getPid
//                        (), false);
//
//                int kind = 0;
//
//                Map<String, Object> changeFields = rdLink.changedFields();
//                if (changeFields != null && changeFields.containsKey("kind")) {
//                    kind = (int) changeFields.get("kind");
//                } else {
//                    kind = rdLink.getKind();
//                }
//                // LINK种别为高速、城市高速、国道时，主从CODE字段设为1，其他为0
//                if (kind == 1 || kind == 2 || kind == 3) {
//                    for (IRow name : names) {
//                        RdLinkName rdLinkName = (RdLinkName) name;
//                        if (rdLinkName.getCode() != 1) {
//                            String target = "[RD_LINK," + rdLink.getPid() + "]";
//                            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
//                        }
//                    }
//                } else {
//                    for (IRow name : names) {
//                        RdLinkName rdLinkName = (RdLinkName) name;
//                        if (rdLinkName.getCode() != 0) {
//                            String target = "[RD_LINK," + rdLink.getPid() + "]";
//                            this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
//                        }
//                    }
//                }
//            } else if (obj instanceof RdLinkName) {
//                RdLinkName rdLinkName = (RdLinkName) obj;
//                int linkPid = rdLinkName.getLinkPid();
//                if (linkPidList.contains(linkPid)) {
//                    continue;
//                } else {
//                    linkPidList.add(linkPid);
//                }
//
//                RdLinkSelector rdSelector = new RdLinkSelector(getConn());
//                RdLink rdLink = (RdLink) rdSelector.loadByIdOnlyRdLink(linkPid, false);
//
//                int code = 0;
//                Map<String, Object> changeFields = rdLinkName.changedFields();
//                if (changeFields != null && changeFields.containsKey("kind")) {
//                    code = (int) changeFields.get("code");
//                } else {
//                    code = rdLinkName.getCode();
//                }
//
//                int kind = rdLink.getKind();
//                // LINK种别为高速、城市高速、国道时，主从CODE字段设为1，其他为0
//                if (kind == 1 || kind == 2 || kind == 3) {
//                    if (code != 1) {
//                        String target = "[RD_LINK," + rdLink.getPid() + "]";
//                        this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
//                    }
//                } else {
//                    if (code != 0) {
//                        String target = "[RD_LINK," + rdLink.getPid() + "]";
//                        this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());
//                    }
//                }
//            }
//        }
//    }


}
