package com.navinfo.dataservice.engine.check.rules;


import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildlink;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.model.utils.CheckGeometryUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * @Title: Check002
 * @Package: com.navinfo.dataservice.engine.check.rules
 * @Description: 不能打断过短的link（2M)
 * @Author: Crayeres
 * @Date: 05/08/17
 * @Version: V1.0
 */
public class Check002 extends baseRule {

    private final static List<ObjType> OBJ_TYPES =
            Arrays.asList(ObjType.LCLINK, ObjType.LULINK, ObjType.CMGBUILDLINK, ObjType.ADLINK, ObjType.ZONELINK, ObjType.RWLINK);

    /**
     * 日志记录
     */
    private Logger logger = Logger.getLogger(Check002.class);

    @Override
    public void preCheck(CheckCommand checkCommand) throws Exception {
        for (IRow row : checkCommand.getGlmList()) {
            if (CheckGeometryUtils.notContains(OBJ_TYPES, row.objType()) || row.status() == ObjStatus.DELETE) {
                continue;
            }

            // 优先获取changedfields内的length
            double tempLength = getLinkLengthAccordingChangedfields(row);
            double linkLength = 0d == tempLength ? getLinkLength(row) : tempLength;

            StringBuffer target = new StringBuffer();
            target.append("[").append(row.tableName().toUpperCase()).append(",").append(row.parentPKValue()).append("]");

            logger.debug(String.format("CHECK002 {target: %s, length: %.2f}", target, linkLength));
            if (linkLength <= 2.0d) {
                this.setCheckResult("不能打断过短的link（2M)", target.toString(), row.parentPKValue());
            }
        }
    }

    private double getLinkLengthAccordingChangedfields(IRow row) {
        double length = 0d;
        if (row.changedFields().containsKey("length")) {
            length = Double.parseDouble(row.changedFields().get("length").toString());
        }
        return length;
    }

    private double getLinkLength(IRow row) {
        double length;
        switch (row.objType()) {
            case LCLINK:
                length = ((LcLink) row).getLength();break;
            case LULINK:
                length = ((LuLink) row).getLength(); break;
            case CMGBUILDLINK:
                length = ((CmgBuildlink) row).getLength(); break;
            case ADLINK:
                length = ((AdLink) row).getLength(); break;
            case ZONELINK:
                length = ((ZoneLink) row).getLength(); break;
            case RWLINK:
                length = ((RwLink) row).getLength(); break;
            default:
                length = 0d;
        }
        return length;
    }

    @Override
    public void postCheck(CheckCommand checkCommand) throws Exception {
    }
}
