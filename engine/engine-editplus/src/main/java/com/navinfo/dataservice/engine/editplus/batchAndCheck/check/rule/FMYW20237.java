package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @Title: FMYW20237
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查条件：非删除POI对象；
 * 检查原则：分类为机场(kindCode=230126)、机场出发到达(230127)、客运火车站(230103)的poi数据的引导坐标不能建立在内部属性道路上(link.formOfway=52)。
 * 不满足以上检查原则报log：引导坐标不能建立在内部属性道路上，请重新关联道路！
 * @Author: Crayeres
 * @Date: 8/8/2017
 * @Version: V1.0
 */
public class FMYW20237 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }

    public void run() throws Exception {
        List<String> kindCodes = Arrays.asList(new String[]{"230126", "230127", "230103"});

        List<Long> pids = new ArrayList<>();
        for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
            BasicObj basicObj = entry.getValue();

            if (!basicObj.objName().equals(ObjectName.IX_POI)) {
                continue;
            }

            IxPoiObj poiObj = (IxPoiObj) basicObj;
            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String kindCode = poi.getKindCode();
            if (StringUtils.isEmpty(kindCode) || !kindCodes.contains(kindCode)) {
                continue;
            }

            pids.add(entry.getKey());
        }

        if (CollectionUtils.isNotEmpty(pids)) {
            String pidStr = pids.toString().replace("[", "").replace("]", "");

            Connection conn = getCheckRuleCommand().getConn();


            List<Clob> values = new ArrayList<>();
            String pidString;
            if (pids.size() > 1000) {
                Clob clob = ConnectionUtil.createClob(conn);
                clob.setString(1, pidStr);
                pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
                values.add(clob);
            } else {
                pidString = " PID IN (" + pidStr + ")";
            }

            String sql = "SELECT IP.PID" + "  FROM IX_POI IP, RD_LINK RL, RD_LINK_FORM RLF" + " WHERE IP." + pidString + "   AND IP" +
                    ".LINK_PID = RL.LINK_PID" + "   AND RL.LINK_PID = RLF.LINK_PID" + "   AND RLF.FORM_OF_WAY = 52";

            PreparedStatement pstmt = null;
            ResultSet rs = null;
            try {
                pstmt = conn.prepareStatement(sql);
                if (CollectionUtils.isNotEmpty(values)) {
                    for (int i = 0; i < values.size(); i++) {
                        pstmt.setClob(i + 1, values.get(i));
                    }
                }

                //去重用，若targets重复（不判断顺序，只要pid相同即可），则不重复报。否则报出
                Set<String> filterPid = new HashSet<>();

                rs = pstmt.executeQuery();
                while (rs.next()) {
                    Long pid = rs.getLong("PID");
                    String targets = "[IX_POI," + pid + "]";
                    if (!filterPid.contains(targets)) {
                        setCheckResult("", targets, 0);
                    }
                    filterPid.add(targets);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                DBUtils.closeResultSet(rs);
                DBUtils.closeStatement(pstmt);
            }
        }
    }
}
