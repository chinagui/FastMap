package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: PermitCheckLinkSnapSamelink
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 此link不是该组同一关系中的主要素，不能进行修形操作
 * @Author: Crayeres
 * @Date: 05/09/17
 * @Version: V1.0
 */
public class PermitCheckLinkSnapSamelink extends baseRule {

    private final static List<ObjType> OBJ_TYPES = Arrays.asList(ObjType.LULINK, ObjType.ADLINK, ObjType.ZONELINK, ObjType.RWLINK);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(PermitCheckLinkSnapSamelink.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType()) ) {
                continue;
            }

            switch (row.objType()) {
                case LULINK:
                case ADLINK:
                case ZONELINK:
                case RWLINK: verifySameLink(checkCommand, row);
                    break;
                default:
            }


        }
    }

    /**
     * 验证同一线主对象是否修改
     * @param checkCommand result结果集
     * @param row 修改对象
     * @throws Exception
     */
    private void verifySameLink(CheckCommand checkCommand, IRow row) throws Exception {
        RdSameLinkSelector selector = new RdSameLinkSelector(getConn());
        RdSameLinkPart sameLinkPart = selector.loadLinkPartByLink(row.parentPKValue(), row.tableName().toUpperCase(), false);

        if (null == sameLinkPart) {
            return;
        }

        RdSameLink sameLink = (RdSameLink) selector.loadById(sameLinkPart.getGroupId(), false);

        logger.debug(String.format("PermitCheckLinkSnapSamelink {ObjType: %s, Pid: %d}", row.objType(), row.parentPKValue()));

        sameLinkPart = CheckGeometryUtils.getMainLink(sameLink.getParts(), getConn());
        if (sameLinkPart.getTableName().toUpperCase().equals(row.tableName().toUpperCase())
                && sameLinkPart.getLinkPid() == row.parentPKValue()) {
            return;
        }

        boolean flag = false;
        for (IRow r : checkCommand.getGlmList()) {
            if (r.tableName().toUpperCase().equals(row.tableName().toUpperCase())
                    && sameLinkPart.getLinkPid() == row.parentPKValue()) {
                flag = true;
            }
        }
        if (!flag) {
            setCheckResult("", String.format("[%s,%d]", row.tableName().toUpperCase(), row.parentPKValue()), 0);
        }
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
