package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;

import net.sf.json.JSONObject;

/**
 * 
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:27 
* @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		
		RdTrafficsignal rdTrafficsignal = this.command.getRdTrafficsignal();
		
		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());

				return null;
			} else {
				boolean isChanged = false;
				if(content.containsKey("location"))
				{
					JSONObject obj = parseContent(content);
					
					isChanged = rdTrafficsignal.fillChangeFields(obj);
				}
				else
				{
					isChanged = rdTrafficsignal.fillChangeFields(content);
				}
				
				if (isChanged) {
					result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
				}
			}
		}

		return null;
	}
	
	/**
	 * 处理location二进制转10进制
	 * @param content
	 * @return
	 */
	private JSONObject parseContent(JSONObject content)
	{
		JSONObject obj = new JSONObject();
		
		Iterator<?> keys = content.keys();
		
		while(keys.hasNext())
		{
			String key = keys.next().toString();
			
			Object value = content.get(key); 
			
			if(key.equals("location"))
			{
				value = Integer.parseInt(content.getString(key),2);
			}
			obj.put(key, value);
		}
		
		return obj;
	}

}
