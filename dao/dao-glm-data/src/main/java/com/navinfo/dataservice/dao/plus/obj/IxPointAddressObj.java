package com.navinfo.dataservice.dao.plus.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressChildren;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressFlag;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressName;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressNameTone;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressParent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class IxPointAddressObj extends AbstractIxObj {

	public IxPointAddressObj(BasicRow mainrow) {
		super(mainrow);
	}

	@Override
	public String objName() {
		// TODO Auto-generated method stub
		return ObjectName.IX_POINTADDRESS;
	}

	@Override
	public String objType() {
		// TODO Auto-generated method stub
		return ObjType.FEATURE;
	}

	@Override
	public BasicRow createSubRowByTableName(String tableName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasicRow createSubRowByName(String subRowName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BasicRow createSubSubRowByName(String subRowName, long subId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BasicRow> getSubRowByName(String subRowName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<IxPointaddressName> getIxPointaddressNames() {
		return (List) subrows.get(IX_POINTADDRESS_NAME);
	}
	
	
	/**
	 * @Title: isFreshFlag
	 * @Description: 判断点门牌是否是鲜度验证
	 * @return  boolean
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年10月9日 下午4:55:21 
	 */
	public boolean isFreshFlag() {
		log.info("计算鲜度验证");
		IxPointaddress ixPa = (IxPointaddress) this.mainrow;
		log.info("ixPa.getOpType(): "+ixPa.getOpType());
		if (!(ixPa.getOpType().equals(OperationType.UPDATE)))
			return false;
		Map<String, Object> ixPaOldValues = ixPa.getOldValues();
		if (ixPaOldValues != null) {
			for (String key : ixPaOldValues.keySet()) {
				log.info(ixPa.getPid() +"oldvalues key:"+key);
				if (key.equals(IxPointaddress.DPR_NAME) 
						|| key.equals(IxPointaddress.DP_NAME)
						|| key.equals(IxPointaddress.LINK_PID) 
						|| key.equals(IxPointaddress.X_GUIDE) 
						|| key.equals(IxPointaddress.Y_GUIDE) 
						|| key.equals(IxPointaddress.MEMOIRE) 
						|| key.equals(IxPointaddress.GEOMETRY) 
					) {
					return false;
				} else {
					continue;
				}
			}
		}
		
		return true;
	}


	/**
	 * @return
	 * @throws Exception
	 *             创建一个IxPointaddressName对象，完成主键赋值，完成objPid赋值，
	 *             完成并将其写入到IxPointaddress的subrows属性中。
	 *             暂时没有维护IxPointaddressName对象的外键
	 */
	public IxPointaddressName createIxPointaddressName() throws Exception {
		IxPointaddressName ixPointaddressName = (IxPointaddressName) ObjFactory.getInstance()
				.createRow(IX_POINTADDRESS_NAME, this.objPid());
		if (subrows.containsKey(IX_POINTADDRESS_NAME)) {
			subrows.get(IX_POINTADDRESS_NAME).add(ixPointaddressName);
		} else {
			List<BasicRow> ixPointaddressNames = new ArrayList<BasicRow>();
			ixPointaddressNames.add(ixPointaddressName);
			subrows.put(IX_POINTADDRESS_NAME, ixPointaddressNames);
		}
		return ixPointaddressName;
	}

	public List<IxPointaddressFlag> getIxPointaddressFlags() {
		return (List) subrows.get(IX_POINTADDRESS_FLAG);
	}

	/**
	 * @return
	 * @throws Exception
	 *             创建一个IxPointaddressFlag对象，完成主键赋值，完成objPid赋值，
	 *             完成并将其写入到IxPointaddress的subrows属性中。
	 *             暂时没有维护IxPointaddressFlag对象的外键
	 */
	public IxPointaddressFlag createIxPointaddressFlag() throws Exception {
		IxPointaddressFlag ixPointaddressFlag = (IxPointaddressFlag) ObjFactory.getInstance()
				.createRow(IX_POINTADDRESS_FLAG, this.objPid());
		if (subrows.containsKey(IX_POINTADDRESS_FLAG)) {
			subrows.get(IX_POINTADDRESS_FLAG).add(ixPointaddressFlag);
		} else {
			List<BasicRow> ixPointaddressFlags = new ArrayList<BasicRow>();
			ixPointaddressFlags.add(ixPointaddressFlag);
			subrows.put(IX_POINTADDRESS_FLAG, ixPointaddressFlags);
		}
		return ixPointaddressFlag;
	}

	public List<IxPointaddressNameTone> getIxPointaddressNameTones() {
		return (List) subrows.get(IX_POINTADDRESS_NAME_TONE);
	}

	/**
	 * @return
	 * @throws Exception
	 *             创建一个IxPointaddressNameTone对象，完成主键赋值，完成objPid赋值，
	 *             完成并将其写入到IxPointaddress的subrows属性中。
	 *             暂时没有维护IxPointaddressNameTone对象的外键
	 */
	public IxPointaddressNameTone createIxPointaddressNameTone() throws Exception {
		IxPointaddressNameTone ixPointaddressNameTone = (IxPointaddressNameTone) ObjFactory.getInstance()
				.createRow(IX_POINTADDRESS_NAME_TONE, this.objPid());
		if (subrows.containsKey(IX_POINTADDRESS_NAME_TONE)) {
			subrows.get(IX_POINTADDRESS_NAME_TONE).add(ixPointaddressNameTone);
		} else {
			List<BasicRow> ixPointaddressNameTones = new ArrayList<BasicRow>();
			ixPointaddressNameTones.add(ixPointaddressNameTone);
			subrows.put(IX_POINTADDRESS_NAME_TONE, ixPointaddressNameTones);
		}
		return ixPointaddressNameTone;
	}
	
	
	public List<IxPointaddressChildren> getIxPointaddressChildrens(){
		return (List) subrows.get(IX_POINTADDRESS_CHILDREN);
	}
	
	public IxPointaddressChildren createIxPointaddressChildren(long groupId) throws Exception {
		IxPointaddressChildren ixPointaddressChildren = (IxPointaddressChildren) ObjFactory.getInstance()
				.createRow(IX_POINTADDRESS_CHILDREN, this.objPid());
		ixPointaddressChildren.setGroupId(groupId);
		if(subrows.containsKey(IX_POINTADDRESS_CHILDREN)){
			subrows.get(IX_POINTADDRESS_CHILDREN).add(ixPointaddressChildren);
		}else{
			List<BasicRow> ixPointaddressChildrenList = new ArrayList<BasicRow>();
			ixPointaddressChildrenList.add(ixPointaddressChildren);
			subrows.put(IX_POINTADDRESS_CHILDREN, ixPointaddressChildrenList);
		}
		return ixPointaddressChildren;
	}
	
	
	public List<IxPointaddressParent> getIxPointaddressParents(){
		return (List) subrows.get(IX_POINTADDRESS_PARENT);
	}
	
	public IxPointaddressParent createIxPointaddressParent() throws Exception {
		IxPointaddressParent ixPointaddressParent = (IxPointaddressParent) ObjFactory.getInstance()
				.createRow(IX_POINTADDRESS_PARENT, this.objPid());
		if(subrows.containsKey(IX_POINTADDRESS_PARENT)){
			subrows.get(IX_POINTADDRESS_PARENT).add(ixPointaddressParent);
		}else{
			List<BasicRow> ixPointaddressParentList = new ArrayList<BasicRow>();
			ixPointaddressParentList.add(ixPointaddressParent);
			subrows.put(IX_POINTADDRESS_PARENT, ixPointaddressParentList);
		}
		return ixPointaddressParent;
	}
	
	public IxPointaddressName getCHName(){
		List<IxPointaddressName> names = getIxPointaddressNames();
		if(names == null || names.isEmpty()){
			return null;
		}
		for(IxPointaddressName name : names){
			if("CHI".equals(name.getLangCode()) || "CHT".equals(name.getLangCode())){
				return name;
			}
		}
		return null;
	}
	
	public IxPointaddressName getCHIName(){
		List<IxPointaddressName> names = getIxPointaddressNames();
		if(names == null || names.isEmpty()){
			return null;
		}
		for(IxPointaddressName name : names){
			if("CHI".equals(name.getLangCode())){
				return name;
			}
		}
		return null;
	}
	
	
	public IxPointaddressName getCHTName(){
		List<IxPointaddressName> names = getIxPointaddressNames();
		if(names == null || names.isEmpty()){
			return null;
		}
		for(IxPointaddressName name : names){
			if("CHT".equals(name.getLangCode())){
				return name;
			}
		}
		return null;
	}
	
	public IxPointaddressName getENGName(){
		List<IxPointaddressName> names = getIxPointaddressNames();
		if(names == null || names.isEmpty()){
			return null;
		}
		for(IxPointaddressName name : names){
			if("ENG".equals(name.getLangCode())){
				return name;
			}
		}
		return null;
	}

	public static final String IX_POINTADDRESS = "IX_POINTADDRESS";
	public static final String IX_POINTADDRESS_NAME = "IX_POINTADDRESS_NAME";
	public static final String IX_POINTADDRESS_FLAG = "IX_POINTADDRESS_FLAG";
	public static final String IX_POINTADDRESS_NAME_TONE = "IX_POINTADDRESS_NAME_TONE";
	public static final String IX_POINTADDRESS_PARENT = "IX_POINTADDRESS_PARENT";
	public static final String IX_POINTADDRESS_CHILDREN = "IX_POINTADDRESS_CHILDREN";
	
}
