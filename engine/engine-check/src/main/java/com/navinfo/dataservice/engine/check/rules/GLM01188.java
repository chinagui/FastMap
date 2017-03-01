package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.commons.util.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperatorResultWithGeo;

/**
 * GLM01188
 *
 * @author Zhang Xiaolong
 * @ClassName: GLM01188
 * @date 2017年2月6日 上午11:30:45
 * @Description: 一个Node上挂接的特殊交通类型link数大于2时，报log1
 */

public class GLM01188 extends baseRule {

    private static Logger logger = Logger.getLogger(GLM01188.class);

    private Set<Integer> checkNodeList = new HashSet<>();

    public GLM01188() {
    }

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
        prepareData(checkCommand);
        for (int nodePid : checkNodeList) {

            logger.info("GLM01188:检查类型：postCheck， 检查规则：GLM01188， 检查要素：RDNODE(" + nodePid + ")");
            StringBuilder sb = new StringBuilder();

            sb.append("SELECT L.link_pid FROM RD_LINK L WHERE (L.E_NODE_PID = " + nodePid + " OR L.S_NODE_PID = " +
                    nodePid + ") AND L.SPECIAL_TRAFFIC=1 GROUP BY L.link_pid");

            DatabaseOperator getObj = new DatabaseOperator();
            List<Object> linkPidList = new ArrayList<Object>();
            linkPidList = getObj.exeSelect(this.getConn(), sb.toString());

            DatabaseOperatorResultWithGeo doR = new DatabaseOperatorResultWithGeo();

            if (linkPidList.size() > 2) {
                String targets = "";
                for (Object obj : linkPidList) {
                    int linkPid = Integer.parseInt(String.valueOf(obj));
                    if (StringUtils.isNotEmpty(targets))
                        targets += ";";
                    targets += "[RD_LINK," + linkPid + "]";
                }

                logger.info("GLM01188:[targets=" + targets + "]");
                for (Object obj : linkPidList) {
                    int linkPid = Integer.parseInt(String.valueOf(obj));

                    StringBuilder resultSb = new StringBuilder();

                    resultSb.append("SELECT RL.GEOMETRY, '[RD_LINK,' || RL.LINK_PID || ']' TARGET, RL.MESH_ID  FROM "
                            + "RD_LINK RL where rl.link_pid=");

                    resultSb.append(linkPid);

                    log.info("RdLink后检查GLM01188 SQL:" + resultSb.toString());

                    List<Object> resultList = new ArrayList<Object>();

                    resultList = doR.exeSelect(this.getConn(), resultSb.toString());

                    if (!resultList.isEmpty()) {
                        this.setCheckResult(resultList.get(0).toString(), targets, (int) resultList.get(2));
                    }
                }
            }
        }
    }

    /**
     * @param checkCommand
     * @throws Exception
     */
    private void prepareData(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (row instanceof RdLink) {
                RdLink link = (RdLink) row;
                if (link.status() != ObjStatus.DELETE) {
                    int specialTraffic = link.getSpecialTraffic();
                    if (link.changedFields().containsKey("specialTraffic")) {
                        specialTraffic = (int) link.changedFields().get("specialTraffic");
                    }
                    //特殊交通类型的link
                    if (specialTraffic == 1) {
                        checkNodeList.add(link.getsNodePid());

                        checkNodeList.add(link.geteNodePid());
                    }
                }
            }
        }
    }
}
