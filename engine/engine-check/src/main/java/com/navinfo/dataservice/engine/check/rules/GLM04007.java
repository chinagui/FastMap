package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGateCondition;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04007
 * @author songdongyan
 * @date 2017年3月22日
 * @Description: 大门类型为紧急车辆进入，不允许制作时间段信息
 * 通行时间编辑服务端后检查
 * 大门类型编辑服务端后检查
 */
public class GLM04007 extends baseRule{

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
		for (IRow obj : checkCommand.getGlmList()) {
			// 大门类型编辑RdGate
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				if(rdGate.status().equals(ObjStatus.UPDATE)){
					if(rdGate.changedFields().containsKey("type")){
						int type = Integer.parseInt(rdGate.changedFields().get("type").toString());
						if(type==1){
							checkRdGate(rdGate.getPid());
						}
					}
				}
			}
			//通行时间短编辑RdGateCondition
			else if (obj instanceof RdGateCondition) {
				RdGateCondition rdGateCondition = (RdGateCondition) obj;
				if(!rdGateCondition.status().equals(ObjStatus.DELETE)){
					String timeDomain = rdGateCondition.getTimeDomain();
					if(rdGateCondition.changedFields().containsKey("timeDomain")){
						timeDomain = rdGateCondition.changedFields().get("timeDomain").toString();
					}
					if(timeDomain!=null){
						checkRdGate(rdGateCondition.getPid());
					}
				}
			}	
		}	
		
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void checkRdGate(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1                                  ");
		sb.append("  FROM RD_GATE G, RD_GATE_CONDITION C     ");
		sb.append(" WHERE G.PID = " + pid);
		sb.append("   AND G.PID = C.PID                      ");
		sb.append("   AND G.TYPE = 0                         ");
		sb.append("   AND G.U_RECORD != 2                    ");
		sb.append("   AND C.U_RECORD != 2                    ");
		sb.append("   AND C.TIME_DOMAIN IS NOT NULL          ");

		String sql = sb.toString();
		log.info("RdGate GLM04007 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			this.setCheckResult("", "[RD_GATE," + pid + "]", 0);
		}
		
	}

}
