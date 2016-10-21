package com.navinfo.dataservice.engine.edit.zhangyuntao.rd;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import net.sf.json.JSONObject;
import org.junit.Test;

/**
 * Created by chaixin on 2016/9/26 0026.
 */
public class IxPoiTest extends InitApplication{


    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void creat(){
        String parameter = "{\"command\":\"CREATE\",\"type\":\"IXPOI\",\"dbId\":19,\"data\":{\"longitude\":116.47318840026855,\"latitude\":40.01432055968962,\"x_guide\":116.47318840026855,\"y_guide\":40.01422195512273,\"linkPid\":204000506,\"name\":\"测试\",\"kindCode\":\"230227\"}}";
        parameter = "{\"command\":\"CREATE\",\"dbId\":17,\"type\":\"IXSAMEPOI\",\"poiPids\":[202000044,210000024]}";
        TestUtil.run(parameter);
    }

    @Test
    public void test() throws Exception {
        EditApi api = (EditApi) ApplicationContextUtil.getBean("editApi");
//        JSONObject obj = JSONObject.fromObject("{ \"dbId\": \"17\", \"objId\": 201000015, \"command\": \"CREATE\", \"type\": \"IXPOIUPLOAD\",\"data\": { \"accessFlag\": 0, \"addressFlag\": 0, \"addresses\": [], \"adminReal\": 0,\"advertisements\": [], \"airportCode\": \"\", \"attractions\": [], \"audioes\": [],\"buildings\": [], \"businesstimes\": [], \"carrentals\": [], \"chain\": \"\",\"chargingplotPhs\": [], \"chargingplots\": [], \"chargingstations\": [], \"children\":[], \"collectTime\": \"20160926170246\", \"contacts\": [], \"dataVersion\": \"260+\",\"details\": [], \"difGroupid\": \"\", \"editFlag\": 1, \"editionFlag\": \"\",\"entryImages\": [], \"events\": [], \"exPriority\": \"\", \"fieldState\": \"改种别代码\",\"fieldTaskId\": 0, \"flags\": [], \"fullAttrFlag\": 9, \"gasstations\": [ { \"egType\":\"null\", \"fuelType\": \"0\", \"memo\": \"\", \"mgType\": \"null\", \"oilType\": \"null\", \"openHour\":\"null\", \"payment\": \"null\", \"photoName\": \"\", \"pid\": 320000001, \"poiPid\": 201000015,\"rowId\": \"86c72d6f3528434c848daa37b642ff86\", \"service\": \"null\", \"serviceProv\": \"\",\"uDate\": \"\", \"uRecord\": 0 } ], \"geoAdjustFlag\": 9, \"geometry\": { \"type\":\"Point\", \"coordinates\": [ 116.59975, 40.21171 ] }, \"hotels\": [], \"icons\": [],\"importance\": 0, \"indoor\": 0, \"introductions\": [], \"kindCode\": \"230215\",\"label\": \"\", \"level\": \"B1\", \"linkPid\": 1001713, \"log\":\"改名称|改分类|改POI_LEVEL|改RELATION\", \"meshId\": 605624, \"meshId5k\": \"\",\"nameGroupid\": 0, \"names\": [ { \"keywords\": \"\", \"langCode\": \"CHI\", \"name\":\"加油站\", \"nameClass\": 1, \"nameFlags\": [], \"nameGroupid\": 1, \"namePhonetic\": \"\",\"nameTones\": [], \"nameType\": 2, \"nidbPid\": \"\", \"pid\": 202000013, \"poiPid\":201000015, \"rowId\": \"cd3034394ef246e9814c25e1f5610c47\", \"uDate\": \"\", \"uRecord\":0 } ], \"oldAddress\": \"\", \"oldBlockcode\": \"\", \"oldKind\": \"230215\", \"oldName\":\"加油站\", \"oldXGuide\": 0, \"oldYGuide\": 0, \"open24h\": 2, \"operateRefs\": [],\"parents\": [], \"parkings\": [], \"photos\": [ { \"fccPid\":\"6e3f82a9258b4917808d567f32f7a5af\", \"memo\": \"\", \"photoId\": 0, \"poiPid\":201000015, \"rowId\": \"6e3f82a9258b4917808d567f32f7a5af\", \"status\": \"\", \"tag\": 3,\"uDate\": \"\", \"uRecord\": 0 } ], \"pid\": 201000015, \"pmeshId\": 0, \"poiMemo\": \"\",\"poiNum\": \"00365520160926165840\", \"postCode\": \"\", \"regionId\": 0, \"reserved\": \"\",\"restaurants\": [], \"roadFlag\": 0, \"rowId\": \"BBDA1FECFA9C4A68ACC4B7AD2A17F27E\",\"samepoiParts\": [], \"side\": 0, \"sportsVenue\": \"\", \"state\": 0, \"status\": 0,\"taskId\": 0, \"tourroutes\": [], \"type\": 0, \"uDate\": \"\", \"uRecord\": 1,\"verifiedFlag\": 9, \"videoes\": [], \"vipFlag\": \"\", \"xGuide\": 116.59952, \"yGuide\":40.21163 } }");
        JSONObject obj = JSONObject.fromObject("{\"data\": { \"accessFlag\": 0, \"addressFlag\": 0, \"addresses\": [], \"adminReal\": 0,\"advertisements\": [], \"airportCode\": \"\", \"attractions\": [], \"audioes\": [],\"buildings\": [], \"businesstimes\": [], \"carrentals\": [], \"chain\": \"\",\"chargingplotPhs\": [], \"chargingplots\": [], \"chargingstations\": [], \"children\":[], \"collectTime\": \"20160926170246\", \"contacts\": [], \"dataVersion\": \"260+\",\"details\": [], \"difGroupid\": \"\", \"editFlag\": 1, \"editionFlag\": \"\",\"entryImages\": [], \"events\": [], \"exPriority\": \"\", \"fieldState\": \"改种别代码\",\"fieldTaskId\": 0, \"flags\": [], \"fullAttrFlag\": 9, \"gasstations\": [ { \"egType\":\"\", \"fuelType\": \"0\", \"memo\": \"\", \"mgType\": \"\", \"oilType\": \"\", \"openHour\":\"\", \"payment\": \"\", \"photoName\": \"\", \"pid\": 320000001, \"poiPid\": 201000015,\"rowId\": \"86c72d6f3528434c848daa37b642ff86\", \"service\": \"\", \"serviceProv\": \"\",\"uDate\": \"\", \"uRecord\": 0 } ], \"geoAdjustFlag\": 9, \"geometry\": { \"type\":\"Point\", \"coordinates\": [ 116.59975, 40.21171 ] }, \"hotels\": [], \"icons\": [],\"importance\": 0, \"indoor\": 0, \"introductions\": [], \"kindCode\": \"230215\",\"label\": \"\", \"level\": \"B1\", \"linkPid\": 1001713, \"log\":\"改名称|改分类|改POI_LEVEL|改RELATION\", \"meshId\": 605624, \"meshId5k\": \"\",\"nameGroupid\": 0, \"names\": [ { \"keywords\": \"\", \"langCode\": \"CHI\", \"name\":\"加油站\", \"nameClass\": 1, \"nameFlags\": [], \"nameGroupid\": 1, \"namePhonetic\": \"\",\"nameTones\": [], \"nameType\": 2, \"nidbPid\": \"\", \"pid\": 202000013, \"poiPid\":201000015, \"rowId\": \"cd3034394ef246e9814c25e1f5610c47\", \"uDate\": \"\", \"uRecord\":0 } ], \"oldAddress\": \"\", \"oldBlockcode\": \"\", \"oldKind\": \"230215\", \"oldName\":\"加油站\", \"oldXGuide\": 0, \"oldYGuide\": 0, \"open24h\": 2, \"operateRefs\": [],\"parents\": [], \"parkings\": [], \"photos\": [ { \"fccPid\":\"6e3f82a9258b4917808d567f32f7a5af\", \"memo\": \"\", \"photoId\": 0, \"poiPid\":201000015, \"rowId\": \"6e3f82a9258b4917808d567f32f7a5af\", \"status\": \"\", \"tag\": 3,\"uDate\": \"\", \"uRecord\": 0 } ], \"pid\": 201000015, \"pmeshId\": 0, \"poiMemo\": \"\",\"poiNum\": \"00365520160926165840\", \"postCode\": \"\", \"regionId\": 0, \"reserved\": \"\",\"restaurants\": [], \"roadFlag\": 0, \"rowId\": \"BBDA1FECFA9C4A68ACC4B7AD2A17F27E\",\"samepoiParts\": [], \"side\": 0, \"sportsVenue\": \"\", \"state\": 0, \"status\": 0,\"taskId\": 0, \"tourroutes\": [], \"type\": 0, \"uDate\": \"\", \"uRecord\": 1,\"verifiedFlag\": 9, \"videoes\": [], \"vipFlag\": \"\", \"xGuide\": 116.59952, \"yGuide\":40.21163 } }");
        // JSONObject obj = JSONObject.fromObject("{'pid':1,'pids':2}");
        api.run(obj);
    }

}
