package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ClassName GLM33006
 * @author Han Shaoming(修改)
 * @date 2017年3月23日 下午6:28:59
 * @Description TODO
 * 制作可变限速的进入LINK、退出link和接续LINK不能为作业中道路，步行道路，渡轮，人渡，其他道路，私道，非引导道路，交叉点内道路、
 * 环岛、特殊交通类型、M（转弯道）、II（交叉点内部道路）、无时间段的车辆限制、步行街、穿行限制
 * 修改可变限速	服务端后检查
 * 新增可变限速	服务端后检查
 * Link种别编辑	服务端后检查
 * 道路属性编辑	服务端后检查
 * 限制类型编辑	服务端后检查
 * 时间段编辑（link限制表）	服务端后检查
 */
public class GLM33006 extends baseRule {
    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {

    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdVariableSpeed) {
            	RdVariableSpeed speed = (RdVariableSpeed) row;
            	List<Integer> pids = new ArrayList<Integer>();
            	if(speed.status().equals(ObjStatus.INSERT)){
            		pids.add(speed.getInLinkPid());
            		int outLinkPid = speed.getOutLinkPid();
            		pids.add(outLinkPid);
            		for (IRow via : speed.getVias()) {
            			RdVariableSpeedVia speedVia = (RdVariableSpeedVia) via;
            			pids.add(speedVia.getLinkPid());
            		}
            	}else if(speed.status().equals(ObjStatus.UPDATE)){
            		Map<String, Object> changedFields = speed.changedFields();
        			if(changedFields != null && !changedFields.isEmpty() ){
        				if(changedFields.containsKey("outLinkPid")){
        					int outLinkPid = Integer.valueOf(speed.changedFields().get("outLinkPid").toString());
        					pids.add(outLinkPid);
        				}
        			}
            	}
            	if(!pids.isEmpty()){
            		boolean check = this.check(pids);
            		if(check){
            			String target = "[RD_VARIABLE_SPEED," + speed.getPid() + "]";
    					this.setCheckResult("", target, 0);
            		}
            	}

//                String sql = "SELECT 1 FROM RD_LINK RL, RD_LINK_FORM RLF, RD_LINK_LIMIT RLL WHERE RL.LINK_PID IN (" +
//                        StringUtils.getInteStr(pids) + ") AND RL.LINK_PID = RLF.LINK_PID AND RL.LINK_PID = RLL" + ""
//                        + ".LINK_PID AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2 AND RLL.U_RECORD <> 2 AND (RL.KIND "
//                        + "IN" + " " + "(0, 8, 9, 10, 11, 13) OR RLF.FORM_OF_WAY IN (18, 20, 33, 50) OR RL.IMI_CODE "
//                        + "IN " + "(1, 2) " + "OR " + "(RLL.TYPE = 2 AND RLL.TIME_DOMAIN IS NULL) OR RLL.TYPE = 3)";
//                DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
//                List<Object> resultList = getObj.exeSelect(getConn(), sql);
//				if (!resultList.isEmpty())
//                    this.setCheckResult("", "", 0);
            } else if(row instanceof RdVariableSpeedVia){
            	RdVariableSpeedVia speedVia = (RdVariableSpeedVia) row;
            	List<Integer> pids = new ArrayList<Integer>();
            	int linkPid = speedVia.getLinkPid();
            	if(!speedVia.status().equals(ObjStatus.DELETE)){
            		Map<String, Object> changedFields = speedVia.changedFields();
        			if(changedFields != null && !changedFields.isEmpty() ){
        				if(changedFields.containsKey("linkPid")){
        					linkPid = Integer.valueOf(speedVia.changedFields().get("linkPid").toString());
        				}
        			}
            	}
            	pids.add(linkPid);
            	if(!pids.isEmpty()){
            		boolean check = this.check(pids);
            		if(check){
            			String target = "[RD_VARIABLE_SPEED," + speedVia.getVspeedPid() + "]";
    					this.setCheckResult("", target, 0);
            		}
            	}
            }
            else if (row instanceof RdLink && row.status() == ObjStatus.UPDATE) {
                RdLink link = (RdLink) row;

                int kind = link.getKind();
                if (link.changedFields().containsKey("kind"))
                    kind = Integer.valueOf(link.changedFields().get("kind").toString());

                int imiCode = link.getImiCode();
                if (link.changedFields().containsKey("imiCode"))
                    imiCode = Integer.valueOf(link.changedFields().get("imiCode").toString());

                int specialTraffic = link.getSpecialTraffic();
                if (link.changedFields().containsKey("specialTraffic"))
                    specialTraffic = Integer.valueOf(link.changedFields().get("specialTraffic").toString());

                if (Arrays.asList(new Integer[]{0, 8, 9, 10, 11, 13}).contains(kind) || imiCode == 1 || imiCode == 2
                        || specialTraffic == 1) {
                    List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                            (link.pid(), false);
                    List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                            .loadRdVariableSpeedByViaLinkPid(link.pid(), false);

                    if (!list1.isEmpty() || !list2.isEmpty()) {
                        setCheckResult(link.getGeometry(), "[RD_LINK," + link.pid() + "]", link.mesh());
                    }
                }
            } else if (row instanceof RdLinkForm && row.status() != ObjStatus.DELETE) {
                RdLinkForm form = (RdLinkForm) row;

                int formOfWay = form.getFormOfWay();
                if (form.changedFields().containsKey("formOfWay"))
                    formOfWay = Integer.valueOf(form.changedFields().get("formOfWay").toString());

                if (Arrays.asList(new Integer[]{18, 20, 33, 50}).contains(formOfWay)) {
                    List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                            (form.getLinkPid(), false);
                    List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                            .loadRdVariableSpeedByViaLinkPid(form.getLinkPid(), false);
                    if (!list1.isEmpty() || !list2.isEmpty()) {
                        setCheckResult("", "[RD_LINK," + form.getLinkPid() + "]", 0);
                    }
                }
            } else if (row instanceof RdLinkLimit && row.status() != ObjStatus.DELETE) {
                RdLinkLimit linkLimit = (RdLinkLimit) row;

                int type = linkLimit.getType();
                if (linkLimit.changedFields().containsKey("type"))
                    type = Integer.valueOf(linkLimit.changedFields().get("type").toString());

                if (type == 3) {
                	List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                            (linkLimit.getLinkPid(), false);
                    List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                            .loadRdVariableSpeedByViaLinkPid(linkLimit.getLinkPid(), false);
                    if (!list1.isEmpty() || !list2.isEmpty()) {
                    	setCheckResult("", "[RD_LINK," + linkLimit.getLinkPid() + "]", 0);
                    }
                } else if (type == 2) {
                    String timeDomain = linkLimit.getTimeDomain();
                    if (linkLimit.changedFields().containsKey("timeDomain"))
                        timeDomain = linkLimit.changedFields().get("timeDomain").toString();

                    if (StringUtils.isEmpty(timeDomain)) {
                    	List<RdVariableSpeed> list1 = new RdVariableSpeedSelector(getConn()).loadRdVariableSpeedByLinkPid
                                (linkLimit.getLinkPid(), false);
                        List<RdVariableSpeed> list2 = new RdVariableSpeedSelector(getConn())
                                .loadRdVariableSpeedByViaLinkPid(linkLimit.getLinkPid(), false);
                        if (!list1.isEmpty() || !list2.isEmpty()) {
                        	setCheckResult("", "[RD_LINK," + linkLimit.getLinkPid() + "]", 0);
                        }
                    }
                }
            }
        }
    }
    
    /**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(List<Integer> pids) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT RL.LINK_PID FROM RD_LINK RL WHERE RL.LINK_PID IN ("+StringUtils.getInteStr(pids) + ")");
		sb.append(" AND (RL.KIND IN (0, 8, 9, 10, 11, 13)OR RL.IMI_CODE IN (1, 2))");
		sb.append(" AND RL.U_RECORD <>2");
		sb.append(" UNION");
		sb.append(" SELECT RLF.LINK_PID FROM RD_LINK_FORM RLF");
		sb.append(" WHERE RLF.LINK_PID IN ("+StringUtils.getInteStr(pids) + ")");
		sb.append(" AND RLF.FORM_OF_WAY IN (18, 20, 33, 50) AND RLF.U_RECORD <>2");
		sb.append(" UNION");
		sb.append(" SELECT RLL.LINK_PID FROM RD_LINK_LIMIT RLL");
		sb.append(" WHERE RLL.LINK_PID IN ("+StringUtils.getInteStr(pids) + ")");
		sb.append(" AND((RLL. TYPE = 2 AND RLL.TIME_DOMAIN IS NULL)");
		sb.append(" OR RLL. TYPE = 3) AND RLL.U_RECORD <>2");
		String sql = sb.toString();
		log.info("后检查GLM33006--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
