package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.List;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 1、‘中文地址’地址长度要求大于50字符（需要报出
 * LOG：PID=**地址超长
 * 2、一汽丰田品牌（品牌代码为404B）地址长度要求为16字符以内
 * 3、日产天籁品牌（品牌代码为4024）地址长度要求为24字符以内
 * 4、日产英菲尼迪品牌（品牌代码为4096）地址长度要求为24字符以内
 * 5、广汽丰田（品牌代码为404A）地址长度要求为16个字符以内；
 * 6、雷克萨斯（品牌代码为400C）地址长度要求为16个字符以内
 * 注：XX以内，均是小于等于XX，大于XX则报出log；
 * 满足以上报LOG：PID=**地址超长
 * Created by ly on 2017/7/7.
 */
public class FMYW20134 extends BasicCheckRule {

    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        IxPoi poi = (IxPoi) poiObj.getMainrow();

        String chain = poi.getChain() == null ? "" : poi.getChain();

        int limitCount = Integer.MAX_VALUE;

        switch (chain) {

            case "4024":
            case "4096":
                limitCount = 24;
                break;

            case "404B":
            case "404A":
            case "400C":
                limitCount = 16;
                break;

            default:
                limitCount = 50;
                break;
        }

        List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();

        if (addresses == null || addresses.size() == 0) {

            return;
        }

        for (IxPoiAddress addresse : addresses) {

            String langCode = addresse.getLangCode() == null ? "" : addresse.getLangCode();

            if (!langCode.equals("CHI") && !langCode.equals("CHT")) {

                continue;
            }

            if (addresse.getFullname() != null && addresse.getFullname().length() > limitCount) {

                String strLog = "PID=" + poi.getPid() + "地址超长";

                setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);

                return;
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
