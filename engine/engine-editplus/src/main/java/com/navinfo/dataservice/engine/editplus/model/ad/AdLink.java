package com.navinfo.dataservice.engine.editplus.model.ad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.engine.editplus.model.AbstractLink;
import com.navinfo.dataservice.engine.editplus.model.BasicRow;
import com.navinfo.dataservice.engine.editplus.model.obj.BasicObj;
import com.navinfo.dataservice.engine.editplus.model.obj.ObjectType;

/** 
 * @ClassName: AdLink
 * @author xiaoxiaowen4127
 * @date 2016年8月17日
 * @Description: AdLink.java
 */
public class AdLink extends AbstractLink {
	
	public AdLink(long objPid) {
		super(objPid);
		// TODO Auto-generated constructor stub
	}

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
	public String tableName() {
		return "AD_LINK";
	}
}
