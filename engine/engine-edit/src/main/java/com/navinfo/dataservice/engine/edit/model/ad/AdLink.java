package com.navinfo.dataservice.engine.edit.model.ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.edit.model.AbstractLink;
import com.navinfo.dataservice.engine.edit.model.BasicObj;
import com.navinfo.dataservice.engine.edit.model.BasicRow;

/** 
 * @ClassName: AdLink
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: AdLink.java
 */
public class AdLink extends AbstractLink {

	private int kind = 1;
	private int form = 1;
	private int scale;
	private int editFlag = 1;
	private List<BasicRow> meshes = new ArrayList<BasicRow>();
	
	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getEditFlag() {
		return editFlag;
	}

	public void setEditFlag(int editFlag) {
		this.editFlag = editFlag;
	}

	public List<BasicRow> getMeshes() {
		return meshes;
	}

	public void setMeshes(List<BasicRow> meshes) {
		this.meshes = meshes;
	}

	@Override
	public String primaryKey() {
		return "LINK_PID";
	}

	@Override
	public Map<Class<? extends BasicRow>, List<BasicRow>> childRows() {
		Map<Class<? extends BasicRow>,List<BasicRow>> childList = new HashMap<>();
		childList.put(AdLinkMesh.class, meshes);
		return childList;
	}

	@Override
	public Map<Class<? extends BasicObj>, List<BasicObj>> childObjs() {
		return null;//没有子对象，返回null;
	}


	@Override
	public String tableName() {
		return "AD_LINK";
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.edit.model.BasicRow#objType()
	 */
	@Override
	public ObjType objType() {
		return ObjType.ADLINK;
	}

}
