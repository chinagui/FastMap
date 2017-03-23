package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaixin on 2016/12/20 0020.
 * RD_HWHG_LIMIT表的关联link不能为9、10级路，否则报log
 * link种别编辑服务端后检查
 * 新增限高限重服务端后检查
 */
public class GLM35004 extends baseRule {
    private Logger log = Logger.getLogger(GLM35004.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
        	if(row instanceof RdHgwgLimit){
        		RdHgwgLimit rdHgwgLimit = (RdHgwgLimit) row;
				checkRdHgwgLimit(rdHgwgLimit);
        	}
        	
        	else if(row instanceof RdLink){
        		RdLink rdLink = (RdLink) row;
				checkRdLink(rdLink);
        	}
        	
            if (!(row instanceof RdLinkForm))
                return;
            RdLinkForm form = (RdLinkForm) row;
            int newKind = -1;
            if (form.changedFields().containsKey("formOfWay")) {
                newKind = (int) form.changedFields().get("formOfWay");
            }
            if ((newKind == -1 && (form.getFormOfWay() != 9 && form.getFormOfWay() != 10)) && newKind != 9 && newKind != 10)
                return;
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT RL.GEOMETRY,'[RD_LINK,' || RL.LINK_PID || ']'");
            sql.append(",RL.MESH_ID FROM RD_LINK RL, RD_HGWG_LIMIT RHL WHERE RL.LINK_PID = ");
            sql.append(form.getLinkPid());
            sql.append(" AND RL.LINK_PID = RHL.LINK_PID ");
            DatabaseOperatorResultWithGeo getObj = new DatabaseOperatorResultWithGeo();
            List<Object> resultList = getObj.exeSelect(this.getConn(), sql.toString());
            if (resultList.isEmpty())
                return;
            this.setCheckResult(resultList.get(0).toString(), resultList.get(1).toString(), (int) resultList.get(2));
        }
    }

	/**
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			if(rdLink.changedFields().containsKey("kind")){
				int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString());
				if((kind==9)||(kind==10)){
					StringBuffer sql = new StringBuffer();
					
		            sql.append("SELECT 1 ");
		            sql.append(" FROM RD_LINK RL, RD_HGWG_LIMIT RHL");
		            sql.append(" WHERE RL.LINK_PID = RHL.LINK_PID ");
		            sql.append(" AND RL.LINK_PID = " + rdLink.getPid());
		            sql.append(" AND RL.U_RECORD <> 2 ");
		            sql.append(" AND RHL.U_RECORD <> 2 ");
		            
		            log.info("GLM35004 RdLink SQL:" + sql.toString());
		            
		            DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql.toString());
					
			        if(!resultList.isEmpty()){
						String target = "[RD_LINK," + rdLink.getPid() + "]";
						this.setCheckResult("", target, 0);
					}
				}
			}
		}
		
	}

	/**
	 * @param rdHgwgLimit
	 * @throws Exception 
	 */
	private void checkRdHgwgLimit(RdHgwgLimit rdHgwgLimit) throws Exception {
		if(rdHgwgLimit.status().equals(ObjStatus.INSERT)){
			StringBuffer sql = new StringBuffer();
			
            sql.append("SELECT 1 ");
            sql.append(" FROM RD_LINK RL, RD_HGWG_LIMIT RHL");
            sql.append(" WHERE RL.LINK_PID = RHL.LINK_PID ");
            sql.append(" AND RHL.PID = " + rdHgwgLimit.getPid());
            sql.append(" AND RL.U_RECORD <> 2 ");
            sql.append(" AND RHL.U_RECORD <> 2 ");
            sql.append(" AND RL.KIND IN (9,10) ");
            
            log.info("GLM35004 RdHgwgLimit SQL:" + sql.toString());
            
            DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql.toString());
			
	        if(!resultList.isEmpty()){
				String target = "[RD_HGWG_LIMIT," + rdHgwgLimit.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}
}
