package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.alibaba.druid.sql.visitor.functions.If;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 表内代理店等级全部为A，否则报LOG：PID=**等级标识错误
 * 表内代理店判断原则：读取SC_POINT_SPEC_KINDCODE_NEW表TYPE =7类型，通过分类和品牌匹配
 * Created by ly on 2017/7/7.
 */
public class FMYW20136 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        IxPoi poi = (IxPoi) poiObj.getMainrow();

        if (poi.getLevel() != null && poi.getLevel().equals("A")) {

            return;
        }

        MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        Map<String, List<String>> mapKindChain = metadataApi.scPointSpecKindCodeType7();

        String kindCode = poi.getKindCode() == null ? "" : poi.getKindCode();

        if (!mapKindChain.containsKey(kindCode)) {

            return;
        }

        String chain =  poi.getChain() == null ? "" : poi.getChain();

        if (mapKindChain.get(kindCode).contains(chain)) {

            String strLog = "PID=" + poi.getPid() + "等级标识错误";

            setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
