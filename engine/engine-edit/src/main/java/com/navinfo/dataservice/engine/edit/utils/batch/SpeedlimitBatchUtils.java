package com.navinfo.dataservice.engine.edit.utils.batch;

import java.util.Arrays;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/**
 * @Title: SpeedlimitBatchUtils.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月23日 下午2:14:01
 * @version: v1.0
 */
public class SpeedlimitBatchUtils {

	private final static Integer[] ROAD = new Integer[] { 3, 4, 6, 7 };

	public SpeedlimitBatchUtils() {
	}

	/**
	 * 根据RdLink类型计算速度值
	 * 
	 * @param rdLink
	 * @return int[总速度值， 左侧车道速度值， 右侧车道速度值]</br>
	 * 
	 *         当RdLink为双方向时总速度值为空， 当RdLink为单方向时仅返回总速度值
	 */
	public static int[] calcSpeedLimit(RdLink rdLink, int formOfWay) {
		int speedLimit = 0;
		speedLimit = walkWaySpeed(formOfWay);
		if (0 != speedLimit) {
			if (1 == rdLink.getDirect()) {
				return new int[] { 0, speedLimit, speedLimit };
			} else {
				return new int[] { speedLimit, 0, 0 };
			}
		}

		int leftSpeedLimit = 0;
		int rightSpeedLimit = 0;
		int laneNum, laneLeft, laneRight;

		int kind = rdLink.getKind();
		laneNum = rdLink.getLaneNum();

		// 3,4,6,7级道路
		if (Arrays.asList(ROAD).contains(kind)) {
			// 双方向
			if (1 == rdLink.getDirect()) {
				// 双向车道数不同
				if (0 == laneNum) {
					laneLeft = rdLink.getLaneLeft();
					laneRight = rdLink.getLaneRight();

					leftSpeedLimit = loadSpeedLimit(laneLeft, rdLink.getDirect());
					rightSpeedLimit = loadSpeedLimit(laneRight, rdLink.getDirect());
				} else {
					laneLeft = laneRight = laneNum % 2 == 0 ? laneNum / 2 : (laneNum + 1) / 2;
					// 双向车道数相同
					leftSpeedLimit = rightSpeedLimit = loadSpeedLimit(laneLeft, rdLink.getDirect());
				}
			} else {
				speedLimit = loadSpeedLimit(laneNum, rdLink.getDirect());
			}
		} else {
			speedLimit = loadOtherSpeedLimit(kind);
		}

		if (1 == rdLink.getUrban()) {
			speedLimit = speedLimit >= 50 ? speedLimit - 10 : speedLimit;
			leftSpeedLimit = leftSpeedLimit >= 50 ? leftSpeedLimit - 10 : leftSpeedLimit;
			rightSpeedLimit = rightSpeedLimit >= 50 ? rightSpeedLimit - 10 : rightSpeedLimit;
		}

		return new int[] { speedLimit, leftSpeedLimit, rightSpeedLimit };
	}

	/**
	 * RdLink的Kind为8，9，10，11，13时获取速度值
	 * 
	 * @param kind
	 * @return
	 */
	private static int loadOtherSpeedLimit(int kind) {
		int speedLimit = 0;
		if (8 == kind) {
			speedLimit = 15;
		} else if (9 == kind) {
			speedLimit = 15;
		} else if (10 == kind) {
			speedLimit = 10;
		} else if (11 == kind) {
			speedLimit = 10;
		} else if (13 == kind) {
			speedLimit = 15;
		}
		return speedLimit;
	}

	/**
	 * RdLink的Kind为3，4，6，7时根据laneNum获取速度值
	 * 
	 * @param laneNum
	 * @return
	 */
	private static int loadSpeedLimit(int laneNum, int direct) {
		int speedLimit = 0;
		if (1 == laneNum) {
			speedLimit = 1 == direct ? 50 : 60;
		} else if (2 == laneNum) {
			speedLimit = 1 == direct ? 60 : 70;
		} else if (3 == laneNum) {
			speedLimit = 1 == direct ? 70 : 80;
		} else if (4 <= laneNum) {
			speedLimit = 80;
		}
		return speedLimit;
	}

	/**
	 * 判断是否为私道、区域内道路、步行街中的一种
	 * 
	 * @param forms
	 * @return
	 */
	private static int walkWaySpeed(int formOfWay) {
		if (18 == formOfWay)
			return 15;
		if (20 == formOfWay)
			return 10;
		if (52 == formOfWay)
			return 15;
		return 0;
	}
}
