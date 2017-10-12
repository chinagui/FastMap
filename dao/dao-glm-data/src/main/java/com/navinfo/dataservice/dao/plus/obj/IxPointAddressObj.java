package com.navinfo.dataservice.dao.plus.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.navinfo.dataservice.dao.plus.model.basic.BasicRow;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddress;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressFlag;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressName;
import com.navinfo.dataservice.dao.plus.model.ixpointaddress.IxPointaddressNameTone;

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
		IxPointaddress ixPa = (IxPointaddress) this.mainrow;
		if (!(ixPa.getOpType().equals(OperationType.UPDATE)))
			return false;
		Map<String, Object> ixPaOldValues = ixPa.getOldValues();
		if (ixPaOldValues != null) {
			for (String key : ixPaOldValues.keySet()) {
				if (key.equals(IxPointaddress.DPR_NAME) 
						|| key.equals(IxPointaddress.DP_NAME)
						|| key.equals(IxPointaddress.LINK_PID) 
						|| key.equals(IxPointaddress.X_GUIDE) 
						|| key.equals(IxPointaddress.Y_GUIDE) 
						|| key.equals(IxPointaddress.MEMOIRE) 
						|| key.equals(IxPointaddress.GEOMETRY) 
						|| key.equals(IxPointaddress.MEMO) 
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

	public static final String IX_POINTADDRESS = "IX_POINTADDRESS";
	public static final String IX_POINTADDRESS_NAME = "IX_POINTADDRESS_NAME";
	public static final String IX_POINTADDRESS_FLAG = "IX_POINTADDRESS_FLAG";
	public static final String IX_POINTADDRESS_NAME_TONE = "IX_POINTADDRESS_NAME_TONE";
}
