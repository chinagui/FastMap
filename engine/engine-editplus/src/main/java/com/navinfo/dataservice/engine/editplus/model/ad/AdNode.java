package com.navinfo.dataservice.engine.editplus.model.ad;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.editplus.model.AbstractNode;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: AdNode
 * @author xiaoxiaowen4127
 * @date 2016年8月18日
 * @Description: AdNode.java
 */
public class AdNode extends AbstractNode {

	@Override
	public String primaryKey() {
		return "NODE_PID";
	}

	@Override
	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
		return null;
	}

	@Override
	public Map<Class<? extends BasicObj>, List<BasicObj>> childObjs() {
		return null;
	}


	@Override
	public String tableName() {
		return "AD_NODE";
	}


	@Override
	public ObjectType objType() {
		return ObjectType.AD_NODE;
	}

}
