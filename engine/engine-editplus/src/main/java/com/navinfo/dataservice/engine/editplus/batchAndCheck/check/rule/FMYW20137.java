package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 同一PID，其官方标准化中文名称与别名标准化中文相同，则报LOG：PID=**名称与别名重复
 * Created by ly on 2017/7/7.
 */
public class FMYW20137 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        Set<String> standardName = new HashSet<>();

        Set<String> aliasName = new HashSet<>();

        getNames(poiObj, standardName, aliasName);

        if (standardName.size() == 0 || aliasName.size() == 0) {

            return;
        }

        standardName.retainAll(aliasName);

        if (standardName.size() > 0) {

            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String strLog = "PID=" + poi.getPid() + "名称与别名重复";

            setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);
        }
    }

    /**
     * 获取 官方标准化中文名称与别名标准化中文
     */
    public void getNames(IxPoiObj poiObj, Set<String> standardName, Set<String> aliasName) {

        standardName.clear();

        aliasName.clear();

        List<IxPoiName> names = poiObj.getIxPoiNames();

        if (names == null || names.size() == 0) {

            return;
        }

        for (IxPoiName name : names) {

            String langCode = name.getLangCode() == null ? "" : name.getLangCode();

            if (!langCode.equals("CHI") && !langCode.equals("CHT")) {

                continue;
            }

            if (name.getNameType() != 1 ) {

                continue;
            }

            if (name.getNameClass() == 1) {

                standardName.add(name.getName() == null ? "" : name.getName());

            } else if (name.getNameClass() == 3) {

                aliasName.add(name.getName() == null ? "" : name.getName());
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
