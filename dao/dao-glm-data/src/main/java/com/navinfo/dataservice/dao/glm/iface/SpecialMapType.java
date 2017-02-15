package com.navinfo.dataservice.dao.glm.iface;

public enum SpecialMapType {

	// 卡车限制信息
	rdLinkLimitTruck(501),
	// link限制信息数量（普通限制信息）
	rdLinkLimit(502),
	// 普通线限速限速等级
	rdlinkSpeedlimitSpeedClass(503),
	// 普通线限速限速等级赋值标识
	rdlinkSpeedlimitSpeedClassWork(504),
	// 普通线限速限速来源
	rdlinkSpeedlimitSpeedLimitSrc(505),
	// link车道等级
	rdLinkLaneClass(506),
	// link功能等级
	rdLinkFunctionClass(507),
	// 车道数（总数）
	rdLinkLaneNum(508),
	// 开发状态
	rdLinkDevelopState(509),
	// 上下线分离
	rdLinkMultiDigitized(510),
	// 铺设状态
	rdLinkPaveStatus(511),
	// 收费信息
	rdLinkTollInfo(512),
	// 特殊交通
	rdLinkSpecialTraffic(513),
	// 高架
	rdLinkIsViaduct(514),
	// 供用信息
	rdLinkAppInfo(515),
	// 交叉口内道路
	rdLinkForm50(516),
	// 道路名内容
	rdLinkNameContent(517),
	// 道路名组数
	rdLinkNameGroupid(518),
	// 名称类型
	rdLinkNameType(519),
	// 条件线限速个数
	rdlinkSpeedlimitConditionCount(520),
	// 禁止穿行
	rdLinkLimitType3(521),
	// IC
	rdLinkFormOfWay10(522),
	// JCT
	rdLinkFormOfWay11(523),
	// SA
	rdLinkFormOfWay12(524),
	// PA
	rdLinkFormOfWay13(525),
	// 全封闭道路
	rdLinkFormOfWay14(526),
	// 匝道
	rdLinkFormOfWay15(527),
	// 跨线天桥
	rdLinkFormOfWay16(528),
	// 跨线地道
	rdLinkFormOfWay17(529),
	// 步行街
	rdLinkFormOfWay20(530),
	// 隧道
	rdLinkFormOfWay31(531),
	// 环岛
	rdLinkFormOfWay33(532),
	// 辅路
	rdLinkFormOfWay34(533),
	// 调头口
	rdLinkFormOfWay35(534),
	// POI连接路
	rdLinkFormOfWay36(535),
	// 提右
	rdLinkFormOfWay37(536),
	// 提左
	rdLinkFormOfWay38(537),
	// 主辅路出入口
	rdLinkFormOfWay39(538),
	// 道路维修中
	rdLinkLimitType0(539),
	// 外地车限行
	rdLinkLimitType8(540),
	// 尾号限行
	rdLinkLimitType9(541),
	// 在建
	rdLinkLimitType10(542),
	// 车辆限制
	rdLinkLimitType2(543),
	// 季节性关闭道路
	rdLinkLimitType5(544),
	// Usage fee required
	rdLinkLimitType6(545),
	// 超车限制
	rdLinkLimitType7(546),
	// 单行限制
	rdLinkLimitType1(547),
	// Rtic等级
	rdLinkRticRank(548),
	// IntRtic等级
	rdLinkIntRticRank(549),
	// ZONE类型
	rdLinkZoneTpye(550),
	// ZONE个数
	rdLinkZoneCount(551),
	// link的左右ZONE号码
	rdLinkZoneSide(552),	
	// link属性
	rdLinkProperty(553);

	private int value;

	// 定义get方法返回数据
	public int getValue() {
		return value;
	}

	private SpecialMapType(int value) {
		this.value = value;
	}

}
