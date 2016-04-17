package com.navinfo.navicommons.geo.computation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.navicommons.database.QueryRunner;

import oracle.spatial.geometry.JGeometry;

/** 
* @ClassName: CompGridUtil 
* @author Xiao Xiaowen 
* @date 2016年4月11日 下午9:31:51 
* @Description: TODO
*/
public class CompGridUtil {

	public static Set<String> intersectGeometryGrid(JGeometry geo,String[] meshIds)throws Exception{
		Set<String> grids = new HashSet<String>();
		int type = geo.getType();
		if(type==1){
			double[] point = geo.getPoint();
			CollectionUtils.addAll(grids, point2Grid(point[0],point[1],meshIds));
		}else if(type==2){
			grids.addAll(intersectLineGrid(geo,meshIds));
		}else if(type == 3){
			//...
		}
		return grids;
	}
	public static Set<String> intersectGeometryGrid(JGeometry geo,String meshId)throws Exception{
		Set<String> grids = new HashSet<String>();
		int type = geo.getType();
		if(type==1){
			double[] point = geo.getPoint();
			grids.add(point2Grid(point[0],point[1]));
		}else if(type==2){
			double[] lines = geo.getOrdinatesArray();
			int pointCount = lines.length/2;
			for(int i=1;i<pointCount;i++){
				grids.addAll(intersectLineGrid(new double[]{lines[i*2-2],lines[i*2-1],lines[i*2],lines[i*2+1]},meshId));
			}
		}else if(type == 3){
			//...
		}
		return grids;
	}
	/**
	 * 传入线的几何，和所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshIds：
	 * @return
	 */
	private static Set<String> intersectLineGrid(JGeometry line,String[] meshIds)throws Exception{
		if(meshIds!=null&&meshIds.length>0){
			if(meshIds.length==1){
				return intersectGeometryGrid(line,meshIds[0]);
			}else if(meshIds.length==2){
				//
				Set<String> grids = new HashSet<String>();
				double[] points = line.getOrdinatesArray();
				int m5_0 = Integer.valueOf(meshIds[0].substring(4, 5));
				int m6_0 = Integer.valueOf(meshIds[0].substring(5, 6));
				int m5_1 = Integer.valueOf(meshIds[1].substring(4, 5));
				int m6_1 = Integer.valueOf(meshIds[1].substring(5, 6));
				if(m5_0==m5_1){
					int m7_s = point2Grid_M7(points[1]);
					int m7_e = point2Grid_M7(points[points.length-1]);
					int m7_min = m7_s>m7_e?m7_e:m7_s;
					int m7_max = m7_s>m7_e?m7_s:m7_e;
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						for(int i = m7_min;i<=m7_max;i++){
							grids.add(meshIds[0]+i+"3");
							grids.add(meshIds[1]+i+"0");
						}
					}else{
						for(int i = m7_min;i<=m7_max;i++){
							grids.add(meshIds[0]+i+"0");
							grids.add(meshIds[1]+i+"3");
						}
					}
				}else if(m6_0==m6_1){
					int m8_s = point2Grid_M8(points[0]);
					int m8_e = point2Grid_M8(points[points.length-2]);
					int m8_min = m8_s>m8_e?m8_e:m8_s;
					int m8_max = m8_s>m8_e?m8_s:m8_e;
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						for(int i = m8_min;i<=m8_max;i++){
							grids.add(meshIds[0]+"3"+i);
							grids.add(meshIds[1]+"0"+i);
						}
					}else{
						for(int i = m8_min;i<=m8_max;i++){
							grids.add(meshIds[0]+"0"+i);
							grids.add(meshIds[1]+"3"+i);
						}
					}
				}
				return grids;
			}else{
				throw new Exception("");
			}
		}else{
			throw new Exception("");
		}
	}
	/**
	 * 传入该line所属的图幅号
	 * @param line:[x1,y1,x2,y2]
	 * @param meshId：line所属的图幅号
	 * @return
	 */
	public static Set<String> intersectLineGrid(double[] line,String meshId)throws Exception{
		//计算line的外接矩形相交的grid矩形
		String[] rawGrids = intersectRectGrid(CompGeometryUtil.line2Rect(line),meshId);
		Set<String> interGrids = new HashSet<String>();
		//再计算line是否和每个grid矩形相交
		for(String gridId:rawGrids){
			double[] grid = grid2Rect(gridId);
			if(CompGeometryUtil.intersectLineRect(line,grid)){
				interGrids.add(gridId);
			}
		}
		return interGrids;
	}
	/**
	 * 计算rect所在图幅内相交的grid，rect范围超过图幅范围会抛出异常
	 * @param rect:[minx,miny,maxx,maxy]
	 * @param meshId:rect所属的图幅号
	 * @return
	 * @throws Exception
	 */
	public static String[] intersectRectGrid(double[] rect,String meshId)throws Exception{
		//计算矩形左下角点所在的grid矩形
		String lbGrid = point2Grid(rect[0],rect[1]);
		//计算矩形右上角点所在的grid矩形
		String rtGrid =  point2Grid(rect[2],rect[3]);
		//判断两次算的grid所在的图幅是否和meshId一致
		if(meshId.equals(lbGrid.substring(0,6))&&
				meshId.equals(rtGrid.substring(0,6))){
			//算法是左下grid的第7位和右上grid的第7位数字之间及左下grid的第8位和右上grid的第8位数字之间都相交
			int lbGridM7 = Integer.valueOf(lbGrid.substring(6, 7));
			int lbGridM8 = Integer.valueOf(lbGrid.substring(7, 8));
			int rtGridM7 = Integer.valueOf(rtGrid.substring(6, 7));
			int rtGridM8 = Integer.valueOf(rtGrid.substring(7, 8));
			if(rtGridM7>lbGridM7||rtGridM8>lbGridM8){
				int size = (rtGridM7-lbGridM7+1)*(rtGridM8-lbGridM8+1);
				String[] grids = new String[size];
				int index = 0;
				for(int i=lbGridM7;i<=rtGridM7;i++){
					for(int j=lbGridM8;j<=rtGridM8;j++){
						grids[index]=meshId+i+j;
					}
				}
				return grids;
			}else{
				return new String[]{lbGrid};
			}
		}else{
			throw new Exception("参数错误：rect超过图幅。");
		}
	}
	/**
	 * 根据grid号获取grid的矩形
	 * @param gridId
	 * @return [minx,miny,maxx,maxy]
	 */
	public static double[] grid2Rect(String gridId){
		int m1 = Integer.valueOf(gridId.substring(0, 1));
		int m2 = Integer.valueOf(gridId.substring(1, 2));
		int m3 = Integer.valueOf(gridId.substring(2, 3));
		int m4 = Integer.valueOf(gridId.substring(3, 4));
		int m5 = Integer.valueOf(gridId.substring(4, 5));
		int m6 = Integer.valueOf(gridId.substring(5, 6));
		int m7 = Integer.valueOf(gridId.substring(6, 7));
		int m8 = Integer.valueOf(gridId.substring(7, 8));
		double minx = (m3 * 10 + m4) + (m6 * 450 + m8*450/4.0)/3600 + 60;
		double miny = ((m1 * 10 + m2) * 2400 + m5 * 300 + m7*300/4)/3600.0;
//		double rtX = lbX+1/(8*4);
		double maxx = minx+0.03125;
		double maxy = miny+1/(12*4);
		return new double[]{minx,miny,maxx,maxy};
	}

	/**
	 * 计算点所属的grid号
	 * @param x
	 * @param y
	 * @param meshId:点所属的图幅号
	 * @return 8位grid号码字符串
	 */
	public static String[] point2Grid(double x,double y,String[] meshIds){
		String[] grids = null;
		if(meshIds!=null&&meshIds.length>1){
			if(meshIds.length==2){
				grids = new String[2];
				int m5_0 = Integer.valueOf(meshIds[0].substring(4, 5));
				int m6_0 = Integer.valueOf(meshIds[0].substring(5, 6));
				int m5_1 = Integer.valueOf(meshIds[1].substring(4, 5));
				int m6_1 = Integer.valueOf(meshIds[1].substring(5, 6));
				if(m5_0==m5_1){
					int m7 = point2Grid_M7(y);
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						grids[0]=meshIds[0]+m7+"3";
						grids[1]=meshIds[1]+m7+"0";
					}else{
						grids[0]=meshIds[0]+m7+"0";
						grids[1]=meshIds[1]+m7+"3";
					}
				}else if(m6_0==m6_1){
					int m8 = point2Grid_M8(x);
					if(Integer.valueOf(meshIds[0])<Integer.valueOf(meshIds[1])){
						grids[0]=meshIds[0]+"3"+m8;
						grids[1]=meshIds[1]+"0"+m8;
					}else{
						grids[0]=meshIds[0]+"0"+m8;
						grids[1]=meshIds[1]+"3"+m8;
					}
				}
			}else if(meshIds.length==4){
				grids = new String[4];
				List<Integer> sortMesh = new ArrayList<Integer>();
				for(int i=0;i<4;i++){
					sortMesh.add(Integer.valueOf(meshIds[i]));
				}
				Collections.sort(sortMesh);
				grids[0]=sortMesh.get(0)+"33";
				grids[3]=sortMesh.get(3)+"00";
				if(sortMesh.get(2)%10==sortMesh.get(0)%10){
					grids[1]=sortMesh.get(1)+"30";
					grids[2]=sortMesh.get(2)+"03";
				}else{
					grids[1]=sortMesh.get(2)+"30";
					grids[2]=sortMesh.get(1)+"03";
				}
			}
			return grids;
		}else{
			return new String[]{point2Grid(x,y)};
		}
	}
	/**
	 * 计算点所属的grid号
	 * @param x
	 * @param y
	 * @param meshId:点所属的图幅号
	 * @return 8位grid号码字符串
	 */
	public static String point2Grid(double x,double y,String meshId){
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
		
		int m7 = (int)((y*3600-lbY)/75);
		int m8 = (int)((x*3600-lbX)/112.5);
		
		StringBuffer sb = new StringBuffer();
		sb.append(meshId);
		sb.append(m7);
		sb.append(m8);
		return sb.toString();
	}
	/**
	 * 计算点所在的grid号
	 * @param x
	 * @param y
	 * @return
	 */
	public static int point2Grid_M7(double y){
		int M7;

		
		//double yt = y*3600/300;
		double yt = y*12.0;
		
		M7 = (int)((yt-(int)yt)*4.0);
		return M7;
	}
	/**
	 * 计算点所在的grid号
	 * @param x
	 * @param y
	 * @return
	 */
	public static int point2Grid_M8(double x){
		int M6;
		int M8;
		double xt = (x-(int)x) * 8.0;
		M6 = (int) xt;
		M8 = (int)((xt-M6)*4.0);
		return M8;
	}
	/**
	 * 计算点所在的grid号
	 * @param x
	 * @param y
	 * @return
	 */
	public static String point2Grid(double x,double y){
		int M1M2;
		int M3M4;
		int M5;
		int M6;
		int M7;
		int M8;

		M1M2 = (int) (y * 1.5);
		M3M4 = ((int)x) - 60;
		
		//double yt = y*3600/300;
		double yt = y*12.0;
		M5 = ((int)yt)%8;
		double xt = (x-(int)x) * 8.0;
		M6 = (int) xt;
		
		M7 = (int)((yt-(int)yt)*4.0);
		M8 = (int)((xt-M6)*4.0);
		StringBuilder builder = new StringBuilder();
		builder.append(M1M2);
		builder.append(M3M4);
		builder.append(M5);
		builder.append(M6);
		builder.append(M7);
		builder.append(M8);
		String id = builder.toString();
		if (id.length() == 7) {
			id = "0" + id;
		}
		return id;
	}
	
	
/* test part*/
	private static void t1(){
		//595671:116.125 39.91667,116.25 40
		//0.03125 0.0208s
		System.out.println(point2Grid(116.0087,39.890));
		System.out.println(point2Grid(116.0087,39.890,"595660"));
	}
	private static void t2(){
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.4.131:1521/orcl", "TEMP_XXW_01", "TEMP_XXW_01").getConnection();
			QueryRunner runn = new QueryRunner();
			String sql = "SELECT GEOMETRY FROM RD_link WHERE ROWNUM=1";
			JGeometry geo = runn.query(conn, sql, new ResultSetHandler<JGeometry>(){

				@Override
				public JGeometry handle(ResultSet rs) throws SQLException {
					rs.next();
					try{
						JGeometry geo = JGeometry.load(rs.getBytes("GEOMETRY"));
						return  geo;
					}catch(Exception e){
						throw new SQLException(e.getMessage(),e);
					}
				}
				
			});
			System.out.println(geo.getPoint());
			System.out.println(geo.getOrdinatesArray());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		t2();
	}
}
