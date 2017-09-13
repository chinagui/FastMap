package com.navinfo.navicommons.geo.computation;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.commons.util.DoubleUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Created by IntelliJ IDEA. User: liuqing Date: 2010-8-4 Time: 8:58:15
 * 地理坐标相关的工具类
 * 注意：方法中传入图幅号和GRID号参数，如果类型是number型，则可以传5位图幅或者7位grid号，String类型的必须传入之前图幅补齐至6位，grid号补齐至8位
 */
public abstract class MeshUtils {

    private static Logger log = Logger.getLogger(MeshUtils.class);

   /* public static void main(String[] args) throws Exception {*/
        //		Set<String> meshes = new HashSet<String>();
        //		meshes.add("595671");
        //		meshes.add("595672");
        //		meshes.add("595661");
        //		Geometry geo = meshes2Jts(meshes);
        //		System.out.println("GEOMEYRY:"+JtsGeometryFactory.writeWKT(geo));
        //		for(String d:point2Meshes(116.37475,39.8854)){
        //			System.out.println(String.valueOf(d));
        //		}
        //		System.out.println(""+second2Decimal(300.0));
        //		String[] results = point2Meshes(116.74963, 39.0);
        //		Set<String> results = getNeighborMeshSet("605602",2);
        //		Set<String> meshes = CompGeometryUtil.geoToMeshesWithoutBreak(JtsGeometryFactory.read("POLYGON (
        // (116.463992525 40.77742045223645, 116.477837075 40.77264595242743, 116.499513575 40.75410725316898,
        // 116.558322725 40.70280255522117, 116.561971625 40.681209156084904, 116.560584725 40.63396505797467,
        // 116.513650475 40.57633706027978, 116.4617963 40.525370812318435, 116.402449925 40.52012866252812,
        // 116.328167 40.49948901335371, 116.294812625 40.48393106397603, 116.294731475 40.450386315317814,
        // 116.288055275 40.404981317134016, 116.290748975 40.383416117996624, 116.290748975 40.38341601799663,
        // 116.285011025 40.37729766824136, 116.269207025 40.35668901906571, 116.24731295 40.331597670069364,
        // 116.23034855 40.31823252060397, 116.20527935 40.298544171391505, 116.19905915 40.297825621420245,
        // 116.19038855 40.30371777118456, 116.16588485 40.30343032119605, 116.1455021 40.306561921070795,
        // 116.124039875 40.31176557086265, 116.111507 40.32862102018843, 116.104813775 40.33073537010385,
        // 116.067505925 40.32706537025066, 116.0585864 40.32408902036971, 116.056230275 40.3226517704272,
        // 116.055726875 40.31700107065323, 116.048206025 40.31367202078639, 116.0404913 40.31266297082675,
        // 116.031736025 40.31306102081083, 116.028926975 40.32412202036839, 116.013643175 40.33222382004432,
        // 115.988573825 40.322730170424066, 115.985557925 40.305325171120266, 115.982919125 40.280152672127166,
        // 115.975379525 40.26950832255294, 115.959155675 40.25704622305142, 115.9332041 40.25548847311373,
        // 115.933202825 40.25548902311371, 115.921047725 40.261202322885175, 115.890776525 40.302702621225166,
        // 115.888597625 40.340642619707566, 115.898267825 40.35241586923664, 115.883402525 40.350491569313604,
        // 115.874570675 40.34799001941367, 115.872299 40.35879501898147, 115.864708025 40.36279801882135,
        // 115.854896975 40.37000001853327, 115.848998975 40.37694001825567, 115.8326948 40.37882026818046,
        // 115.818279725 40.38641521787666, 115.81277502500001 40.40458401714991, 115.764307025 40.414668166746544,
        // 115.762052825 40.41730111664123, 115.761195425 40.41829756660137, 115.7593181 40.42048226651398,
        // 115.754155625 40.426524866272274, 115.7515385 40.44171326566474, 115.753443425 40.45961351494873,
        // 115.761470825 40.46778761462176, 115.773010025 40.473981014374026, 115.77737 40.48622601388423,
        // 115.766232875 40.48765371382712, 115.755348275 40.489826613740206, 115.745681975 40.49233201363999,
        // 115.736023025 40.50473501314387, 115.7405957 40.51301866281252, 115.7553158 40.53282196202039,
        // 115.755279425 40.533170412006456, 115.755225875 40.533709561984885, 115.7551721 40.53423846196373,
        // 115.7550908 40.535047361931376, 115.754832725 40.53757901183011, 115.754591825 40.53997151173441,
        // 115.756468625 40.53976616174262, 115.76101010000001 40.53925796176295, 115.7612264 40.539233911763915,
        // 115.775120825 40.54924211136358, 115.7915 40.56134801087935, 115.79921817500001 40.55745901103491,
        // 115.8136616 40.55699301105355, 115.821598025 40.56342151079641, 115.824878825 40.58025096012323,
        // 115.827625625 40.5875892598297, 115.871566325 40.59479530954146, 115.888234625 40.59652645947221,
        // 115.891228625 40.601918659256526, 115.909738025 40.61845620859502, 115.927891025 40.612770008822466,
        // 115.943868575 40.611373158878344, 115.953750275 40.60146375927472, 115.972474025 40.60152480927228,
        // 115.975502 40.59155300967115, 115.982342975 40.579115410168654, 116.002871975 40.57579301030155,
        // 116.006222 40.58837400979831, 116.0169854 40.59434600955943, 116.01991782500001 40.59852510939226,
        // 116.030871275 40.59725885944292, 116.061569 40.60907200897039, 116.077984025 40.61103800889175,
        // 116.094465125 40.612736258823816, 116.111183 40.61621900868451, 116.120411 40.62535900831891,
        // 116.117908025 40.630562008110786, 116.1159584 40.63743540783585, 116.111591 40.644068007570546,
        // 116.113873025 40.649010007372866, 116.132248025 40.65577200710239, 116.136802925 40.66332545680025,
        // 116.142512975 40.66726750664257, 116.165591975 40.66820800660495, 116.173714175 40.69288245561797,
        // 116.171204675 40.69606850549053, 116.186561975 40.71871400458471, 116.203376975 40.71570800470495,
        // 116.216682425 40.731966154054625, 116.213257925 40.74028020372206, 116.2019534 40.751510353272856,
        // 116.2358735 40.775634252307896, 116.235107 40.78333800199975, 116.252420375 40.812393300837535,
        // 116.262809225 40.82688465025788, 116.277035 40.84021499972467, 116.329508225 40.86426724876258,
        // 116.362889525 40.85984279893956, 116.368355975 40.85911824896854, 116.3766272 40.85802194901239,
        // 116.382867425 40.85719484904548, 116.383263575 40.85205149925121, 116.383957175 40.8430466996114,
        // 116.384297525 40.83862744978817, 116.387536625 40.79657445147029, 116.423554025 40.788997051773386,
        // 116.432225225 40.7809142520967, 116.435295725 40.77449810235335, 116.4388658 40.76707655265021,
        // 116.444843825 40.76934325255954, 116.453941025 40.76614400268751, 116.465564975 40.77260100242923,
        // 116.463992525 40.77742045223645), (115.9064288 40.48128261408196, 115.918357625 40.48696741385457,
        // 115.92830765 40.49541906351651, 115.943398625 40.50084326329954, 115.968273875 40.50273541322385,
        // 115.979218925 40.50715046304725, 115.995802325 40.50891651297661, 116.003596625 40.50450146315321,
        // 116.003928275 40.48999486373347, 116.004591575 40.486462863874756, 116.007244925 40.479272664162366,
        // 116.013878375 40.47775891422291, 116.021672675 40.46728896464171, 116.022833675 40.46224321484354,
        // 116.024823575 40.448115065408665, 116.025155375 40.44496146553481, 116.014541675 40.440924865696275,
        // 116.0060012 40.43871296578475, 116.004591575 40.43454846595133, 116.0032649 40.42754451623149,
        // 115.982535575 40.4267242662643, 115.97557055 40.4251467663274, 115.972253825 40.42527296632235,
        // 115.95567035 40.4251467663274, 115.95243657500001 40.4264718662744, 115.939252775 40.42584086629964,
        // 115.9349411 40.43751371583272, 115.91089505 40.44281466562068, 115.886351525 40.45089231529758,
        // 115.884859025 40.45833886499972, 115.897794125 40.46780486462107, 115.906583375 40.48004751413137,
        // 115.9064288 40.48128261408196))"));
        //		int[] meshes = new int[]{605641, 605642, 605555, 605537, 605556, 605663, 605557, 605664, 605660,
        // 605661, 605662, 615604, 605640, 615506, 615611, 615505, 615612, 615613, 615507, 615614, 605577, 605576,
        // 605575, 605652, 605546, 605674, 605547, 605672, 605673, 605670, 605671, 605650, 605651, 605631, 605630,
        // 615602, 615603, 615600, 615601, 615622, 615623, 605565, 605567, 605566};
        //		for(String m:meshes){
        //			System.out.println(mesh2WKT(String.valueOf(m)));
        //		}
        //		String wkt = mesh2WKT("595652");//595650,595651,595652
        //		System.out.println(wkt);
        //		List<String> results = lonlat2MeshIds(76.01,30.33333);
        //		System.out.println(StringUtils.join(results,","));

        //		int[] locs = mesh2Location("595671");//116.125,39.91667--116.25,40
        //		for(int l:locs){
        //			System.out.println(l);
        //		}
        //		List<String> meshes = lonlat2MeshIds(116.375,39.87867);//595663,595662
        //
        //		for(String str:meshes){
        //			System.out.println(str);
        //		}
        //		int[] results = mesh2Location("595664");
        //		for(int r:results){
        //			System.out.println(r/3600.0);
        //		}
        //		System.out.println(String.format("%02d",9));

        //		System.out.println(location2Mesh(23.1*3600, 88.9*3600));
        //		String meshId = "595671";
        //		Set<String> meshIdSet = new HashSet<String>();
        //		meshIdSet.add("35672");
        //		Set<String> result = MeshUtils.getNeighborMeshSet(meshIdSet, 3);
        //		System.out.println(result.size());
        //		System.out.println(result);

        //		double[] a = mesh2LocationLatLon("595671");

        //		System.out.println(a[0]+","+a[1]);
        //
        //		List<String> b = lonlat2MeshIds(a[0],a[1]);
        //
        //		System.out.println(b.toString());


        // int meshid = 595673;
        // double[] a =mesh2LocationLatLon("24967");
        // System.out.println(a[0]+","+a[1]);
        //
        // System.out.println(lonlat2Mesh(109.875,1.833333));

        // String wkt = mesh2WKT("595651"); System.out.println(wkt);

        //		String wkt1 = "LINESTRING (1 1, 4 3)";
        //
        //		String wkt2 = "POLYGON ((2 2,3 2,3 3,2 3,2 2))";
        //
        //		Geometry geom1 = new WKTReader().read(wkt1);
        //
        //		Geometry geom2 = new WKTReader().read(wkt2);
        //
        //		Geometry geom = linkInterMeshPolygon(geom1, geom2);
        //
        //		System.out.println(geom.getGeometryType());

        // (M3*10+M4)*3600+M6*450+60*3600
        // (M1*10+M2)*2400+M5*300
        // int x = (5 * 10 + 6) * 3600 + 3 * 450 + 60 * 3600;
        // int y = (5 * 10 + 9) * 2400 + 7 * 300;

        // System.out.println(decimal2Second(22.97195));
        // System.out.println(second2Decimal(decimal2Second(22.97195)));

        // System.out.println(x); //经度
        // System.out.println(y); //维度
        // System.out.println(location2Mesh(x + 225, y + 150));

		/*
         * System.out.println("=================================================="
		 * ); String[] allMesh = get9NeighborMesh("595673"); for (int i = 0; i <
		 * allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
        // System.out
        // .println("==================================================");
        // Set<String> allMesh = get9NeighborMesh2("45172", 3);
        // int i = 0;
        // for (String s : allMesh) {
        // System.out.println("mesh" + (i + 1) + ":" + s);
        // i++;
        // }

		/*
         * System.out.println("=================================================="
		 * ); int lo[] = mesh2Location("595662"); for (int i = 0; i < lo.length;
		 * i++) { int i1 = lo[i]; System.out.println(i1); }
		 * System.out.println("=================================================="
		 * ); lo = mesh2Location("605604"); for (int i = 0; i < lo.length; i++)
		 * { int i1 = lo[i]; System.out.println(i1); }
		 * 
		 * System.out.println("=================================================="
		 * ); allMesh = area2Meshes(418725, 143550, 419625, 144150); for (int i
		 * = 0; i < allMesh.length; i++) { String s = allMesh[i];
		 * System.out.println("mesh" + (i + 1) + ":" + s); }
		 */
        /*
         * int a[] = mesh2Location("595651"); for (int m : a) {
		 * 
		 * // System.out.println(m); ; System.out.println(second2Decimal(m)); ;
		 * } String wkt = mesh2WKT("595651"); System.out.println(wkt);
		 */
        //System.out.println(Arrays.toString(line2Meshes(116.5, 40.08339, 116.5, 40.08333)));
        //System.out.println(Arrays.toString(line2Meshes(116.5, 40.08333, 116.50007, 40.08333)));
        //System.out.println(Arrays.toString(line2Meshes(116.5, 40.08333, 116.5, 40.08328)));
        //System.out.println(Arrays.toString(line2Meshes(116.49989, 40.08333, 116.5, 40.08333)));
        //System.out.println(Arrays.toString(line2Meshes(116.5, 40.08345, 116.50022, 40.08333)));
        //System.out.println(Arrays.toString(line2Meshes(116.50032, 40.08341, 116.50049, 40.0835)));
        //System.out.println(Arrays.toString(line2Meshes(116.5, 40.08328, 116.50007, 40.08333)));
 /*       Geometry geo1 = GeoTranslator.wkt2Geometry("POLYGON ((116.5 39.75, 116.5 39.833333, 116.625 39.833333, 116.625 39.75, 116.5 39.75))");
        Geometry geo2 = GeoTranslator.wkt2Geometry("POLYGON ((116.52351 39.74985, 116.52341 39.74997, 116.52353 39.74997, 116.52365 39.74997, 116.52351 39.74985))");
        System.out.println(geo1.intersects(geo2));
        System.out.println(mesh2Jts("595654").intersects(geo2));
    }*/


    /**
     * 2016.5重新实现Java版，By Xiao Xiaowen
     *
     * @param x
     * @param y
     * @return 图幅数组，且有顺序，按顺序为左下，右下，右上，左上
     */
    public static String[] point2Meshes(double x, double y) {
        //将度单位坐标转换为秒*3600，并乘1000消除小数,最后取整
        long longX = Math.round(x * 3600000);
        long longY = Math.round(y * 3600000);
        int M1M2;
        int M1M2_bak;
        int M3M4;
        int M3M4_bak;
        int M5;
        int M5_bak = -999;
        int M6;
        int M6_bak = -999;


        //一个四位图幅的纬度高度为2400秒
        M1M2 = (int) (longY / (2400000));
        M3M4 = ((int) x) - 60;//简便算法
        //
        M1M2_bak = M1M2;
        M3M4_bak = M3M4;

        //

        int yt = (int) (longY / (300000));
        M5 = yt % 8;
        //判断在图幅线上的情况
        if ((longY % 300000) == 0) {//直接在理想行号图廓线上，0,0.25,0.5,1,...
            M5_bak = M5 - 1;
            if (M5_bak < 0) {
                M1M2_bak--;
                M5_bak = 7;
            }
        } else if ((longY % 300000) <= 12) {//距离理想行号下图廓线距离
            if (yt % 3 == 2) {
                //处于横轴图廓线上
                M5_bak = M5 - 1;
                if (M5_bak < 0) {
                    M1M2_bak--;
                    M5_bak = 7;
                }
            }
            /**
             if(yt%3==0){//0.0,0.25,...
             //不变
             }else if(yt%3==1){//0.08333,0.33333,...
             //不变
             }else if(yt%3==2){//0.16667,0.41667,...
             //处于图廓线上
             M5_bak = M5-1;
             }*/
        } else if ((300000 - (longY % 300000)) <= 12) {//距离理想行号上图廓线距离
            /**
             if(yt%3==0){//0.0,0.25,...
             //处于图廓线上
             M5_bak = M5+1;
             }else if(yt%3==1){//0.08333,0.33333,...
             //不变
             }else if(yt%3==2){//0.16667,0.41667,...
             //处于图廓线上
             M5_bak = M5+1;
             }
             */
            if (yt % 3 == 0) {
                //处于图廓线上
                M5_bak = M5 + 1;
                if (M5_bak > 7) {
                    M1M2_bak++;
                    M5_bak = 0;
                }
            }
        }
        int xt = (int) (longX / (450000));
        M6 = xt % 8;
        //经度坐标没有四舍五入，所以理论上只有=0和大于12的情况
        if ((longX % 450000) <= 12) {
            M6_bak = M6 - 1;
            if (M6_bak < 0) {
                M3M4_bak--;
                M6_bak = 7;
            }
        }

        String[] meshes = null;
        if (M5_bak > -999 && M6_bak > -999) {//图廓点，4个图幅,4个grid号
            meshes = new String[4];
            if (M1M2_bak < M1M2 || M5_bak < M5) {
                if (M3M4_bak < M3M4 || M6_bak < M6) {
                    meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
                    meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
                    meshes[2] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                    meshes[3] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
                } else {
                    meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
                    meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
                    meshes[2] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
                    meshes[3] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                }
            } else {
                if (M3M4_bak < M3M4 || M6_bak < M6) {
                    meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
                    meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                    meshes[2] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
                    meshes[3] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
                } else {
                    meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                    meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
                    meshes[2] = String.format("%02d%02d%d%d", M1M2_bak, M3M4_bak, M5_bak, M6_bak);
                    meshes[3] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
                }
            }
        } else if (M5_bak > -999) {
            meshes = new String[2];
            if (M1M2_bak < M1M2 || M5_bak < M5) {
                meshes[0] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
                meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
            } else {
                meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                meshes[1] = String.format("%02d%02d%d%d", M1M2_bak, M3M4, M5_bak, M6);
            }
        } else if (M6_bak > -999) {
            meshes = new String[2];
            if (M3M4_bak < M3M4 || M6_bak < M6) {
                meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
                meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
            } else {
                meshes[0] = String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6);
                meshes[1] = String.format("%02d%02d%d%d", M1M2, M3M4_bak, M5, M6_bak);
            }
        } else {
            meshes = new String[]{String.format("%02d%02d%d%d", M1M2, M3M4, M5, M6)};
        }
        return meshes;
    }

    /**
     * 计算线段所属图幅号，可以在图廓线上
     * 计算原则：1.只要有任意一点在图幅内部,2.两点都在图幅的边线上
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return
     */
    public static String[] line2Meshes(double x1, double y1, double x2, double y2) {
        Set<String> meshes = new HashSet<String>();
        double[] rect = MyGeoConvertor.lineArr2RectArr(new double[]{x1, y1, x2, y2});
        String[] rectMeshes = rect2Meshes(rect[0], rect[1], rect[2], rect[3]);
        LongLine line = new LongLine(new LongPoint(MyGeoConvertor.degree2Millisec(x1), MyGeoConvertor.degree2Millisec
                (y1)), new LongPoint(MyGeoConvertor.degree2Millisec(x2), MyGeoConvertor.degree2Millisec(y2)));
        for (String mesh : rectMeshes) {
            double[] meshRect = mesh2Rect(mesh);
            LongRect longRect = MyGeoConvertor.rectArr2Rect(MyGeoConvertor.degree2Millisec(meshRect));
            if (LongLineUtil.intersectant(line, longRect)) {
                meshes.add(mesh);
            }
        }
        return meshes.toArray(new String[0]);
    }

    /**
     * 给定坐标范围（度），计算范围内包含的图幅
     * 两个点都在同一图幅的图廓线上，会计算属于这个图幅
     *
     * @param lbX 左下经度
     * @param lbY 左下维度
     * @param rtX 右上经度
     * @param rtY 右上维度
     * @return
     */
    public static String[] rect2Meshes(double lbX, double lbY, double rtX, double rtY) {
        // 计算左下坐标位于图幅
        //String lbMesh = null;
        //String[] lbMeshes = point2Meshes(lbX, lbY);
        //if (lbMeshes.length == 1) {
        //    lbMesh = lbMeshes[0];
        //} else if (lbMeshes.length == 2) {
        //    lbMesh = lbMeshes[1];
        //} else {
        //    lbMesh = lbMeshes[2];
        //}
        //// 计算右上坐标位于图幅
        //String rtMesh = point2Meshes(rtX, rtY)[0];
        //if (lbMesh.equals(rtMesh)) {
        //    return new String[]{lbMesh};
        //} else {
        //    // 跨多个图幅
        //    return MeshUtils.getBetweenMeshes(lbMesh, rtMesh);
        //}
        String lbMesh = null;
        String rtMesh = null;

        // 计算左下坐标位于图幅
        String[] lbMeshes = point2Meshes(lbX, lbY);
        // 计算右上坐标位于图幅
        String[] rtMeshes = point2Meshes(rtX, rtY);

        // 左下坐标处于单个图幅内
        if (lbMeshes.length == 1) {
            lbMesh = lbMeshes[0];
            rtMesh = rtMeshes[0];
        // 左下坐标处于两个图幅共用线
        } else if (lbMeshes.length == 2) {
            // 右上坐标处于单个图幅内
            if (rtMeshes.length == 1) {
                lbMesh = lbMeshes[1];
                rtMesh = rtMeshes[0];
            // 右上坐标处于两个图幅共用线(两点同时为双图幅)
            } else if (rtMeshes.length == 2) {
                // 左下、右上坐标的图幅号完全相等
                if (Arrays.equals(lbMeshes, rtMeshes)) {
                    lbMesh = lbMeshes[0];
                    rtMesh = rtMeshes[1];
                } else {
                    for (String m : Arrays.asList(lbMeshes)) {
                        if (Arrays.asList(rtMeshes).contains(m)) {
                            lbMesh = rtMesh = m;
                            break;
                        }
                    }
                    // 左下、右上坐标的图幅号完全不相等（左下点处于左下左上图幅, 右上点处于右下右上图幅）
                    if (StringUtils.isEmpty(lbMesh) || StringUtils.isEmpty(rtMesh)) {
                        lbMesh = lbMeshes[0];
                        rtMesh = rtMeshes[1];
                    }
                }
            // 右上坐标处于四个图幅共用线
            } else if (rtMeshes.length == 4) {
                lbMesh = lbMeshes[0];
                for (String m : lbMeshes) {
                    if (!lbMesh.equals(m))
                        rtMesh = m;
                }
            }
        // 左下坐标处于四个图幅共用线
        } else if (lbMeshes.length == 4) {
            // 右上坐标处于单个图幅内
            if (rtMeshes.length == 1) {
                lbMesh = lbMeshes[2];
                rtMesh = rtMeshes[0];
            // 右上坐标处于两个图幅共用线
            } else if (rtMeshes.length == 2) {
                rtMesh = rtMeshes[1];
                for (String m : rtMeshes) {
                    if (!rtMesh.equals(m))
                        lbMesh = m;
                }
            // 右上坐标处于四个图幅共用线
            } else if (rtMeshes.length == 4) {
                // TODO ..暂未实现
                lbMesh = lbMeshes[2];
                rtMesh = rtMeshes[0];
            }
        }

        if (lbMesh.equals(rtMesh)) {
            // 未跨图幅
            return new String[]{lbMesh};
        } else {
            // 跨多个图幅
            return MeshUtils.getBetweenMeshes(lbMesh, rtMesh);
        }
    }

    /**
     * @param meshId
     * @return rect:[minx,miny,maxx,maxy]
     */
    public static double[] mesh2Rect(String meshId) {
        meshId = StringUtils.leftPad(meshId, 6, '0');
        int m12 = Integer.valueOf(meshId.substring(0, 2));
        int m34 = Integer.valueOf(meshId.substring(2, 4));
        int m5 = Integer.valueOf(meshId.substring(4, 5));
        int m6 = Integer.valueOf(meshId.substring(5, 6));
        double[] rect = new double[4];
        rect[0] = (m34 + 60) + DoubleUtil.keepSpecDecimal(m6 / 8.0);
        rect[2] = (m34 + 60) + DoubleUtil.keepSpecDecimal((m6 + 1) / 8.0);
        //纬度拉伸1.5之后成为m12，则还原时除以1.5
        int intLat = m12 / 3;
        int modLat = m12 % 3;
        rect[1] = intLat * 2 + DoubleUtil.keepSpecDecimal((modLat * 8 + m5) / 12.0);
        rect[3] = intLat * 2 + DoubleUtil.keepSpecDecimal((modLat * 8 + m5 + 1) / 12.0);
        return rect;
    }

    /**
     * 根据图幅号计算左下/中心坐标点/右上坐标点
     *
     * @param meshId
     * @return 左下/中心坐标点/右上坐标点
     */
    public static int[] mesh2Location(String meshId) {

        if (meshId.length() < 6) {
            int length = 6 - meshId.length();
            for (int i = 0; i < length; i++) {
                meshId = "0" + meshId;
            }
        }

        int m1 = Integer.valueOf(meshId.substring(0, 1));
        int m2 = Integer.valueOf(meshId.substring(1, 2));
        int m3 = Integer.valueOf(meshId.substring(2, 3));
        int m4 = Integer.valueOf(meshId.substring(3, 4));
        int m5 = Integer.valueOf(meshId.substring(4, 5));
        int m6 = Integer.valueOf(meshId.substring(5, 6));
        int lbX = (m3 * 10 + m4) * 3600 + m6 * 450 + 60 * 3600;
        int lbY = (m1 * 10 + m2) * 2400 + m5 * 300;
        int cX = lbX + 450 / 2;
        int cY = lbY + 300 / 2;
        int rtX = lbX + 450;
        int rtY = lbY + 300;
        return new int[]{lbX, lbY, cX, cY, rtX, rtY};

    }

    /**
     * 根据图幅号计算左下/中心坐标点/右上坐标点的经纬度
     *
     * @param meshId
     * @return 左下/中心坐标点/右上坐标点经纬度
     */
    public static double[] mesh2LocationLatLon(String meshId) {
        int[] data = mesh2Location(meshId);

        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = second2Decimal(data[i]);
        }

        return result;
    }

    /**
     * 图幅号转换成POLYGON的wkt
     *
     * @param meshId
     * @return
     */
    public static String mesh2WKT(String meshId) {
        int a[] = mesh2Location(meshId);
        double lbX = second2Decimal(a[0]);
        double lbY = second2Decimal(a[1]);
        double rtX = second2Decimal(a[4]);
        double rtY = second2Decimal(a[5]);
        return "POLYGON ((" + lbX + " " + lbY + ", " + lbX + " " + rtY + ", " + rtX + " " + rtY + ", " + rtX + " " +
                lbY + "," + lbX + " " + lbY + "))";

    }

    /**
     * 图幅号转换成POLYGON的JTS对象
     *
     * @param meshId
     * @return
     * @throws ParseException
     */
    public static Geometry mesh2Jts(String meshId) throws ParseException {

        String wkt = mesh2WKT(meshId);
        Geometry jts = new WKTReader().read(wkt);

        return jts;

    }

    public static Geometry linkInterMeshPolygon(Geometry linkGeom, Geometry meshGeom) {
        // 在图幅切分前优先判断是否为图幅线，如果是图幅线则不进行切分
        // 避免含有多个形状点的线被图幅线切分为多个线段
        try {
            if(linkGeom.getCoordinates().length > 2){
                if(linkGeom.coveredBy(meshGeom))
                    return linkGeom;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return meshGeom.intersection(linkGeom);
    }


    /**
     * 暂未实现
     * 给定左下图幅，右上图幅，计算其外接矩形包含图幅
     *
     * @param lbMesh 左下图幅
     * @param rtMesh 右上图幅
     * @return
     */
    public static String[] getBetweenMeshes(String lbMesh, String rtMesh) {
        if (lbMesh.equals(rtMesh)) {
            return new String[]{lbMesh};
        } else {
            // 计算横纵向跨多少个图幅
            int lbM12 = Integer.valueOf(lbMesh.substring(0, 2));
            int lbM34 = Integer.valueOf(lbMesh.substring(2, 4));
            int lbM5 = Integer.valueOf(lbMesh.substring(4, 5));
            int lbM6 = Integer.valueOf(lbMesh.substring(5, 6));
            int rtM12 = Integer.valueOf(rtMesh.substring(0, 2));
            int rtM34 = Integer.valueOf(rtMesh.substring(2, 4));
            int rtM5 = Integer.valueOf(rtMesh.substring(4, 5));
            int rtM6 = Integer.valueOf(rtMesh.substring(5, 6));
            int hSize = (rtM34 - lbM34) * 8 + (rtM6 - lbM6) + 1;
            int vSize = (rtM12 - lbM12) * 8 + (rtM5 - lbM5) + 1;
            int meshSize = hSize * vSize; // 图幅数
            // 从左下角开始计算图幅
            String allMesh[] = new String[meshSize];
            //
            int targetM12 = 0;
            int targetM34 = 0;
            int targetM5 = 0;
            int targetM6 = 0;
            for (int v = 0; v < vSize; v++) {
                targetM12 = lbM12 + ((lbM5 + v) / 8);
                targetM5 = (lbM5 + v) % 8;
                for (int h = 0; h < hSize; h++) {
                    targetM34 = lbM34 + ((lbM6 + h) / 8);
                    targetM6 = (lbM6 + h) % 8;
                    allMesh[v * hSize + h] = String.format("%02d%02d%d%d", targetM12, targetM34, targetM5, targetM6);
                }
            }

            return allMesh;

        }
    }


    /**
     * 经纬度小数形式转换成秒
     *
     * @param x //小数
     * @return
     */
    public static double decimal2Second(double x) {

        return x * 3600;

    }

    /**
     * 经纬度秒转换成小数
     *
     * @param x //秒
     * @return
     */
    public static double second2Decimal(double x) {
        return Double.parseDouble(new java.text.DecimalFormat("#.000000").format(x / 3600));
    }

    public static String getNeighborMesh(String meshId, TopoLocation meshLoc) {
        meshId = StringUtils.leftPad(meshId, 6, '0');
        int m12 = Integer.valueOf(meshId.substring(0, 2));
        int m34 = Integer.valueOf(meshId.substring(2, 4));
        int m5 = Integer.valueOf(meshId.substring(4, 5));
        int m6 = Integer.valueOf(meshId.substring(5, 6));
        switch (meshLoc) {
            case Top:
                if ((++m5) > 7) {
                    m12++;
                    m5 = 0;
                }
                break;
            case Bottom:
                if ((--m5) < 0) {
                    m12--;
                    m5 = 7;
                }
                break;
            case Left:
                if ((--m6) < 0) {
                    m34--;
                    m6 = 7;
                }
                break;
            case Right:
                if ((++m6) > 7) {
                    m34++;
                    m6 = 0;
                }
                break;
            case LeftTop:
                if ((++m5) > 7) {
                    m12++;
                    m5 = 0;
                }
                if ((--m6) < 0) {
                    m34--;
                    m6 = 7;
                }
                break;
            case LeftBottom:
                if ((--m5) < 0) {
                    m12--;
                    m5 = 7;
                }
                if ((--m6) < 0) {
                    m34--;
                    m6 = 7;
                }
                break;
            case RightTop:
                if ((++m5) > 7) {
                    m12++;
                    m5 = 0;
                }
                if ((++m6) > 7) {
                    m34++;
                    m6 = 0;
                }
                break;
            case RightBottom:
                if ((--m5) < 0) {
                    m12--;
                    m5 = 7;
                }
                if ((++m6) > 7) {
                    m34++;
                    m6 = 0;
                }
                break;
            default:
                break;
        }
        return String.format("%02d%02d%d%d", m12, m34, m5, m6);
    }

    /**
     * 计算某图幅周报的9个图幅
     * [0]为本身图幅，从1-8的顺序为左下开始逆时针方向
     *
     * @param meshId
     * @return
     */
    public static String[] get9NeighborMeshes(String meshId) {
        String allMesh[] = new String[9];
        allMesh[0] = meshId;
        allMesh[1] = getNeighborMesh(meshId, TopoLocation.LeftBottom);
        allMesh[2] = getNeighborMesh(meshId, TopoLocation.Bottom);
        allMesh[3] = getNeighborMesh(meshId, TopoLocation.RightBottom);
        allMesh[4] = getNeighborMesh(meshId, TopoLocation.Right);
        allMesh[5] = getNeighborMesh(meshId, TopoLocation.RightTop);
        allMesh[6] = getNeighborMesh(meshId, TopoLocation.Top);
        allMesh[7] = getNeighborMesh(meshId, TopoLocation.LeftTop);
        allMesh[8] = getNeighborMesh(meshId, TopoLocation.Left);
        return allMesh;

    }

    /**
     * 计算1圈的扩圈图幅
     * 使用set可以保证输入和输出不会有重复
     *
     * @param meshSet
     * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
     * @author XXW
     */
    private static Set<String> generateNeighborMeshSet(Set<String> meshSet) {
        Set<String> neiMeshSet = new HashSet<String>();
        for (String meshId : meshSet) {
            CollectionUtils.addAll(neiMeshSet, get9NeighborMeshes(meshId));
        }
        return neiMeshSet;
    }

    /**
     * 计算1圈的扩圈图幅
     * 使用set可以保证输入和输出不会有重复
     *
     * @param meshSet
     * @return 包含自己的外围一圈的图幅Set，无图幅或图幅格式不对，返回null
     * @author XXW
     */
    public static Set<String> getNeighborMeshSet(Set<String> meshSet) {
        //check
        if (meshSet != null) {
            Set<String> checkedMeshSet = new HashSet<String>();
            for (String meshId : meshSet) {
                checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
            }
            return generateNeighborMeshSet(checkedMeshSet);
        }
        return null;
    }

    /**
     * 不检查图幅位数
     * 计算n圈的邻接图幅
     *
     * @param meshId
     * @param extendCount
     * @return
     */
    private static Set<String> generateNeighborMeshSet(String meshId, int extendCount) {
        Set<String> outMeshes = new HashSet<String>();
        if (extendCount == 1) {
            CollectionUtils.addAll(outMeshes, get9NeighborMeshes(meshId));
            return outMeshes;
        } else {
            Set<String> meshes = generateNeighborMeshSet(meshId, extendCount - 1);
            outMeshes = generateNeighborMeshSet(meshes);
            return outMeshes;
        }
    }

    /**
     * 会检查图幅位数
     * 计算n圈的邻接图幅
     *
     * @param meshId
     * @param extendCount
     * @return
     */
    public static Set<String> getNeighborMeshSet(String meshId, int extendCount) {
        if (StringUtils.isNotEmpty(meshId) && extendCount > 0) {
            meshId = StringUtils.leftPad(meshId, 6, '0');
            return generateNeighborMeshSet(meshId, extendCount);
        }
        return null;
    }


    /**
     * 计算n圈的邻接图幅
     *
     * @param meshSet
     * @param extendCount
     * @return
     */
    private static Set<String> generateNeighborMeshSet(Set<String> meshSet, int extendCount) {
        Set<String> outMeshes = new HashSet<String>();
        if (extendCount == 1) {
            outMeshes = generateNeighborMeshSet(meshSet);
            return outMeshes;
        } else {
            Set<String> meshes = generateNeighborMeshSet(meshSet, extendCount - 1);
            outMeshes = generateNeighborMeshSet(meshes);
            return outMeshes;
        }
    }

    /**
     * 计算n圈的邻接图幅
     *
     * @param meshSet
     * @param extendCount
     * @return
     */
    public static Set<String> getNeighborMeshSet(Set<String> meshSet, int extendCount) {
        if (meshSet != null && extendCount > 0) {
            Set<String> checkedMeshSet = new HashSet<String>();
            for (String meshId : meshSet) {
                checkedMeshSet.add(StringUtils.leftPad(meshId, 6, '0'));
            }
            return generateNeighborMeshSet(checkedMeshSet, extendCount);
        }
        return null;
    }

    /**
     * 点是否在图框线上
     *
     * @param dLongitude
     * @param dLatitude
     * @return
     */
    public static boolean isPointAtMeshBorder(double dLongitude, double dLatitude) {
        String[] meshes = point2Meshes(dLongitude, dLatitude);
        if (meshes.length > 1) {
            return true;
        }
        return false;
    }

    /**
     * 点是否在图框线上
     * 编辑过程中，刚开始的时候把坐标都乘以100000，然后我们空间计算的那些类里头，大多数都是使用原始坐标的 ，
     * 这期间要调用的话，要注意一下精度转换
     *
     * @param dLongitude
     * @param dLatitude
     * @return
     */
    public static boolean isPointAtMeshBorderWith100000(double dLongitude, double dLatitude) {
        String[] meshes = point2Meshes(dLongitude * 0.00001, dLatitude * 0.00001);
        if (meshes.length > 1) {
            return true;
        }
        return false;
    }

    /***
     * @author zhaokk
     * @param  g
     * @return 图幅号
     * 判断是否图廓线
     * @throws Exception
     */
    public static boolean isMeshLine(Geometry g) throws Exception {
        Coordinate[] cs = g.getCoordinates();
        if (g.getCoordinates().length == 2) {
            if (isPointAtMeshBorder(cs[0].x, cs[0].y) && isPointAtMeshBorder(cs[1].x, cs[1].y)) {
                if (cs[0].x == cs[1].x || cs[0].y == cs[1].y) {
                    return true;
                }
            }

        }
        return false;
    }

    public static boolean locateMeshBorder(double x, double y, String mesh) {
        TopoLocation loc = TopoLocation.valueOf(meshLocate(x, y, mesh));
        if (loc == TopoLocation.Inside || loc == TopoLocation.Outside) {
            return false;
        }
        return true;
    }

    public static String meshLocate(double x, double y, String mesh) {
        double[] rect = mesh2Rect(mesh);
        if (x < rect[0] || x > rect[2] || y < rect[1] || y > rect[2]) {
            return TopoLocation.Outside.toString();
        }
        if (x == rect[0] && y == rect[1]) {
            return TopoLocation.LeftBottom.toString();
        } else if (x == rect[2] && y == rect[1]) {
            return TopoLocation.RightBottom.toString();
        } else if (x == rect[2] && y == rect[3]) {
            return TopoLocation.RightTop.toString();
        } else if (x == rect[0] && y == rect[3]) {
            return TopoLocation.LeftTop.toString();
        } else if (x == rect[0]) {
            return TopoLocation.Left.toString();
        } else if (y == rect[1]) {
            return TopoLocation.Bottom.toString();
        } else if (x == rect[2]) {
            return TopoLocation.Right.toString();
        } else if (y == rect[3]) {
            return TopoLocation.Top.toString();
        } else {
            return TopoLocation.Inside.toString();
        }
    }

    /**
     * 判断grid是否被一个面包含
     *
     * @param face
     * @param meshId
     * @return
     */
    public static boolean meshInFace(double[] face, String meshId) {
        double[] rect = mesh2Rect(meshId);

        double[] p1 = new double[]{rect[0], rect[1]};
        double[] p2 = new double[]{rect[0], rect[3]};
        double[] p3 = new double[]{rect[2], rect[1]};
        double[] p4 = new double[]{rect[2], rect[3]};

        if (!CompGeometryUtil.pointInFace(p1, face)) {
            return false;
        }
        if (!CompGeometryUtil.pointInFace(p2, face)) {
            return false;
        }
        if (!CompGeometryUtil.pointInFace(p3, face)) {
            return false;
        }
        if (!CompGeometryUtil.pointInFace(p4, face)) {
            return false;
        }

        return true;
    }

    /**
     * grid转wkt
     *
     * @param meshes
     * @return
     * @throws ParseException
     */
    public static Geometry meshes2Jts(Set<String> meshes) {
        if (meshes == null)
            return null;
        Geometry geometry = null;
        for (String mesh : meshes) {

            Geometry geo = JtsGeometryConvertor.convert(mesh);

            if (geometry == null) {
                geometry = geo;
            } else {
                geometry = geometry.union(geo);
            }
        }
        return geometry;
    }

    //rect:[minx,miny,maxx,maxy]
    /**
     * @Title: meshs2Rect
     * @Description: TODO
     * @param meshes
     * @return  double[minx,miny,maxx,maxy] [最小经度,最小维度,最大经度,最大维度]
     * @throws 
     * @author zl zhangli5174@navinfo.com
     * @date 2017年6月6日 下午4:20:55 
     */
    public static double[] meshs2Rect(Set<Integer> meshes){
    	Set<Integer> xSet = new HashSet<Integer>();//所有经度集合
    	Set<Integer> ySet = new HashSet<Integer>();//所有维度集合
    	
    	for(Integer mesh : meshes){
    		String meshStr = mesh.toString();
    		meshStr = StringUtils.leftPad(meshStr, 6, '0');
    		if(meshStr.length() != 6){
    			log.info("不能识别图幅号: "+mesh);
    			continue;
    		}
    		String xStr = meshStr.substring(0, 2)+meshStr.substring(4, 5);
    		String yStr = meshStr.substring(2, 4)+meshStr.substring(5);
    		xSet.add(Integer.parseInt(xStr));
    		ySet.add(Integer.parseInt(yStr));
    	}
    	int[] xArr = null;
    	if(xSet != null && xSet.size() > 0){
    		xArr = getMinMaxInteger(xSet);
    	}
    	int[] yArr = null;
    	if(ySet != null && ySet.size() > 0){
    		yArr = getMinMaxInteger(ySet);
    	}
    	
    	String maxMesh = null;
    	String minMesh = null;
    	
    	if(xArr.length == 2 && yArr.length ==2){
    		String x0Str = xArr[0]+"";
    		String x1Str = xArr[1]+"";
    		
    		String y0Str = yArr[0]+"";
    		String y1Str = yArr[1]+"";
    		
    		minMesh= x0Str.substring(0,2) + y0Str.substring(0, 2)+x0Str.substring(2)+y0Str.substring(2);
    		
    		maxMesh= x1Str.substring(0,2) + y1Str.substring(0, 2)+x1Str.substring(2)+y1Str.substring(2);
    	}
    	
    	double[] minPoint = mesh2Rect(minMesh);
    	
    	double[] maxPoint = mesh2Rect(maxMesh);
    	
//    	System.out.println(minPoint[0]+" , "+minPoint[1]+" , "+maxPoint[2]+" , "+maxPoint[3]);
    	return new double[]{minPoint[0],minPoint[1],maxPoint[2],maxPoint[3]};
    }
    public static int[] getMinMaxInteger(Set<Integer> set){
    	Integer max = Collections.max(set);
    	Integer min = Collections.min(set);
        return new int[]{min, max};
    }

    public static String[] geometry2Mesh(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();

        if (GeometryTypeName.LINESTRING.equals(geometry.getGeometryType())) {
            Set<String> meshes = new HashSet<>();
            for (int index = 0; index < coordinates.length -1; index++) {
                meshes.addAll(Arrays.asList(
                        line2Meshes(coordinates[index].x, coordinates[index].y, coordinates[index + 1].x, coordinates[index + 1].y)));
            }
            return meshes.toArray(new String[]{});
        }
        if (GeometryTypeName.POLYGON.equals(geometry.getGeometryType())) {
            coordinates = geometry.getBoundary().getCoordinates();
            Set<String> meshes = new HashSet<>();
            for (int index = 0; index < coordinates.length -1; index++) {
                String[] array = line2Meshes(coordinates[index].x, coordinates[index].y, coordinates[index + 1].x, coordinates[index + 1].y);
                meshes.addAll(Arrays.asList(array));
            }
            if (meshes.size() == 1) {
                return meshes.toArray(new String[]{});
            } else {
                Iterator<String> iterator = meshes.iterator();
                while (iterator.hasNext()) {
                    try {
                        Geometry mesh = mesh2Jts(iterator.next());
                        String type = geometry.intersection(mesh).getGeometryType();
                        if (GeometryTypeName.LINESTRING.equals(type) || GeometryTypeName.MULTILINESTRING.equals(type)) {
                            iterator.remove();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (meshes.size() > 0) {
                    return meshes.toArray(new String[]{});
                }
            }
        }

        Set<String> meshes = new HashSet<>();
        for (Coordinate coordinate : coordinates) {
            meshes.addAll(Arrays.asList(point2Meshes(coordinate.x, coordinate.y)));
        }
        return meshes.toArray(new String[]{});
    }

    public static void main(String[] args) throws Exception {
        String wkt = "POLYGON ((119.74966 38.60531, 119.75 38.60504, 119.75 38.60529, 119.74966 38.60531))";
        System.out.println(Arrays.toString(MeshUtils.geometry2Mesh(GeoTranslator.wkt2Geometry(wkt))));
    }
}
