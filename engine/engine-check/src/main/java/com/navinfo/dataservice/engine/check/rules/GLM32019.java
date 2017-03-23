package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneCondition;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32019
 * @author songdongyan
 * @date 2017年3月22日
 * @Description: 
 * 检查原则：RD_LINK_LIMIT车辆限制允许的车辆类型与详细车道允许的车辆类型相同，否则报log
 * （1）如果RD_LINK_LIMIT中限制类型存在为Type=2的记录，则该link车道上允许的车辆类型应与限制类型中记录的车辆类型相同，否则报log1
 * （2）如果RD_LINK_LIMIT中限制类型不存在为TYPE=2的记录，且link存在有全封闭属性，车道上存在允许的车辆类型,且车辆类型不为2147484551或2147484160，2147484288,0，则报log2
 * （3）如果RD_LINK_LIMIT中限制类型不存在为TYPE=2的记录，且link存在有步行街属性，车道上存在允许的车辆类型,且车辆类型不为2147483786，则报log3 
 * （4）如果RD_LINK_LIMIT中限制类型不存在TYPE=2的记录，且link不存在有全封闭、公交车专用道、步行街属性，车道上存在允许的车辆类型，且车辆类型不为2147484559，2147484160，0，则报log4
 * （5）如果RD_LINK_LIMIT中限制类型不存在TYPE=2的记录，且link存在有步行街/公交车专用道属性，车道上不存在限制信息，则报log5
 * 
 * log1：车道上的车辆类型与link上车辆类型不一致
 * log2：全封闭属性的link上车道车辆类型存在错误
 * log3：步行街上车道车辆类型存在错误
 * log4：非步行街/公交车专用link上车道车辆类型存在错误
 * log5：步行街/公交车专用link上车道车辆类型存在错误
 */
public class GLM32019 extends baseRule{

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
		for(IRow row:checkCommand.getGlmList()){
			//道路限制信息编辑
			if (row instanceof RdLinkLimit){
				RdLinkLimit rdLinkLimit = (RdLinkLimit) row;
				this.checkRdLinkLimit(rdLinkLimit);
			}
			else if (row instanceof RdLane){
				RdLane rdLane = (RdLane) row;
				this.checkRdLane(rdLane.getPid());
			}
			else if(row instanceof RdLaneCondition){
				RdLaneCondition rdLaneCondition = (RdLaneCondition) row;
				this.checkRdLane(rdLaneCondition.getLanePid());
			}
		}
	}



	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdLane(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT DISTINCT '车道上的车辆类型与link上车辆类型不一致' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_LIMIT RLL         ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLL.LINK_PID                                   ");
		sb.append("   AND RLL.TYPE = 2                                                  ");
		sb.append("   AND RLC.VEHICLE <> RLL.VEHICLE                                    ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLL.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '车道上的车辆类型与link上车辆类型不一致' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LINK_LIMIT RLL                                ");
		sb.append(" WHERE RLN.LINK_PID = RLL.LINK_PID                                   ");
		sb.append("   AND RLL.TYPE = 2                                                  ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LANE_CONDITION RLC                                 ");
		sb.append("         WHERE RLN.LANE_PID = RLC.LANE_PID)                          ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLL.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '全封闭属性的link上车道车辆类型存在错误' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_FORM RLF          ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLF.LINK_PID                                   ");
		sb.append("   AND RLF.FORM_OF_WAY = 14                                          ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147484551, 2147484160, 2147484288, 0)    ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLF.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '步行街上车道车辆类型存在错误' LOG                   ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_FORM RLF          ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLF.LINK_PID                                   ");
		sb.append("   AND RLF.FORM_OF_WAY = 20                                          ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147483786)                               ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLF.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '非步行街/公交车专用link上车道车辆类型存在错误' LOG  ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC                            ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147484559, 2147484160, 0)                ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_FORM RLF                                      ");
		sb.append("         WHERE RLN.LINK_PID = RLF.LINK_PID                           ");
		sb.append("           AND RLF.FORM_OF_WAY IN (14, 22, 20))                      ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '步行街/公交车专用link上车道车辆类型存在错误' LOG    ");
		sb.append("  FROM RD_LANE RLN, RD_LINK_FORM LF                                  ");
		sb.append(" WHERE RLN.LINK_PID = LF.LINK_PID                                    ");
		sb.append("   AND LF.FORM_OF_WAY IN (22, 20)                                    ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LANE_CONDITION RLC                                 ");
		sb.append("         WHERE RLN.LANE_PID = RLC.LANE_PID)                          ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND LF.U_RECORD <> 2                                              ");
		sb.append("   AND RLN.LANE_PID = " + pid);
		
		String sql = sb.toString();
		log.info("RdLane GLM32019 sql:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		Iterator it = resultList.iterator();
        while(it.hasNext()){
			String target = "[RD_LANE," + pid + "]";
			this.setCheckResult("", target, 0,it.next().toString());
		}
	}

	/**
	 * @param rdLinkLimit
	 * @throws Exception 
	 */
	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT '车道上的车辆类型与link上车辆类型不一致' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_LIMIT RLL         ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLL.LINK_PID                                   ");
		sb.append("   AND RLL.TYPE = 2                                                  ");
		sb.append("   AND RLC.VEHICLE <> RLL.VEHICLE                                    ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLL.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '车道上的车辆类型与link上车辆类型不一致' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LINK_LIMIT RLL                                ");
		sb.append(" WHERE RLN.LINK_PID = RLL.LINK_PID                                   ");
		sb.append("   AND RLL.TYPE = 2                                                  ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LANE_CONDITION RLC                                 ");
		sb.append("         WHERE RLN.LANE_PID = RLC.LANE_PID)                          ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLL.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '全封闭属性的link上车道车辆类型存在错误' LOG         ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_FORM RLF          ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLF.LINK_PID                                   ");
		sb.append("   AND RLF.FORM_OF_WAY = 14                                          ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147484551, 2147484160, 2147484288, 0)    ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLF.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '步行街上车道车辆类型存在错误' LOG                   ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC, RD_LINK_FORM RLF          ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLN.LINK_PID = RLF.LINK_PID                                   ");
		sb.append("   AND RLF.FORM_OF_WAY = 20                                          ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147483786)                               ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLF.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '非步行街/公交车专用link上车道车辆类型存在错误' LOG  ");
		sb.append("  FROM RD_LANE RLN, RD_LANE_CONDITION RLC                            ");
		sb.append(" WHERE RLN.LANE_PID = RLC.LANE_PID                                   ");
		sb.append("   AND RLC.VEHICLE NOT IN (2147484559, 2147484160, 0)                ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_FORM RLF                                      ");
		sb.append("         WHERE RLN.LINK_PID = RLF.LINK_PID                           ");
		sb.append("           AND RLF.FORM_OF_WAY IN (14, 22, 20))                      ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND RLC.U_RECORD <> 2                                             ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		sb.append("                                                                     ");
		sb.append("UNION                                                                ");
		sb.append("                                                                     ");
		sb.append("SELECT DISTINCT '步行街/公交车专用link上车道车辆类型存在错误' LOG    ");
		sb.append("  FROM RD_LANE RLN, RD_LINK_FORM LF                                  ");
		sb.append(" WHERE RLN.LINK_PID = LF.LINK_PID                                    ");
		sb.append("   AND LF.FORM_OF_WAY IN (22, 20)                                    ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LINK_LIMIT RLL                                     ");
		sb.append("         WHERE RLN.LINK_PID = RLL.LINK_PID                           ");
		sb.append("           AND RLL.TYPE = 2)                                         ");
		sb.append("   AND NOT EXISTS (SELECT 1                                          ");
		sb.append("          FROM RD_LANE_CONDITION RLC                                 ");
		sb.append("         WHERE RLN.LANE_PID = RLC.LANE_PID)                          ");
		sb.append("   AND RLN.U_RECORD <> 2                                             ");
		sb.append("   AND LF.U_RECORD <> 2                                              ");
		sb.append("   AND RLN.LINK_PID = " + rdLinkLimit.getLinkPid());
		
		String sql = sb.toString();
		log.info("RdLinkLimit GLM32019 sql:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		Iterator it = resultList.iterator();
        while(it.hasNext()){
			String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
			this.setCheckResult("", target, 0,it.next().toString());
		}
		
	}

}
