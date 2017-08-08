package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiGasstation;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Title: FMYW20267
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description: 检查条件：
                    非删除POI对象；
                    检查原则：
                    加油站ix_poi_gasStation.open_hour非空非法字符检查，判断条件：如果加油站ix_poi_gasStation.open_hour在
                    TY_CHARACTER_EGALCHAR_EXT.EXTENTION_TYPE in (“GBK”,“ENG_F_U”,“ENG_F_L”,“DIGIT_F”,“SYMBOL_F”)
                    对应的“CHARACTER”范围内，则不报log，否则报log：**是非法字符！
 * @Author: Crayeres
 * @Date: 8/8/2017
 * @Version: V1.0
 */
public class FMYW20264 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {

    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }

    public void run() throws Exception {
        MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");
        Map<String, List<String>> map = metadataApi.tyCharacterEgalcharExtGetExtentionTypeMap();

        List<String> types = Arrays.asList(new String[]{"GBK", "ENG_F_U", "ENG_F_L", "DIGIT_F", "SYMBOL_F"});

        label1:
        for (Map.Entry<Long, BasicObj> entryRow : getRowList().entrySet()) {
            BasicObj basicObj = entryRow.getValue();
            if (!basicObj.objName().equals(ObjectName.IX_POI)) {
                continue;
            }

            IxPoiObj poiObj = (IxPoiObj) basicObj;
            List<IxPoiGasstation> gasstations = poiObj.getIxPoiGasstations();

            for (IxPoiGasstation gasstation : gasstations) {
                if (StringUtils.isEmpty(gasstation.getOpenHour())) {
                    continue;
                }

                label2:
                for (Character character : gasstation.getOpenHour().toCharArray()) {
                    String str = String.valueOf(character);

                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        String key = entry.getKey();

                        if (!types.contains(key)) {
                            continue;
                        }

                        if (entry.getValue().contains(str)) {
                            continue label2;
                        }
                    }

                    setCheckResult("", String.format("[IX_POI,%d]", entryRow.getKey()), 0, String.format("%s是非法字符!", str));
                    continue label1;
                }
            }
        }
    }
}
