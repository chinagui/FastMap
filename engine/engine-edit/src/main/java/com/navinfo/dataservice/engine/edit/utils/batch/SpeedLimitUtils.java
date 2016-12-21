package com.navinfo.dataservice.engine.edit.utils.batch;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.Scope.row;

/**
 * @author zhangyt
 * @Title: SpeedLimitUtils.java
 * @Description: TODO
 * @date: 2016年8月23日 下午3:15:34
 * @version: v1.0
 */
public class SpeedLimitUtils {

    public SpeedLimitUtils() {
    }

    public static void updateRdLink(RdLink rdLink, JSONObject json, Result result) {
        if (json.containsKey("speedlimits")) {
            return;
        }
        boolean kindChange = false, laneChange = false, directChange = false, formChange = false, urbanChange = false;
        RdLink link = new RdLink();
        link.copy(rdLink);

        int speedLimit[] = new int[]{0, 0, 0};
        if (json.containsKey("kind")) {
            link.setKind(json.getInt("kind"));
            kindChange = true;
        }

        if (json.containsKey("direct")) {
            link.setDirect(json.getInt("direct"));
            directChange = true;
        }

        if (json.containsKey("urban")) {
            link.setUrban(json.getInt("urban"));
            urbanChange = true;
        }

        if (json.containsKey("laneNum")) {
            link.setLaneNum(json.getInt("laneNum"));
            laneChange = true;
        }
        if (json.containsKey("laneLeft")) {
            link.setLaneLeft(json.getInt("laneLeft"));
            laneChange = true;
        }
        if (json.containsKey("laneRight")) {
            link.setLaneRight(json.getInt("laneRight"));
            laneChange = true;
        }

        // 如果link为私道/区域内道路/步行街中任意形态不做限速值维护
        List<IRow> rows = link.getForms();
        if (json.containsKey("forms")) {
            JSONArray array = json.getJSONArray("forms");
            for (Object obj : array.toArray()) {
                JSONObject jsonObj = (JSONObject) obj;
                RdLinkForm form = new RdLinkForm();
                try {
                    form.Unserialize(jsonObj);
                } catch (Exception e) {
                }
                if (jsonObj.containsKey("objStatus")) {
                    if (jsonObj.getString("objStatus").equals(OperType.UPDATE.toString())) {
                        String rowId = jsonObj.getString("rowId");
                        Iterator<IRow> iterator = rows.iterator();
                        while (iterator.hasNext())
                            if (iterator.next().rowId().equals(rowId))
                                iterator.remove();
                        rows.add(form);
                    } else if (jsonObj.getString("objStatus").equals(OperType.DELETE.toString())) {
                        String rowId = jsonObj.getString("rowId");
                        Iterator<IRow> iterator = rows.iterator();
                        while (iterator.hasNext())
                            if (iterator.next().rowId().equals(rowId))
                                iterator.remove();
                    } else {
                        rows.add(form);
                    }
                }
            }
        }
        if (null != rows && !rows.isEmpty()) {
            for (IRow row : rows) {
                if (isWalkigWay(((RdLinkForm) row).getFormOfWay())) {
                    return;
                }
            }
        }

        int newForm = 0;
        if (json.containsKey("forms")) {
            JSONArray formsArray = json.getJSONArray("forms");
            if (null != formsArray) {
                @SuppressWarnings("unchecked") Iterator<JSONObject> iterator = formsArray.iterator();
                JSONObject formJSON = null;
                while (iterator.hasNext()) {
                    formJSON = iterator.next();
                    if (formJSON.containsKey("objStatus")) {
                        String objStatus = formJSON.getString("objStatus");
                        if (ObjStatus.UPDATE.equals(Enum.valueOf(ObjStatus.class, objStatus))) {
                            if (formJSON.containsKey("formOfWay") && isWalkigWay(formJSON.getInt("formOfWay"))) {
                                formChange = true;
                                if (0 == newForm || newForm < formJSON.getInt("formOfWay")) {
                                    newForm = formJSON.getInt("formOfWay");
                                }
                                break;
                            }
                        }
                    } else {
                        if (formJSON.containsKey("formOfWay") && isWalkigWay(formJSON.getInt("formOfWay"))) {
                            formChange = true;
                            if (0 == newForm || newForm < formJSON.getInt("formOfWay")) {
                                newForm = formJSON.getInt("formOfWay");
                            }
                            break;
                        }
                    }
                }
            }
        }

        // A变化
        if (kindChange) {
            int oldKindValue = rdLink.getKind();
            int newKindValue = link.getKind();

            // B变化
            if (laneChange) {
                if (directChange) {
                    int oldDirect = rdLink.getDirect();
                    int newDirect = link.getDirect();

                    if (formChange) {
                        // ABCD变 E不变
                        if (!urbanChange) {
                            if (changeInnerA1A2OrA1A2(oldKindValue, newKindValue) || changeA1OrA2ToA3(oldKindValue, newKindValue) || changeA3ToA1OrA2(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // ABC变 DE不变
                        if (!urbanChange) {
                            if (changeInnerA1A2OrA1A2(oldKindValue, newKindValue) || (changeA3ToA1OrA2(oldKindValue, newKindValue) && directPairToSingle(oldDirect, newDirect)) || changeA1OrA2ToA3(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    }
                } else {
                    if (formChange) {
                        // ABD变 CE不变
                        if (!urbanChange) {
                            if ((changeInnerA1OrA1A2(oldKindValue, newKindValue)) || changeA1OrA2ToA3(oldKindValue, newKindValue) || changeA3ToA1OrA2(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // AB变 CDE不变
                        if (!urbanChange) {
                            if (changeInnerA1A2OrA1A2A3(oldKindValue, newKindValue) || changeHSToA1(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    }
                }
                // B不变化
            } else {
                if (directChange) {
                    // AC变 BDE不变
                    if (!formChange && !urbanChange) {
                        int oldDirect = rdLink.getDirect();
                        int newDirect = link.getDirect();
                        if ((changeInnerA1A2OrA1A2A3(oldKindValue, newKindValue) && (directPairToSingle(oldDirect, newDirect) || directSingleToPair(oldDirect, newDirect))) || changeA1ToHS(oldKindValue, newKindValue)) {
                            speedLimit = calcSpeedLimit(link, newForm);
                        }
                    }
                } else {
                    if (formChange) {
                        // AD变 BCE不变
                        if (!urbanChange) {
                            if (changeInnerA1A2OrA1A2(oldKindValue, newKindValue) || changeA1OrA2ToA3(oldKindValue, newKindValue) || changeA3ToA1OrA2(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // A变 BCDE不变
                        if (!urbanChange) {
                            if (changeInA1A2A3(oldKindValue, newKindValue) || changeInA2(oldKindValue, newKindValue) || changeHSToA1(oldKindValue, newKindValue)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        } else {
                            // AE变 BCD不变
                        }
                    }
                }
            }
            // A不变
        } else {
            if (laneChange) {
                if (directChange) {
                    if (formChange) {
                        // BCD变 AE不变
                        if (!urbanChange) {
                            if (isContain(rdLink.getKind(), A1, A2, A3)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // BC变 ADE不变
                        if (!urbanChange) {
                            speedLimit = calcSpeedLimit(link, newForm);
                        }
                    }
                } else {
                    if (formChange) {
                        // BD变 ACE不变
                        if (!urbanChange) {
                            if (isContain(rdLink.getKind(), A1, A2)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // B变 ACDE不变
                        if (isContain(rdLink.getKind(), A1)) {
                            speedLimit = calcSpeedLimit(link, newForm);
                        } else if (isContain(rdLink.getKind(), A2, A3)) {
                            // 限速值保持不变
                        }
                    }
                }
            } else {
                if (directChange) {
                    if (formChange) {
                        // CD变 ABE不变
                        if (!urbanChange) {
                            if (isContain(rdLink.getKind(), A1, A2)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // C变 ABDE不变
                        if (!urbanChange) {
                            if (isContain(rdLink.getKind(), A1, A2)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    }
                } else {
                    if (formChange) {
                        // D变 ABCE不变
                        if (!urbanChange) {
                            if (isContain(rdLink.getKind(), A1, A2)) {
                                speedLimit = calcSpeedLimit(link, newForm);
                            }
                        }
                    } else {
                        // E变 ABCD不变
                        if (isContain(rdLink.getKind(), A1) && urbanChange)
                            speedLimit = calcSpeedLimit(link, newForm);
                    }
                }
            }
        }

        // 更新限速值
        for (Integer speed : speedLimit) {
            if (0 == speed)
                continue;
            for (IRow row : rdLink.getSpeedlimits()) {
                RdLinkSpeedlimit limit = (RdLinkSpeedlimit) row;
                if (0 == limit.getSpeedType()) {
                    if (0 != speedLimit[0]) {
                        if (link.getDirect() != 3)
                            limit.changedFields().put("fromSpeedLimit", speedLimit[0]);
                        if (link.getDirect() != 2)
                            limit.changedFields().put("toSpeedLimit", speedLimit[0]);
                    } else {
                        if (link.getDirect() != 3)
                            limit.changedFields().put("fromSpeedLimit", speedLimit[1]);
                        if (link.getDirect() != 2)
                            limit.changedFields().put("toSpeedLimit", speedLimit[2]);
                    }
                    result.insertObject(limit, ObjStatus.UPDATE, limit.parentPKValue());
                }
            }
        }
    }

    /**
     * 判断车道是否发生变化
     *
     * @param laneNum
     * @param laneLeft
     * @param laneRight
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean laneChange(Object laneNum, Object laneLeft, Object laneRight) {
        return null != laneNum || null != laneLeft || null != laneRight;
    }

    /**
     * 判断是否为私道、区域内道路、步行街中的一种
     *
     * @param formOfWay
     * @return
     */
    private static boolean isWalkigWay(int formOfWay) {
        return 18 == formOfWay || 20 == formOfWay || 52 == formOfWay;
    }

    /**
     * 判断urban属性
     *
     * @param rdLink
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean isUrban(RdLink rdLink) {
        return rdLink.getUrban() == 1;
    }

    private final static Integer[] A1 = new Integer[]{3, 4, 6, 7};
    private final static Integer[] A2 = new Integer[]{8, 9, 11, 13};
    private final static Integer[] A3 = new Integer[]{10};

    /**
     * 在A1A2区间变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInA1A2(int oldValue, int newValue) {
        if (isContain(oldValue, A1))
            if (isContain(newValue, A2))
                return true;
        if (isContain(oldValue, A2))
            if (isContain(newValue, A1))
                return true;
        return false;
    }

    /**
     * 在A1A2A3区间变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInA1A2A3(int oldValue, int newValue) {
        return isInnerChange(oldValue, newValue) == 1;
    }

    /**
     * 在A1内部变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInA1(int oldValue, int newValue) {
        return isInnerChange(oldValue, newValue) == 2;
    }

    /**
     * 在A2内部变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInA2(int oldValue, int newValue) {
        return isInnerChange(oldValue, newValue) == 3;
    }

    /**
     * 由A1变为高速
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeA1ToHS(int oldValue, int newValue) {
        return isInnerChange(oldValue, newValue) == 4;
    }

    /**
     * 由高速变为A1
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeHSToA1(int oldValue, int newValue) {
        return isInnerChange(oldValue, newValue) == 5;
    }

    /**
     * A1变为A2/A3
     *
     * @return
     */
    private static boolean changeA1OrA2ToA3(int oldValue, int newValue) {
        if (isContain(oldValue, A1, A2))
            if (isContain(newValue, A3))
                return true;
        return false;
    }

    /**
     * A3变为A1/A2
     *
     * @return
     */
    private static boolean changeA3ToA1OrA2(int oldValue, int newValue) {
        if (isContain(oldValue, A3))
            if (isContain(newValue, A1, A2))
                return true;
        return false;
    }

    /**
     * 在A1/A2内部变化或A1A2A3之间变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInnerA1A2OrA1A2A3(int oldValue, int newValue) {
        return changeInA1(oldValue, newValue) || changeInA2(oldValue, newValue) || changeInA1A2A3(oldValue, newValue);
    }

    /**
     * 在A1/A2内部变化或A1A2之间变化
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInnerA1A2OrA1A2(int oldValue, int newValue) {
        return changeInA1(oldValue, newValue) || changeInA2(oldValue, newValue) || changeInA1A2(oldValue, newValue);
    }

    /**
     * 在A1内部或A1A2之间
     *
     * @param oldValue
     * @param newValue
     * @return
     */
    private static boolean changeInnerA1OrA1A2(int oldValue, int newValue) {
        return changeInA1(oldValue, newValue) || changeInA1A2(oldValue, newValue);
    }

    /**
     * @param oldValue
     * @param newValue
     * @return 0： 不符合，1： A1A2A3内部变化， 2： A1内部变化</br>
     * 3： A2内部变化， 4： A1->高速， 5： 高速->A1
     */
    private static int isInnerChange(int oldValue, int newValue) {
        if (isContain(oldValue, A1)) {
            if (isContain(newValue, A2, A3)) {
                return 1;
            } else if (isContain(newValue, A1)) {
                return 2;
            } else if (1 == newValue) {
                return 4;
            }
        } else if (isContain(oldValue, A2)) {
            if (isContain(newValue, A1, A3)) {
                return 1;
            } else if (isContain(newValue, A2)) {
                return 3;
            }
        } else if (isContain(oldValue, A3)) {
            if (isContain(newValue, A1, A2)) {
                return 1;
            }
        }
        if (1 == oldValue) {
            if (isContain(newValue, A1)) {
                return 5;
            }
        }
        return 0;
    }

    /**
     * 双方向变为单方向
     *
     * @param oldDirect
     * @param newDirect
     * @return
     */
    private static boolean directPairToSingle(int oldDirect, int newDirect) {
        return (oldDirect == 1) && (newDirect == 2 || newDirect == 3);
    }

    /**
     * 单方向变为双方向
     *
     * @param oldDirect
     * @param newDirect
     * @return
     */
    private static boolean directSingleToPair(int oldDirect, int newDirect) {
        return (oldDirect == 2 || oldDirect == 3) && (newDirect == 1);
    }

    /**
     * 方向逆转
     *
     * @param oldDirect
     * @param newDirect
     * @return
     */
    @SuppressWarnings("unused")
    private static boolean directReversion(int oldDirect, int newDirect) {
        return (oldDirect == 2 && newDirect == 3) || (oldDirect == 3 && newDirect == 2);
    }

    /**
     * 判断value是否包含在数组内
     *
     * @param value
     * @param arrays
     * @return
     */
    private static boolean isContain(int value, Integer[]... arrays) {
        for (Integer[] arr : arrays) {
            if (Arrays.asList(arr).contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        isContain(8, A2);
    }

    /**
     * 根据RdLink类型计算速度值
     *
     * @param rdLink
     * @return int[总速度值， 左侧车道速度值， 右侧车道速度值]</br>
     * <p>
     * 当RdLink为双方向时总速度值为空， 当RdLink为单方向时仅返回总速度值
     */
    private static int[] calcSpeedLimit(RdLink rdLink, int formOfWay) {
        int speedLimit = 0;
        speedLimit = walkWaySpeed(formOfWay);
        if (0 != speedLimit) {
            if (1 == rdLink.getDirect()) {
                return new int[]{0, speedLimit, speedLimit};
            } else {
                return new int[]{speedLimit, 0, 0};
            }
        }

        int leftSpeedLimit = 0;
        int rightSpeedLimit = 0;
        int laneNum, laneLeft, laneRight;

        int kind = rdLink.getKind();
        laneNum = rdLink.getLaneNum();

        // 3,4,6,7级道路
        if (Arrays.asList(A1).contains(kind)) {
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

        return new int[]{speedLimit * 10, leftSpeedLimit * 10, rightSpeedLimit * 10};
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
     * @param formOfWay
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
