package com.navinfo.dataservice.engine.fcc.tips;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: TipsStatConstant.java
 * @author y
 * @date 2017-4-19 下午7:18:41
 * @Description: copy  from robot
 *  
 */
public class TipsStatConstant {
	//统计坐标为g_location的tips类型
	public static final List<String> gLocationTipsType = new ArrayList<>();

    //统计坐标为SLoc的tips类型
	public static final List<String> gSLocTipsType = new ArrayList<>();

    //统计坐标为Geo的tips类型
	public static final List<String> gGeoTipsType = new ArrayList<>();

    //情报预处理新增Tips需要deep.id赋值的Tips类型
	public static final List<String> preTipsDeepIdType = new ArrayList<>();

	//质检上传linkpid赋值的Tips类型 exp.id
	public static final List<String> fieldQCExpIdType = new ArrayList<>();

    //质检上传linkpid赋值的Tips类型 f.id
    public static final List<String> fieldQCFIdType = new ArrayList<>();

    //质检上传linkpid赋值的Tips类型 f.id and node
    public static final List<String> fieldQCFIdNodeType = new ArrayList<>();

    //质检上传linkpid赋值的Tips类型 in.id
    public static final List<String> fieldQCInIdType = new ArrayList<>();

    //质检上传linkpid赋值的Tips类型 out.id
    public static final List<String> fieldQCOutIdType = new ArrayList<>();


    //质检上传linkpid赋值的Tips类型 farray.id
    public static final List<String> fieldQCFArrayIdType = new ArrayList<>();

    //质检上传linkpid赋值的Tips类型 parray.id
    public static final List<String> fieldQCPArrayIdType = new ArrayList<>();

	static{
		//统计坐标为g_location的tips类型
		gLocationTipsType.add("1101");
		gLocationTipsType.add("1102");
		gLocationTipsType.add("1103");
		gLocationTipsType.add("1104");
		gLocationTipsType.add("1105");
		gLocationTipsType.add("1106");
		gLocationTipsType.add("1107");
		gLocationTipsType.add("1108");
		gLocationTipsType.add("1109");
		gLocationTipsType.add("1110");
		gLocationTipsType.add("1111");
		gLocationTipsType.add("1112");
		gLocationTipsType.add("1113");
		gLocationTipsType.add("1114");
		gLocationTipsType.add("1115");
		gLocationTipsType.add("1116");
        gLocationTipsType.add("1117");
        //20171010 新增
        gLocationTipsType.add("1118");
        gLocationTipsType.add("1119");
        gLocationTipsType.add("1120");
		gLocationTipsType.add("1201");
		gLocationTipsType.add("1202");
		gLocationTipsType.add("1203");
		gLocationTipsType.add("1204");
		gLocationTipsType.add("1205");
		gLocationTipsType.add("1206");
		gLocationTipsType.add("1207");
		gLocationTipsType.add("1208");
		gLocationTipsType.add("1209");
		gLocationTipsType.add("1210");
        gLocationTipsType.add("1211");
        gLocationTipsType.add("1212");
        gLocationTipsType.add("1213");
        gLocationTipsType.add("1214");
		gLocationTipsType.add("1301");
		gLocationTipsType.add("1302");
		gLocationTipsType.add("1303");
		gLocationTipsType.add("1304");
		gLocationTipsType.add("1305");
		gLocationTipsType.add("1306");
		gLocationTipsType.add("1307");
		gLocationTipsType.add("1308");
		gLocationTipsType.add("1309");
		gLocationTipsType.add("1310");
		gLocationTipsType.add("1311");
		gLocationTipsType.add("1401");
		gLocationTipsType.add("1402");
		gLocationTipsType.add("1403");
		gLocationTipsType.add("1404");
		gLocationTipsType.add("1405");
		gLocationTipsType.add("1406");
		gLocationTipsType.add("1407");
		gLocationTipsType.add("1408");
		gLocationTipsType.add("1409");
		gLocationTipsType.add("1410");
		gLocationTipsType.add("1701");
		gLocationTipsType.add("1702");
		gLocationTipsType.add("1703");
		gLocationTipsType.add("1704");
		gLocationTipsType.add("1705");
		gLocationTipsType.add("1706");
		gLocationTipsType.add("1707");
        gLocationTipsType.add("1708");
        gLocationTipsType.add("1709");
		gLocationTipsType.add("1801");
		gLocationTipsType.add("1802");
		gLocationTipsType.add("1803");
		gLocationTipsType.add("1804");
		gLocationTipsType.add("1805");
		gLocationTipsType.add("1806");
		gLocationTipsType.add("2101");
		gLocationTipsType.add("2102");
		gLocationTipsType.add("9901");
		gLocationTipsType.add("8002");
		gLocationTipsType.add("8008");
        gLocationTipsType.add("2203");
        gLocationTipsType.add("2204");

		//统计坐标为gSLoc的tips类型
		gSLocTipsType.add("1501");
		gSLocTipsType.add("1502");
		gSLocTipsType.add("1503");
		gSLocTipsType.add("1504");
		gSLocTipsType.add("1505");
		gSLocTipsType.add("1506");
		gSLocTipsType.add("1507");
		gSLocTipsType.add("1508");
		gSLocTipsType.add("1509");
		gSLocTipsType.add("1510");
		gSLocTipsType.add("1511");
		gSLocTipsType.add("1512");
		gSLocTipsType.add("1513");
		gSLocTipsType.add("1514");
		gSLocTipsType.add("1515");
		gSLocTipsType.add("1516");
		gSLocTipsType.add("1517");
        gSLocTipsType.add("1518");
        gSLocTipsType.add("1519");
        gSLocTipsType.add("1520");

		//统计坐标为gGeo的tips类型
		gGeoTipsType.add("1601");
		gGeoTipsType.add("1602");
		gGeoTipsType.add("1603");
		gGeoTipsType.add("1604");
		gGeoTipsType.add("1605");
		gGeoTipsType.add("1606");
		gGeoTipsType.add("1607");
		gGeoTipsType.add("1901");
		gGeoTipsType.add("2001");
        gGeoTipsType.add("2002");
        gGeoTipsType.add("8001");
		gGeoTipsType.add("8003");
		gGeoTipsType.add("8004");
        gGeoTipsType.add("8005");
        gGeoTipsType.add("8006");
        gGeoTipsType.add("8007");
        gGeoTipsType.add("8009");
        gGeoTipsType.add("8010");
		gGeoTipsType.add("2201");
        gGeoTipsType.add("2202");

        //情报预处理新增Tips需要deep.id赋值的Tips类型
        preTipsDeepIdType.add("2001");
        preTipsDeepIdType.add("1301");
        preTipsDeepIdType.add("1101");
        preTipsDeepIdType.add("1102");
        preTipsDeepIdType.add("1107");
        preTipsDeepIdType.add("2101");
        preTipsDeepIdType.add("1302");

        fieldQCExpIdType.add("1307");

        fieldQCFIdType.add("1101");
        fieldQCFIdType.add("1109");
        fieldQCFIdType.add("1110");
        fieldQCFIdType.add("1111");
        fieldQCFIdType.add("1113");
        fieldQCFIdType.add("1114");
        fieldQCFIdType.add("1117");
        fieldQCFIdType.add("1118");
        fieldQCFIdType.add("1119");
        fieldQCFIdType.add("1120");
        fieldQCFIdType.add("1201");
        fieldQCFIdType.add("1202");
        fieldQCFIdType.add("1203");
        fieldQCFIdType.add("1205");
        fieldQCFIdType.add("1206");
        fieldQCFIdType.add("1207");
        fieldQCFIdType.add("1208");
        fieldQCFIdType.add("1211");
        fieldQCFIdType.add("1212");
        fieldQCFIdType.add("1213");
        fieldQCFIdType.add("1214");
        fieldQCFIdType.add("1304");
        fieldQCFIdType.add("1305");
        fieldQCFIdType.add("1308");
        fieldQCFIdType.add("1311");
        fieldQCFIdType.add("1707");
        fieldQCFIdType.add("2101");

        fieldQCFIdNodeType.add("1115");
        fieldQCFIdNodeType.add("1701");
        fieldQCFIdNodeType.add("1702");
        fieldQCFIdNodeType.add("1704");
        fieldQCFIdNodeType.add("1706");

        fieldQCInIdType.add("1103");
        fieldQCInIdType.add("1104");
        fieldQCInIdType.add("1105");
        fieldQCInIdType.add("1107");
        fieldQCInIdType.add("1108");
        fieldQCInIdType.add("1112");
        fieldQCInIdType.add("1301");
        fieldQCInIdType.add("1302");
        fieldQCInIdType.add("1303");
        fieldQCInIdType.add("1306");
        fieldQCInIdType.add("1401");
        fieldQCInIdType.add("1402");
        fieldQCInIdType.add("1403");
        fieldQCInIdType.add("1404");
        fieldQCInIdType.add("1405");
        fieldQCInIdType.add("1406");
        fieldQCInIdType.add("1407");
        fieldQCInIdType.add("1409");
        fieldQCInIdType.add("1410");
        fieldQCInIdType.add("1703");
        fieldQCInIdType.add("1804");

        fieldQCOutIdType.add("1106");

        fieldQCFArrayIdType.add("1116");
        fieldQCFArrayIdType.add("1204");
        fieldQCFArrayIdType.add("1310");
        fieldQCFArrayIdType.add("1501");
        fieldQCFArrayIdType.add("1502");
        fieldQCFArrayIdType.add("1503");
        fieldQCFArrayIdType.add("1504");
        fieldQCFArrayIdType.add("1505");
        fieldQCFArrayIdType.add("1506");
        fieldQCFArrayIdType.add("1507");
        fieldQCFArrayIdType.add("1508");
        fieldQCFArrayIdType.add("1509");
        fieldQCFArrayIdType.add("1510");
        fieldQCFArrayIdType.add("1511");
        fieldQCFArrayIdType.add("1512");
        fieldQCFArrayIdType.add("1513");
        fieldQCFArrayIdType.add("1514");
        fieldQCFArrayIdType.add("1515");
        fieldQCFArrayIdType.add("1516");
        fieldQCFArrayIdType.add("1517");
        fieldQCFArrayIdType.add("1518");
        fieldQCFArrayIdType.add("1519");
        fieldQCFArrayIdType.add("1520");
        fieldQCFArrayIdType.add("1601");
        fieldQCFArrayIdType.add("1602");
        fieldQCFArrayIdType.add("1604");
        fieldQCFArrayIdType.add("1605");
        fieldQCFArrayIdType.add("1606");
        fieldQCFArrayIdType.add("1607");

        fieldQCPArrayIdType.add("2203");
        fieldQCPArrayIdType.add("2204");

    }

}
