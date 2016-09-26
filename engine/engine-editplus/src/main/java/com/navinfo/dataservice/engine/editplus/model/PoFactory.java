package com.navinfo.dataservice.engine.editplus.model;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.editplus.bo.ad.AdLinkBo;

@Deprecated
public class PoFactory {

	private volatile static PoFactory instance;

	public static PoFactory getInstance() {
		if (instance == null) {
			synchronized (PoFactory.class) {
				if (instance == null) {
					instance = new PoFactory();
				}
			}
		}
		return instance;
	}

	private PoFactory() {

	}
	
	

	public <T> T get(Connection conn, T po, boolean isLock){
//		if(po instanceof BasicObj){
//			Map<String,Object> attrs = ((BasicObj) po).getAttrs();
//			for(String key:attrs.keySet()){
//				if(attrs.get(key)!=null){
//					
//				}
//			}
//			
//		}else{
//			
//		}
		return null;
	}

	public <T,R> T get(Connection conn, Class<T> targetClass, R refPo, boolean isLock){
		return null;
	}
	
	public <T> T create(Class<T> clazz){
		return null;
	}
	
	
	public <T,R> List<T> list(Connection conn, Class<T> targetClass, R refPo, boolean isLock){
		return null;
	}
	
	public static void main(String[] args) {
		Connection conn = null;
		AdLinkBo bo = PoFactory.getInstance().get(conn,new AdLinkBo(), true);
	}
	
}
