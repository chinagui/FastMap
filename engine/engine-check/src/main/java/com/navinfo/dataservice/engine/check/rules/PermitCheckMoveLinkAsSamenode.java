package com.navinfo.dataservice.engine.check.rules;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: PermitCheckMoveLinkAsSamenode
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 如果被分离的NODE做了同一关系，并且此node挂接了至少两根link，则不允许分离节点
 * @Author: Crayeres
 * @Date: 05/10/17
 * @Version: V1.0
 */
public class PermitCheckMoveLinkAsSamenode extends baseRule {

    private Logger logger = Logger.getLogger(PermitCheckMoveLinkAsSamenode.class);

    private final static List<ObjType> OBJ_TYPES = Arrays.asList(ObjType.LULINK, ObjType.ADLINK, ObjType.ZONELINK, ObjType.RWLINK);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (!CheckGeometryUtils.notContains(OBJ_TYPES, row.objType())) {
                continue;
            }

            // 验证sNodePid
            verifySNodePid(row);
            // 验证eNodePid
            verifyENodePid(row);
        }
    }

    /**
     * 验证起点
     * @param row 修形线对象
     * @throws Exception
     */
    private void verifySNodePid(IRow row) throws Exception {
        if (row.changedFields().containsKey("sNodePid")){
            int sNodePid = 0;
            String tableName = "";

            switch (row.objType()) {
                case LULINK: sNodePid = ((LuLink) row).getsNodePid(); tableName = "LU_NODE"; break;
                case ADLINK: sNodePid = ((AdLink) row).getsNodePid(); tableName = "AD_NODE"; break;
                case ZONELINK: sNodePid = ((ZoneLink) row).getsNodePid(); tableName = "ZONE_NODE"; break;
                case RWLINK: sNodePid = ((RwLink) row).getsNodePid(); tableName = "RW_NODE"; break;
                default:
            }
            verifySameNode(tableName, sNodePid);
        }
    }

    /**
     * 验证终点
     * @param row 修形线对象
     * @throws Exception
     */
    private void verifyENodePid(IRow row) throws Exception {
        if(row.changedFields().containsKey("eNodePid")) {
            int eNodePid = 0;
            String tableName = "";

            switch (row.objType()) {
                case LULINK: eNodePid = ((LuLink) row).geteNodePid(); tableName = "LU_NODE"; break;
                case ADLINK: eNodePid = ((AdLink) row).geteNodePid(); tableName = "AD_NODE"; break;
                case ZONELINK: eNodePid = ((ZoneLink) row).geteNodePid(); tableName = "ZONE_NODE"; break;
                case RWLINK: eNodePid = ((RwLink) row).geteNodePid(); tableName = "RW_NODE"; break;
                default:
            }
            verifySameNode(tableName, eNodePid);
        }
    }

    /**
     * 验证分离点是否为同一点, 并且该点挂接线是否超过1条
     * @param tableName
     * @param nodePid
     * @throws Exception
     */
    private void verifySameNode(String tableName, int nodePid) throws Exception {
        if (StringUtils.isEmpty(tableName) || 0 == nodePid) {
            return;
        }

        // 判断是否为同一点对象
        RdSameNodeSelector selector = new RdSameNodeSelector(getConn());
        RdSameNodePart part = selector.loadByNodePidAndTableName(nodePid, tableName, false);
        if (null == part) {
            return;
        }

        int size;
        switch (ReflectionAttrUtils.getObjTypeByTableName(tableName)) {
            case LUNODE: size = countLuNode(nodePid); break;
            case ADNODE: size = countAdNode(nodePid); break;
            case ZONENODE: size = countZoneNode(nodePid); break;
            case RWNODE: size = countRwNode(nodePid); break;
            default: size = 0;
        }

        if (size >= 2) {
            setCheckResult("", String.format("[%s,%d]", tableName.toUpperCase(), nodePid), 0);
        }
    }

    /**
     * 计算LuNode挂接线数量
     * @param nodePid LuNode主键
     * @return 挂接线数量
     * @throws Exception
     */
    private int countLuNode(int nodePid) throws Exception {
        LuLinkSelector selector = new LuLinkSelector(getConn());
        return selector.loadByNodePid(nodePid, false).size();
    }

    /**
     * 计算AdNode挂接线数量
     * @param nodePid AdNode主键
     * @return 挂接线数量
     * @throws Exception
     */
    private int countAdNode(int nodePid) throws Exception {
        AdLinkSelector selector = new AdLinkSelector(getConn());
        return selector.loadByNodePid(nodePid, false).size();
    }

    /**
     * 计算ZoneNode挂接线数量
     * @param nodePid ZoneNode主键
     * @return 挂接线数量
     * @throws Exception
     */
    private int countZoneNode(int nodePid) throws Exception {
        ZoneLinkSelector selector = new ZoneLinkSelector(getConn());
        return selector.loadByNodePid(nodePid, false).size();
    }

    /**
     * 计算RwNode挂接线数量
     * @param nodePid RwNode主键
     * @return 挂接线数量
     * @throws Exception
     */
    private int countRwNode(int nodePid) throws Exception {
        RwLinkSelector selector = new RwLinkSelector(getConn());
        return selector.loadByNodePid(nodePid, false).size();
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {

    }
}
