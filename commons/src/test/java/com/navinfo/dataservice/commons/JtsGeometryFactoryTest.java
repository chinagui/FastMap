package com.navinfo.dataservice.commons;

import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/** 
* @ClassName: MyThread 
* @author Xiao Xiaowen 
* @date 2017年8月5日 下午12:07:31 
* @Description: TODO
*/
public class JtsGeometryFactoryTest implements Runnable{
	private int index = 0;
	public JtsGeometryFactoryTest(int index){
		this.index=index;
	}
	@Override
	public void run() {
		try{
//			testCreateGeometry();
			testWriteWkt();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	private void testRead()throws Exception{
		int count =0 ;
		String wkt = "POINT("+index+" 23)";
		long t = System.currentTimeMillis();
		while(true){
			JtsGeometryFactory.read(wkt);
			if(count++%10000==0){
				System.out.println(Thread.currentThread().getName()+(System.currentTimeMillis()-t));
				t = System.currentTimeMillis();
			}
		}
	}
	private void testCreateGeometry()throws Exception{
		int count =0 ;
		Coordinate coor = new Coordinate(index,30);
		long t = System.currentTimeMillis();
		while(true){
			JtsGeometryFactory.createPoint(coor);
			if(count++%10000==0){
				System.out.println(Thread.currentThread().getName()+(System.currentTimeMillis()-t));
				t = System.currentTimeMillis();
			}
		}
	}

	private void testWriteWkt()throws Exception{
		int count =0 ;
		Coordinate coor = new Coordinate(index,30);
		Geometry point = JtsGeometryFactory.createPoint(coor);
		long t = System.currentTimeMillis();
		while(true){
			JtsGeometryFactory.writeWKT(point);
			if(count++%10000==0){
				System.out.println(Thread.currentThread().getName()+(System.currentTimeMillis()-t));
				t = System.currentTimeMillis();
			}
		}
	}
    public static void main(String[] args) {
		for(int i=0;i<100;i++){
			new Thread(new JtsGeometryFactoryTest(i)).start();
		}
//    	new MyThread(1).run();
	}
}
