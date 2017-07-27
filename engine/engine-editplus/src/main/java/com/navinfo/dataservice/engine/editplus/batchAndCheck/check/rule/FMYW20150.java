package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import net.sf.json.JSONObject;

import java.util.*;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 中文地址fullname字段（langCode="CHI")不能有繁体字。查找的繁体字在TY_CHARACTER_FJT_HZ中所在行的CONVERT字段的值：
 * 1、如果是2表示必须转化，报log：**是繁体字，对应的简体字是**，必须转化
 * Created by ly on 2017/7/7.
 */
public class FMYW20150 extends BasicCheckRule {
    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        List<IxPoiAddress> addresses = poiObj.getIxPoiAddresses();

        if (addresses == null || addresses.size() == 0) {

            return;
        }

        MetadataApi metadataApi = (MetadataApi) ApplicationContextUtil.getBean("metadataApi");

        Map<String, JSONObject> ft = metadataApi.tyCharacterFjtHzCheckSelectorGetFtExtentionTypeMap();

        Map<String, JSONObject> fts=new HashMap<>();

        for (String strFt : ft.keySet()) {

            if (strFt.length() > 1) {

                fts.put(strFt, ft.get(strFt));
            }
        }

        Set<Character> allChars = new HashSet<>();

        Set<String> fullNames=new HashSet<>();

        for (IxPoiAddress address : addresses) {

            String langCode=address.getLangCode()==null?"":address.getLangCode();

            if (!langCode.equals("CHI") || address.getFullname().isEmpty()) {

                continue;
            }

            fullNames.add(address.getFullname());

            char[] chars = address.getFullname().toCharArray();

            for (int i = 0; i < chars.length; i++) {

                allChars.add(chars[i]);
            }
        }

        for (char item : allChars) {

            String strValue = String.valueOf(item);

            if (!ft.containsKey(strValue)) {

                continue;
            }

            JSONObject data = ft.get(strValue);

            setCheckResult( data , poiObj , strValue);
        }

        for (String fullName : fullNames) {

            for (String strFt : fts.keySet()) {

                if (!fullName.contains(strFt)) {

                    continue;
                }
                
                JSONObject data = fts.get(strFt);

                setCheckResult( data , poiObj , strFt);
            }
        }
    }

    private void setCheckResult(JSONObject data ,IxPoiObj poiObj ,String strFt)throws Exception
    {
        Object convert = data.get("convert");

        if (convert.equals(2)) {

            IxPoi poi = (IxPoi) poiObj.getMainrow();

            String jt = (String) data.get("jt");

            String log = "“" + strFt + "”是繁体字，对应的简体字是“" + jt + "”，必须转化";

            setCheckResult(poi.getGeometry(), "[IX_POI," + poi.getPid() + "]", poi.getMeshId(), log);
        }
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
