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
 * 1、官方标准化名称大于35个字符（不区分全半角，只计算字符个数，汉字也属于1个字符），需要报出。
 * 2、一汽大众品牌（品牌代码为“403C”）名称字段要求为24个字符以内
 * 3、宾利品牌（品牌代码为 4003）名称字段要求为24个字符以内
 * 4、一汽丰田品牌（品牌代码为404B）名称字段要求为11个字符以内
 * 5、日产天籁（品牌代码为4024）名称字段要求为16个字符以内
 * 6、日产英菲尼迪（品牌代码为4096）名称字段要求为16个字符以内
 * *7、名称长度检查：广汽丰田（品牌代码为404A）名称字段要求为11个字符以内；
 * 注：XX以内，均是小于等于XX，大于XX则报出log；
 * 满足以上报LOG：PID=**官方标准名称超长
 * Created by ly on 2017/7/7.
 */
public class FMYW20133 extends BasicCheckRule {
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

            case "403C":
            case "4003":
                limitCount = 24;
                break;

            case "4024":
            case "4096":
                limitCount = 16;
                break;

            case "404A":
            case "404B":
                limitCount = 11;
                break;

            default:
                limitCount = 35;
                break;
        }

        List<IxPoiName> names = poiObj.getIxPoiNames();

        if (names == null || names.size() == 0) {

            return;
        }

        for (IxPoiName br : names) {

            if (br.getNameClass() != 1 || br.getNameType() != 1) {

                continue;
            }

            if (!br.getLangCode().equals("CHI") && !br.getLangCode().equals("CHT")) {
                continue;
            }

            if (br.getName() != null && br.getName().length() > limitCount) {

                String strLog = "PID=" + poi.getPid() + "官方标准名称超长";

                setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);

                break;
            }
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
