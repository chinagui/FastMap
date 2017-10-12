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

        System.out.println(convert.convert("品亭曲桥"));
        //System.out.println(convert.convert("视立明视力恢复训练中心福州五四北泰禾店"));
        //System.out.println(convert.convert("蜀风王火锅五四北泰禾店"));
        //System.out.println(convert.convert("老子山张咀镇电子商务专业合作社"));
        //System.out.println(convert.convert("老七三门牛肉"));
        //System.out.println(convert.convert("美罗汇健康药房白城三二一店"));
        //System.out.println(convert.convert("福天升过桥米线五四桥店"));
        //System.out.println(convert.convert("百草堂医药有限公司第一零售店"));
        //System.out.println(convert.convert("玉珠峰宾馆六一桥店"));
        //System.out.println(convert.convert("海洋国际旅行社五一九店"));
        //System.out.println(convert.convert("洛南大盛医药有限公司第八零售药店"));
        //System.out.println(convert.convert("泽强药店连锁二一七店"));
        //System.out.println(convert.convert("泽强药店连锁一九零店"));
        //System.out.println(convert.convert("泽强药店二零六店"));
        //System.out.println(convert.convert("泽强药店二零八店"));
        //System.out.println(convert.convert("泽强药店二二七店"));
        //System.out.println(convert.convert("沈老头包子混沌二零八店"));
        //System.out.println(convert.convert("新标榜三三潮店"));
        //System.out.println(convert.convert("德惠市大青咀镇二青咀村小学"));
        //System.out.println(convert.convert("德惠市夏家店街道苇咀子村村民委员会"));
        //System.out.println(convert.convert("德惠市夏家店街道四青咀村村民委员会"));
        //System.out.println(convert.convert("康佰家大药房八一五店"));
        //System.out.println(convert.convert("商南县药材公司第四零售店"));
        //System.out.println(convert.convert("六九和食杰瀛"));
        //System.out.println(convert.convert("八九九纸包鱼"));
        //System.out.println(convert.convert("八三九牛牛"));
        //System.out.println(convert.convert("全土宁化菜馆五四北店"));
        //System.out.println(convert.convert("健修堂大药房第二零售店"));
        //System.out.println(convert.convert("五零山居"));
        //System.out.println(convert.convert("五七一绝"));
        //System.out.println(convert.convert("九五一品"));
        //System.out.println(convert.convert("九三鸭霸王"));
        //System.out.println(convert.convert("九三鸭霸王"));
        //System.out.println(convert.convert("中国联通一六合兴固网沃店"));
        //System.out.println(convert.convert("三八妇乐"));
        //System.out.println(convert.convert("三八妇乐"));
        //System.out.println(convert.convert("三八妇乐"));
        //System.out.println(convert.convert("三三炊饭"));
        //System.out.println(convert.convert("三三正道凉糕"));
        //System.out.println(convert.convert("三三正道凉糕"));
        //System.out.println(convert.convert("三三咕噜鱼"));
        //System.out.println(convert.convert("万家康大药房长生路五一二店"));
        //System.out.println(convert.convert("一九九叁"));
        //System.out.println(convert.convert("一七闪店"));
    }

}
