package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @author songdongyan
 * @ClassName: GLM01570
 * @date 2016年12月6日
 * @Description: 大门的进入、退出线上同时有风景路线属性，则报log
 * 道路属性编辑服务端后检查：RdLink,RdLinkForm
 * 新增大门服务端后检查:RdGate
 * 大门进入线退出线均为风景路线属性则报错误。
 */
public class GLM01570 extends baseRule {

    /**
     *
     */
    public GLM01570() {
        // TODO Auto-generated constructor stub
    }

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
//			// 道路属性编辑RdLink
//            if (obj instanceof RdLink) {
//                RdLink rdLink = (RdLink) obj;
//                checkRdLink(rdLink, checkCommand.getOperType());
//            }
            // 道路属性编辑RdLinkForm
            if (obj instanceof RdLinkForm) {
                RdLinkForm rdLinkForm = (RdLinkForm) obj;
                checkRdLinkForm(rdLinkForm, checkCommand.getOperType());
            }
            // 大门RdGate
            else if (obj instanceof RdGate) {
                RdGate rdGate = (RdGate) obj;
                checkRdGate(rdGate, checkCommand.getOperType());
            }
        }

    }

    /**
     * @param rdGate
     * @param operType
     * @throws Exception
     */
    private void checkRdGate(RdGate rdGate, OperType operType) throws Exception {
        //新增大门
//		operType=OperType.CREATE;
        if (operType.equals(OperType.CREATE)) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT 1 FROM RD_GATE RG, RD_LINK_FORM CL1, RD_LINK_FORM CL2");
            sb.append(" WHERE RG.IN_LINK_PID = CL1.LINK_PID");
            sb.append(" AND RG.OUT_LINK_PID = CL2.LINK_PID");
            sb.append(" AND CL1.FORM_OF_WAY = 60");
            sb.append(" AND CL2.FORM_OF_WAY = 60");
            sb.append(" AND CL1.U_RECORD <> 2");
            sb.append(" AND CL2.U_RECORD <> 2");
            sb.append(" AND RG.PID = " + rdGate.getPid());

            String sql = sb.toString();
            log.info("RdGate后检查GLM01570:" + sql);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql);

            if (resultList.size() > 0) {
                String target = "[RD_GATE," + rdGate.getPid() + "]";
                this.setCheckResult("", target, 0);
            }
        }

    }

    /**
     * @param rdLinkForm
     * @param operType
     * @throws Exception
     */
    private void checkRdLinkForm(RdLinkForm rdLinkForm, OperType operType) throws Exception {
        int formOfWay = rdLinkForm.getFormOfWay();
        //风景路线60
        if (rdLinkForm.changedFields().containsKey("formOfWay")) {
            formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
        }
        //非单向道路，不触发检查
        if (formOfWay == 60) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT 1");
            sb.append(" FROM RD_LINK R, RD_LINK_FORM F, RD_GATE G");
            sb.append(" WHERE R.LINK_PID = F.LINK_PID");
            sb.append(" AND F.FORM_OF_WAY = 60");
            sb.append(" AND G.IN_LINK_PID = R.LINK_PID");
            sb.append(" AND G.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
            sb.append(" UNION ALL");
            sb.append(" SELECT 1");
            sb.append(" FROM RD_LINK R, RD_LINK_FORM F, RD_GATE G");
            sb.append(" WHERE R.LINK_PID = F.LINK_PID");
            sb.append(" AND F.FORM_OF_WAY = 60");
            sb.append(" AND G.IN_LINK_PID = " + rdLinkForm.getLinkPid());
            sb.append(" AND G.OUT_LINK_PID = R.LINK_PID");

            String sql = sb.toString();
            log.info("RdLink后检查GLM01570:" + sql);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql);

            if (resultList.size() > 0) {
                this.setCheckResult("", "[RD_LINK," + rdLinkForm.getLinkPid() + "]", 0);
            }
        }
    }

    /**
     * @param rdLink
     * @param operType
     * @throws Exception
     */
    private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
//		for(Map.Entry<String, RdLinkForm> entry:rdLink.formMap.entrySet()){
//        if (entry.getValue().getFormOfWay() == 60) {
        if (rdLink.changedFields().containsKey("forms")) {
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT 1");
            sb.append(" FROM RD_LINK R, RD_LINK_FORM F1, RD_LINK_FORM F2,RD_GATE G");
            sb.append(" WHERE R.LINK_PID = F1.LINK_PID");
            sb.append(" AND F1.FORM_OF_WAY = 60");
            sb.append(" AND G.IN_LINK_PID = R.LINK_PID");
            sb.append(" AND G.OUT_LINK_PID = " + rdLink.getPid());
            sb.append(" AND F2.LINK_PID = " + rdLink.getPid());
            sb.append(" AND F2.FORM_OF_WAY = 60");
            sb.append(" AND F1.U_RECORD <> 2");
            sb.append(" AND F2.U_RECORD <> 2");
            sb.append(" AND R.U_RECORD <> 2");
            sb.append(" AND G.U_RECORD <> 2");
            sb.append(" UNION ALL");
            sb.append(" SELECT 1");
            sb.append(" FROM RD_LINK R, RD_LINK_FORM F1, RD_LINK_FORM F2, RD_GATE G");
            sb.append(" WHERE R.LINK_PID = F1.LINK_PID");
            sb.append(" AND F1.FORM_OF_WAY = 60");
            sb.append(" AND G.IN_LINK_PID = " + rdLink.getPid());
            sb.append(" AND G.OUT_LINK_PID = R.LINK_PID");
            sb.append(" AND F2.LINK_PID = " + rdLink.getPid());
            sb.append(" AND F2.FORM_OF_WAY = 60");
            sb.append(" AND F1.U_RECORD <> 2");
            sb.append(" AND F2.U_RECORD <> 2");
            sb.append(" AND R.U_RECORD <> 2");
            sb.append(" AND G.U_RECORD <> 2");

            String sql = sb.toString();
            log.info("RdLink后检查GLM01570:" + sql);

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> resultList = new ArrayList<Object>();
            resultList = getObj.exeSelect(this.getConn(), sql);

            if (resultList.size() > 0) {
                this.setCheckResult("", "[RD_LINK," + rdLink.getPid() + "]", 0);
            }
        }
//		}
    }

}
