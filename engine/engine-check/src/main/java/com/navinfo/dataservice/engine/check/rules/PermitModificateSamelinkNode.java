package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: PermitModificateSamelinkNode
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: node不是该组同一关系中的主要素，不能进行移动操作
 * @Author: Crayeres
 * @Date: 05/10/17
 * @Version: V1.0
 */
public class PermitModificateSamelinkNode extends baseRule {

    private final static List<ObjType> OBJ_TYPES = Arrays.asList(ObjType.LUNODE, ObjType.ADNODE,  ObjType.ZONENODE, ObjType.RWNODE);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(PermitModificateSamelinkNode.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType()) ) {
                continue;
            }

            // 检查同一点
            verifySameNode(checkCommand, row);
        }
    }

    /**
     * 验证同一点主对象是否修改
     * @param checkCommand result结果集
     * @param row 修改对象
     * @throws Exception
     */
    private void verifySameNode(CheckCommand checkCommand, IRow row) throws Exception {
        RdSameNodeSelector selector = new RdSameNodeSelector(getConn());
        List<RdSameNode> sameNodes = selector.loadSameNodeByNodePids(
                String.valueOf(row.parentPKValue()), row.tableName().toUpperCase(), false);

        logger.debug(String.format("PermitCheckLinkSnapSamelink {ObjType: %s, Pid: %d}", row.objType(), row.parentPKValue()));

        for (RdSameNode sameNode : sameNodes) {
            boolean flag = false;
            List<RdSameNodePart> sameNodeParts = CheckGeometryUtils.getMainNode(sameNode.getParts(), getConn());
            for (RdSameNodePart part : sameNodeParts) {
                if (part.getTableName().toUpperCase().equals(row.tableName().toUpperCase()) && part.getNodePid() == row.parentPKValue()) {
                    flag = true;
                }
            }
            if (flag) {
                return;
            }

            for (IRow r : checkCommand.getGlmList()) {
                for (RdSameNodePart part : sameNodeParts) {
                    if (r.tableName().toUpperCase().equals(row.tableName().toUpperCase()) && row.parentPKValue() == part.getNodePid()) {
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag) {
                setCheckResult("node不是该组同一关系中的主要素，不能进行移动操作",
                        String.format("[%s,%d]", row.tableName(), row.parentPKValue()), 0);
            }
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
