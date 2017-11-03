package org.navinfo.dataservice.engine.meta;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //convert.setKindCode("220100");
        //System.out.println(convert.convert("东亚广告、世博广告、复旦世博传播", "Dong Ya Guang Gao 、 Shi Bo Guang Gao 、 Fu Dan Shi Bo Chuan Bo"));
        //convert.setKindCode("120201");
        //System.out.println(convert.convert("工农三村（三、四街坊）西门", "Gong Nong San Cun ( San 、 Si Jie Fang ) Xi Men"));
        //convert.setKindCode("");
        //System.out.println(convert.convert("海口市妇幼保健院东南（中丹路西）"));
        //System.out.println(convert.convert("月华岛"));
        //System.out.println(convert.convert("5号线"));
        //System.out.println(convert.convert("五号线"));
        //System.out.println(convert.convert("地铁１５号线"));
        //System.out.println(convert.convert("地铁五号线"));
        //System.out.println(convert.convert("轻轨１５号线"));
        //System.out.println(convert.convert("轻轨五号线"));
        //System.out.println(convert.convert("昌平线"));
        //System.out.println(convert.convert("北清路口"));
        //System.out.println(convert.convert("小云东岗"));
        //System.out.println(convert.convert("花园东口"));
        //System.out.println(convert.convert("永丰北桥"));
        //System.out.println(convert.convert("花束林"));
        //System.out.println(convert.convert("始皇陵"));
        //System.out.println(convert.convert("花海岛"));
        //
        //System.out.println(convert.convert("东方有线网络有限公司徐家汇街道管理站"));
        //System.out.println(convert.convert("罗浮南天书轩"));
        //System.out.println(convert.convert("东信隆酒类总汇"));
        //System.out.println(convert.convert("湖南人家湘菜馆"));
        //System.out.println(convert.convert("爱儿宝贝儿童百货宁西店"));
        //System.out.println(convert.convert("世纪华联超市南辛庄店"));
        //System.out.println(convert.convert("爽客连锁便利（汕樟南分店）"));
        //System.out.println(convert.convert("金和嘉园西南门"));
        //System.out.println(convert.convert("广东电网惠州供电局（旧）"));
        //System.out.println(convert.convert("广南烟茶酒"));
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
        System.out.println(convert.convert("光明路31号（与商业道交口）交通大楼一楼"));
        System.out.println(convert.convert("金田佳园10号（六一四路）"));
    }

    @Test
    public void fixErrorData() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("update ix_poi_address set fullname = '%s' where name_id = %d");

        String dir = "D://error_data.txt";
        String outDir = "D://error_data_fix.txt";
        File file = new File(dir);
        File outFile = new File(outDir);
        if (outFile.exists()) {
            outFile.createNewFile();
        }

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        FileWriter fw = new FileWriter(outFile);
        BufferedWriter bw = new BufferedWriter(fw);

        EnglishConvert convert = new EnglishConvert();
        Map<Integer, String> map = loadAdminId();

        while (br.read() != -1) {
            String str = br.readLine();

            String[] array = str.split("\\^\\^\\^");

            Integer nameId = Integer.valueOf(array[0]);
            String fullname = array[1];
            Integer regionId = Integer.valueOf(array[2]);

            String engName = map.containsKey(regionId) ? convert.convert(fullname, map.get(regionId)) : convert.convert(fullname);

            String sql = String.format(builder.toString(), engName, nameId);
            System.out.println(sql);
            bw.write(sql);
            bw.newLine();
        }

        fis.close();

        bw.flush();
        bw.close();
    }

    private Map<Integer, String> loadAdminId() {
        Map<Integer, String> map = new HashMap<>();

        String sql = "select admin_id, region_id from ad_admin where u_record <> 2";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;

        try {
            conn = DBConnector.getInstance().getMkConnection();
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                map.put(resultSet.getInt("region_id"), resultSet.getString("admin_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
            DbUtils.closeQuietly(conn);
        }
        return map;
    }

}
