package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
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
        //System.out.println(convert.convert("ＪＯ（Ｅ郞 ＪＯＥ＇Ｓ　ＳＡＬＯＮ"));
        //System.out.println(convert.convert("ＪＯＥ）郞 ＪＯＥ,Ｓ　ＳＡＬＯＮ"));
        //System.out.println(convert.convert("ＪＯＥ×郞 ＪＯＥ，Ｓ　ＳＡＬＯＮ"));
        //System.out.println(convert.convert("ＪＯＥ*郞 ＪＯＥ，Ｓ　ＳＡＬＯＮ"));
        //System.out.println(convert.convert("７１１便利店０３３｜９℃店"));
        //System.out.println(convert.convert("null"));
        //System.out.println(convert.convert("%"));
        //System.out.println(convert.convert("$"));
        //System.out.println(convert.convert(" "));
        //System.out.println(convert.convert(null));
        //System.out.println(convert.convert("陕西2国道10"));
        //System.out.println(convert.convert("国道210"));
        //System.out.println(convert.convert("陕西国道210"));
        //System.out.println(convert.convert("国210道"));
        //System.out.println(convert.convert("210省道"));
        //System.out.println(convert.convert("陕西210省道"));
        //System.out.println(convert.convert("陕西2省道10"));
        //System.out.println(convert.convert("省道210"));
        //System.out.println(convert.convert("陕西省道210"));
        //System.out.println(convert.convert("省210道"));
        //System.out.println(convert.convert("210县道"));
        //System.out.println(convert.convert("陕西210县道"));
        //System.out.println(convert.convert("陕西2县道10"));
        //System.out.println(convert.convert("县道210"));
        //System.out.println(convert.convert("陕西县道210"));
        //System.out.println(convert.convert("县210道"));
        System.out.println(convert.convert("锦业二路与丈北路十字"));
        convert.setPinyin("jin ye er lu he dong ba bei lu shi zi");
        System.out.println(convert.convert("锦业二路与丈北路十字"));
    }
}
