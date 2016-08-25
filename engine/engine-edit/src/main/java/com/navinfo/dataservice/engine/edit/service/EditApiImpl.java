package com.navinfo.dataservice.engine.edit.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.engine.edit.operation.Transaction;

import net.sf.json.JSONObject;

/**
 * editApi的实现类
* @ClassName: EditApiImpl 
* @author Zhang Xiaolong
* @date 2016年8月24日 下午7:16:24 
* @Description: TODO
 */
@Service("editApi")
public class EditApiImpl implements EditApi {

	@Override
	public JSONObject run(JSONObject dataObj) throws Exception {
		Transaction t = new Transaction(dataObj.toString());

		String msg = t.run();

		String log = t.getLogs();

		JSONObject json = new JSONObject();

		json.put("result", msg);

		json.put("log", log);

		json.put("check", t.getCheckLog());

		json.put("pid", t.getPid());

		return json;
	}
}
