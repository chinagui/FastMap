package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
import com.navinfo.dataservice.engine.meta.translates.TranslateDictData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Title: TranslateTest
 * @Package: org.navinfo.dataservice.engine.meta
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 6/6/2017
 * @Version: V1.0
 */
public class TranslateTest {

    @Before
    public void before() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "dubbo-consumer-datahub-test.xml" });
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }

    @Test
    public void translateTest() {
        EnglishConvert convert = new EnglishConvert();
        System.out.println(convert.convert("名￥著的是"));
        System.out.println(convert.convert("ＪＯ（Ｅ郞 ＪＯＥ＇Ｓ　ＳＡＬＯＮ"));
        System.out.println(convert.convert("ＪＯＥ）郞 ＪＯＥ,Ｓ　ＳＡＬＯＮ"));
        System.out.println(convert.convert("ＪＯＥ×郞 ＪＯＥ，Ｓ　ＳＡＬＯＮ"));
        System.out.println(convert.convert("ＪＯＥ*郞 ＪＯＥ，Ｓ　ＳＡＬＯＮ"));
        System.out.println(convert.convert("７１１便利店０３３｜９℃店"));
        System.out.println(convert.convert("null"));
        System.out.println(convert.convert("%"));
        System.out.println(convert.convert("$"));
        System.out.println(convert.convert(" "));
        System.out.println(convert.convert(null));
        System.out.println(convert.convert("陕西2国道10"));
        System.out.println(convert.convert("国道210"));
        System.out.println(convert.convert("陕西国道210"));
        System.out.println(convert.convert("国210道"));
        System.out.println(convert.convert("210省道"));
        System.out.println(convert.convert("陕西210省道"));
        System.out.println(convert.convert("陕西2省道10"));
        System.out.println(convert.convert("省道210"));
        System.out.println(convert.convert("陕西省道210"));
        System.out.println(convert.convert("省210道"));
        System.out.println(convert.convert("210县道"));
        System.out.println(convert.convert("陕西210县道"));
        System.out.println(convert.convert("陕西2县道10"));
        System.out.println(convert.convert("县道210"));
        System.out.println(convert.convert("陕西县道210"));
        System.out.println(convert.convert("县210道"));
        System.out.println(convert.convert("７１１便利店０３３｜９℃店|１＋１＝２童装|便利店ｎＯ．３"));
        System.out.println(convert.convert("三零六医院"));
        System.out.println(convert.convert("的三零六医院"));
        System.out.println(convert.convert("306医院"));
        System.out.println(convert.convert("锦业二路与丈北路字第三零六医院"));
        System.out.println(convert.convert("锦业二路与丈北路字三零六医院"));
        System.out.println(convert.convert("锦业二路与丈北路三零六医院"));
        System.out.println(convert.convert("锦业二路与丈北路十字第三号楼"));
        System.out.println(convert.convert("锦业二路与丈北路十字第三号楼"));
        System.out.println(convert.convert("锦业二路与丈北路十字第3号楼"));
        System.out.println(convert.convert("锦业二路与丈北路十字3号"));
        System.out.println(convert.convert("锦业二路与丈北路十字3号房"));
        System.out.println(convert.convert("锦业Ⅱ路与丈北路Ⅹ字"));
        System.out.println(convert.convert("锦业二路与丈北路十字甲3号楼"));
        System.out.println(convert.convert("锦业二路与丈北路十字甲3"));
        System.out.println(convert.convert("锦业二路与丈北路十字3号"));
        System.out.println(convert.convert("锦业二路与丈北路十字3号房"));
        System.out.println(convert.convert("锦业二路与丈八北路字三零六医院"));
        System.out.println(convert.convert("锦业二路与丈八北路三零六医院"));
        System.out.println(convert.convert("锦业二路与丈八北路十字第三号楼"));
        System.out.println(convert.convert("锦业二路与丈八北路十字第三号楼"));
        System.out.println(convert.convert("锦业二路与丈八北路十字第3号楼"));
        System.out.println(convert.convert("锦业二路与丈八北路十字3号"));
        System.out.println(convert.convert("锦业二路与丈八北路十字3号房"));
        System.out.println(convert.convert("锦业Ⅱ路与丈八北路Ⅹ字"));
        System.out.println(convert.convert("锦业二路与丈八北路十字甲3号楼"));
        System.out.println(convert.convert("锦业二路与丈八北路十字甲3"));
        System.out.println(convert.convert("锦业二路与丈八北路十字3号"));
        System.out.println(convert.convert("锦业二路与丈八北路十字3号房"));
        System.out.println("----------------------------");
        System.out.println(convert.convert("锦业二路与丈北路十第3号房"));
        System.out.println(convert.convert("锦业二路与丈北路十第五号房"));
        System.out.println(convert.convert("锦业二路与丈北路十第3房"));
        System.out.println(convert.convert("锦业二路与丈北路十第3栋"));
        System.out.println("----------------------------");
        System.out.println(convert.convert("锦业二路与丈北路十东3栋"));
        System.out.println(convert.convert("锦业二路与丈北路十的东3栋"));
        System.out.println(convert.convert("西锦业二路与丈北路十的东3栋"));
        System.out.println(convert.convert("在西锦业二路与丈北路十的东3栋"));
        System.out.println("----------------------------");
        convert.setPriority("4");
        System.out.println(convert.convert("在西锦业二路与丈北路十店东3栋"));
        convert.setPriority("2");
        System.out.println(convert.convert("在西锦业二路与丈北路十店东3栋"));

        System.out.println(convert.convert("北京四维图新股份有限公司", "Bei Jing Si Wei Tu Xin Gu Fen You Xian Gong Si"));
        System.out.println(convert.convert("西红立交桥", "Xi Hong Li Jiao Qiao"));
        System.out.println(convert.convert("贾晓静", "Jia Xiao Jing"));
        System.out.println(convert.convert("望京一医院", "Wang Jing Yi Yi Yuan"));
        System.out.println(convert.convert("5号路", "5 Hao Lu"));
        System.out.println(convert.convert("永丰路3号收费站", "Yong Feng Lu 3 Hao Shou Fei Zhan"));

        TranslateDictData.getInstance().getDictEngKeyword();
    }
}
