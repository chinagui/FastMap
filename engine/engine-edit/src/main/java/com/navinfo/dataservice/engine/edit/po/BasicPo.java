package com.navinfo.dataservice.engine.edit.po;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** 
 * @ClassName: BasicPo
 * @author xiaoxiaowen4127
 * @date 2016年7月22日
 * @Description: BasicPo.java
 */
public class BasicPo {
	
	protected int pid;
	protected Map<String,Object> oldValues=null;
	
	public Map<String,Object> getAttrs(){
		Map<String,Object> attrs = new HashMap<String,Object>();
		return attrs;
	}
	public Object getAttrByColNae(String colName){
		return null;
	}
	
	private void addOldValue(String colName,Object value){
		if(oldValues==null){
			oldValues = new HashMap<String,Object>();
		}
		oldValues.put(colName, value);
	}
	public <T> void setAttrByCol(String colName,T newValue)throws Exception{
		//colName->getter
		String getter="getPid";
		Method methodGetter = this.getClass().getMethod(getter);
		Object oldValue = methodGetter.invoke(this);
		if(oldValue==null&&newValue==null)return;
		if(oldValue!=null&&oldValue.equals(newValue))return;
		//save old value
		addOldValue(colName,newValue);
		//
		//colName->methodName
		String setter="setPid";
		Class[] argtypes = null;
		if(newValue instanceof Integer){
			argtypes= new Class[]{int.class};
		}else if(newValue instanceof Double){
			argtypes = new Class[]{double.class};
		}else if(newValue instanceof Boolean){
			argtypes= new Class[]{boolean.class};
		}else if(newValue instanceof Float){
			argtypes= new Class[]{float.class};
		}else if(newValue instanceof Long){
			argtypes= new Class[]{long.class};
		}else{
			argtypes = new Class[]{newValue.getClass()};
		}
		Method method = this.getClass().getMethod(setter,argtypes);
		method.invoke(this, newValue);
	}
	public void insert(){
		
	}
	public void update(){
		
	}
	public void delete(){
		
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public static void main(String[] args) {
		try{
			BasicPo po = new BasicPo();
			po.setAttrByCol("na", 1);
			System.out.println(po.getPid());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
