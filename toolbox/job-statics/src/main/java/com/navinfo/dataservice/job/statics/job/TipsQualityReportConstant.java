package com.navinfo.dataservice.job.statics.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangjunfang on 2017/11/6.
 */
public class TipsQualityReportConstant {

    public static final int PROBLEM_SUMMARY_COLUMN_NUM = 40;
    public static final String QC_TEMPLATE_XLS_NAME = "tips_quality_template.xls";
    public static final String TEMPLATE_PROBLEM_SUMMARY_NAME = "Problem Summary";
    //没有关联link的tips红绿灯、挂接、草图和立交桥名
    public static final String NO_REALTE_TYPE_QUERY = "'1901','1803','1806','2102'";
    public static Map<Integer, String> problemSummaryMapping = new HashMap<>();
    public static Map<Integer, String> countTableMapping = new HashMap<>();
    public static final String COUNT_TABLE_TOTAL_FLAG = "total";
    public static final String COUNT_TABLE_ONE_FLAG = "1";

    public static final int COUNT_TABLE_COLUMN_NUM = 93;
    public static final String COUNT_TABLE_COLUMN_SPLIT = ";";
    public static final String TEMPLATE_COUNT_TABLE_NAME = "Count Table";

    static {
        //problemSummary excle列序号与FIELD_RD_QCRECORD字段对应关系，列序号从1开始
        problemSummaryMapping.put(2, "FIELD_GROUP");
        problemSummaryMapping.put(3, "LINK_PID");
        problemSummaryMapping.put(4, "PROVINCE");
        problemSummaryMapping.put(5, "CITY");
        problemSummaryMapping.put(6, "QC_SUBTASK_NAME");
        problemSummaryMapping.put(9, "PROBLEM_NUM");
        problemSummaryMapping.put(11, "MESH_ID");
        problemSummaryMapping.put(15, "CLASS_TOP");
        problemSummaryMapping.put(16, "CLASS_MEDIUM");
        problemSummaryMapping.put(17, "CLASS_BOTTOM");
        problemSummaryMapping.put(18, "PROBLEM_TYPE");
        problemSummaryMapping.put(19, "PROBLEM_PHENOMENON");
        problemSummaryMapping.put(20, "PROBLEM_DESCRIPTION");
        problemSummaryMapping.put(21, "INITIAL_CAUSE");
        problemSummaryMapping.put(22, "ROOT_CAUSE");
        problemSummaryMapping.put(23, "CHECK_USERID");
        problemSummaryMapping.put(24, "CHECK_TIME");
        problemSummaryMapping.put(25, "COLLECTOR_USERID");
        problemSummaryMapping.put(26, "COLLECTOR_TIME");
        problemSummaryMapping.put(27, "CHECK_DEPARTMENT");
        problemSummaryMapping.put(28, "CHECK_MODE");
        problemSummaryMapping.put(29, "MODIFY_DATE");
        problemSummaryMapping.put(30, "MODIFY_USERID");
        problemSummaryMapping.put(31, "CONFIRM_USERID");
        problemSummaryMapping.put(32, "VERSION");
        problemSummaryMapping.put(34, "KIND");
        problemSummaryMapping.put(35, "FC");
        problemSummaryMapping.put(36, "MEMO_USERID");

        //countTable表列序号与Tips类型和GDB对应关系,列序号从1开始
        countTableMapping.put(15, "1806" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_TOTAL_FLAG);//草图
        countTableMapping.put(16, "1803" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_TOTAL_FLAG);//挂接
        countTableMapping.put(17, "1804");//顺行
//        countTableMapping.put(18, "2002");//ADAS测线
//        countTableMapping.put(19, "2001");//测线

        //打点
//        countTableMapping.put(20, "1708");//ADAS打点
//        countTableMapping.put(21, "1706");//GPS打点
//        countTableMapping.put(22, "1709");//点位移
        countTableMapping.put(18, "1703");//分叉口提示（SE）
        countTableMapping.put(19, "1704");//交叉路口名称
        countTableMapping.put(20, "1707");//里程桩
        countTableMapping.put(21, "1705");//立交桥名称
        countTableMapping.put(22, "1702");//铁路道口
        countTableMapping.put(23, "1701");//障碍物
        countTableMapping.put(24, "1901" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_TOTAL_FLAG);//道路名

        //点属性
        countTableMapping.put(25, "1115");//车道变化点
        countTableMapping.put(26, "1117");//车道限高限宽
        countTableMapping.put(27, "1113");//车道限速
        countTableMapping.put(28, "1104");//大门
        countTableMapping.put(29, "1101");//点限速
        countTableMapping.put(30, "1109");//电子眼
        countTableMapping.put(31, "1102");//红绿灯
        countTableMapping.put(32, "1103");//红绿灯方位
        countTableMapping.put(33, "1108");//减速带
        countTableMapping.put(34, "1114");//卡车限速
        countTableMapping.put(35, "1118");//卡车条件限速
        countTableMapping.put(36, "1110");//卡车限制
        countTableMapping.put(37, "1112");//可变限速
        countTableMapping.put(38, "1119");//通用禁停
        countTableMapping.put(39, "1120");//卡车禁停
        String sql1116 = "WITH TMP AS\n" +
                " (SELECT DISTINCT PID\n" +
                "    FROM RD_GSC_LINK RGL\n" +
                "   WHERE RGL.TABLE_NAME NOT IN ('RD_LINK', 'RW_LINK'))\n" +
                "SELECT COUNT(1) CT\n" +
                "  FROM (SELECT PID\n" +
                "          FROM RD_GSC_LINK GL\n" +
                "         WHERE GL.LINK_PID = ?\n" +
                "           AND NOT EXISTS (SELECT 1 FROM TMP T WHERE T.PID = GL.PID)\n" +
                "         GROUP BY PID)";
        countTableMapping.put(40, "1116" + COUNT_TABLE_COLUMN_SPLIT + sql1116);//立交
        countTableMapping.put(41, "1106");//坡度
        countTableMapping.put(42, "1107");//收费站
        countTableMapping.put(43, "1111");//条件限速
        countTableMapping.put(44, "1105");//危险信息

        //范围线属性
        String sql1605 = "SELECT COUNT(1) CT\n" +
                "  FROM RD_LINK_FORM DF\n" +
                " WHERE DF.LINK_PID = ?\n" +
                "   AND DF.FORM_OF_WAY = 36";
        countTableMapping.put(45, "1605" + COUNT_TABLE_COLUMN_SPLIT + sql1605);//POI连接路
        countTableMapping.put(46, "1607");//风景路线
        countTableMapping.put(47, "1601");//环岛
        String sql1604 = "SELECT COUNT(1) CT\n" +
                "  FROM RD_LINK_FORM DF\n" +
                " WHERE DF.LINK_PID = ?\n" +
                "   AND DF.FORM_OF_WAY = 52";
        countTableMapping.put(48, "1604" + COUNT_TABLE_COLUMN_SPLIT + sql1604);//区域内道路
        countTableMapping.put(49, "1606");//收费开放道路
        countTableMapping.put(50, "1602");//特殊交通类型

        //关系属性
        countTableMapping.put(51, "1301");//车信
        countTableMapping.put(52, "1310");//公交车道
        countTableMapping.put(53, "1302");//交限
        countTableMapping.put(54, "1304");//禁止穿行
        countTableMapping.put(55, "1308");//禁止卡车驶入
        countTableMapping.put(56, "1305");//禁止驶入
        countTableMapping.put(57, "1303");//卡车交限
        countTableMapping.put(58, "1311");//可变导向车道
        countTableMapping.put(59, "1306");//路口语音引导
//        countTableMapping.put(60, "1307");//自然语音引导

        //行人导航
        countTableMapping.put(60, "2203");//单线虚拟连接
        countTableMapping.put(61, "2204");//复合虚拟连接
        countTableMapping.put(62, "2201");//过街天桥/地下通道
        countTableMapping.put(63, "2202");//人行过道

        //路口图形
        countTableMapping.put(64, "1403");//3D
        countTableMapping.put(65, "1402");//Real Sign
        countTableMapping.put(66, "1401");//方向看板
        countTableMapping.put(67, "1407");//高速分歧
        countTableMapping.put(68, "1410");//高速入口模式图
        countTableMapping.put(69, "1409");//普通路口模式图
        countTableMapping.put(70, "1406");//实景图
        countTableMapping.put(71, "1404");//提左提右
        countTableMapping.put(72, "1405");//一般道路方面

        //万能标记
        countTableMapping.put(73, "2101" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_TOTAL_FLAG);//万能标记

        //起终点打点
        countTableMapping.put(74, "1504");//Overpass
        countTableMapping.put(75, "1505");//Underpass
        countTableMapping.put(76, "1517");//Usage Fee Required
        countTableMapping.put(77, "1507");//步行街
        countTableMapping.put(78, "1512");//辅路
        countTableMapping.put(79, "1503");//高架路
        countTableMapping.put(80, "1508");//公交专用道路
        countTableMapping.put(81, "1516");//季节性关闭道路
        countTableMapping.put(82, "1518");//阶梯
        countTableMapping.put(83, "1509");//跨线立交桥
        countTableMapping.put(84, "1502");//路面无覆盖
        countTableMapping.put(85, "1510");//桥
        String sql1501 = "SELECT COUNT(1) CT\n" +
                "  FROM RD_LINK RL\n" +
                " WHERE RL.LINK_PID = ?\n" +
                "   AND RL.MULTI_DIGITIZED = 1;";
        countTableMapping.put(86, "1501" + COUNT_TABLE_COLUMN_SPLIT + sql1501);//上下线分离
        countTableMapping.put(87, "1514");//施工
        countTableMapping.put(88, "1506");//私道
        countTableMapping.put(89, "1511");//隧道
        countTableMapping.put(90, "1515");//维修
        countTableMapping.put(91, "1519");//休闲路线
        countTableMapping.put(92, "1520");//在建时间变更
        countTableMapping.put(93, "1513");//窄道


        //线属性
        countTableMapping.put(94, "1206");//PA
        countTableMapping.put(95, "1205");//SA
        countTableMapping.put(96, "1204");//潮汐车道
        countTableMapping.put(97, "1202" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_ONE_FLAG);//车道数
        countTableMapping.put(98, "1203" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_ONE_FLAG );//道路方向
        String sql1211 = "SELECT COUNT(1) CT\n" +
                "  FROM RD_LINK_FORM DF\n" +
                " WHERE DF.LINK_PID = ?\n" +
                "   AND DF.FORM_OF_WAY IN (10，11)";
        countTableMapping.put(99, "1211" + COUNT_TABLE_COLUMN_SPLIT + sql1211);//高速连接路
        countTableMapping.put(100, "1212");//高速路行人非机动车通行
        countTableMapping.put(101, "1213");//普通路行人非机动车禁行
        countTableMapping.put(102, "1214");//删除在建属性
        countTableMapping.put(103, "1208");//停车场出入口Link
        String sql1207 = "SELECT COUNT(1) CT\n" +
                "  FROM RD_LINK_FORM DF\n" +
                " WHERE DF.LINK_PID = ?\n" +
                "   AND DF.FORM_OF_WAY = 15";
        countTableMapping.put(104, "1207" + COUNT_TABLE_COLUMN_SPLIT + sql1207);//匝道
        countTableMapping.put(105, "1201" + COUNT_TABLE_COLUMN_SPLIT + COUNT_TABLE_ONE_FLAG);//种别

        //精细化
        countTableMapping.put(106, "2301");//功能面
        countTableMapping.put(107, "2302");//建筑物
    }

    public static void main(String args[]) {
        Map<String,List<String>> map1 = new HashMap();
        List<String> list1 = new ArrayList<>();
        list1.add("a");
        list1.add("b");
        list1.add("c");
        map1.put("1", list1);
        List<String> list2 = new ArrayList<>();
        list2.add("aa");
        list2.add("bb");
        list2.add("cc");
        map1.put("2", list2);

        Map<String,List<String>> map2 = new HashMap();
        List<String> list11 = new ArrayList<>();
        list11.add("e");
        list11.add("d");
        list11.add("f");
        map1.put("1", list11);
        Map<String,List<String>> map3 = new HashMap();
        List<String> list3 = new ArrayList<>();
        list3.add("ee");
        list3.add("de");
        list3.add("fe");
        map1.put("1", list3);

        Map<String,List<String>> map = new HashMap();
        map.putAll(map1);
        map.putAll(map2);

        for(String key : map.keySet()) {
            System.out.println("key: " + key);
            List<String> listss = map.get(key);
            for(String l : listss) {
                System.out.println("list: " + l);
            }
        }
    }
}
