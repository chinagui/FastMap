package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 查中文地址（fullname）
 * 1.地址中存在：零号~九号
 * 2.地址中存在：零弄~九弄
 * 3.地址中存在：至
 * 4.地址中存在：——
 * 5.地址中存在：\
 * 6.地址中存在：、
 * 7.地址中存在双空格或多空格
 * 8.地址中存在：＃
 * 9.地址中存在：F结尾
 * 10.地址中存在：ＮＯ.（不区分大小写）
 * 11.地址中存在：一层~九层
 * 12.地址中存在：一楼~九楼
 * 13.地址中存在：零栋~九栋
 * 存在以上任何一种情况均报LOG：PID=**地址格式错误
 * PS：上述字符均为全角
 * Created by ly on 2017/7/7.
 */
public class FMYW20135 extends BasicCheckRule {
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

        for (IxPoiAddress address : addresses) {

            String langCode = address.getLangCode() == null ? "" : address.getLangCode();

            if (!langCode.equals("CHI") && !langCode.equals("CHT")) {

                continue;
            }

            String fullName = address.getFullname() == null ? "" : address.getFullname();

            if (matcter(fullName)) {

                IxPoi poi = (IxPoi) poiObj.getMainrow();

                String strLog = "PID=" + poi.getPid() + "地址格式错误";

                setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);

                return;
            }
        }
    }

    private boolean matcter(String strContent) {

        //1、2、13
        Pattern p1 = Pattern.compile(".*[零一二三四五六七八九]+[号弄栋]+.*");
        Matcher m1 = p1.matcher(strContent);

        // 3、4、5、6、7、8
        Pattern p2 = Pattern.compile(".*(至|——|、|＼|　　|＃)+.*");
        Matcher m2 = p2.matcher(strContent);

        //9
        Pattern p3 = Pattern.compile(".*ｆ$");
        Matcher m3 = p3.matcher(strContent);

        // 10
        Pattern p4 = Pattern.compile(".*(Ｎｏ|Ｎ０|ｎｏ|ｎＯ)+．+.*");
        Matcher m4 = p4.matcher(strContent);

        //11、12
        Pattern p5 = Pattern.compile(".*[一二三四五六七八九]+[层楼]+.*");
        Matcher m5 = p5.matcher(strContent);

        return (m1.matches() || m2.matches() || m3.matches() || m4.matches() || m5.matches());
    }


    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
