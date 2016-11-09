package com.navinfo.dataservice.dao.glm.iface;

public enum SpecialMapType {
	
	// 卡车限制信息
	rdLinkLimitTruck,
	// link限制信息数量（普通限制信息）
	rdLinkLimit,
	// 普通线限速限速等级
	rdlinkSpeedlimitSpeedClass,
	// 普通线限速限速等级赋值标识
	rdlinkSpeedlimitSpeedClassWork,
	// 普通线限速限速来源
	rdlinkSpeedlimitSpeedLimitSrc,
	// link车道等级
	rdLinkLaneClass,
	// link功能等级
	rdLinkFunctionClass,
	// 车道数（总数）
	rdLinkLaneNum;
}
