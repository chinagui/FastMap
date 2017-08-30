package com.navinfo.dataservice.engine.edit.utils.batch;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkSpeedlimit;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: SpeedUtils
 * @Package: com.navinfo.dataservice.engine.edit.utils.batch
 * @Description: 新增/上下线分离/制作辅路时计算道路限速值
 * @Author: Crayeres
 * @Date: 04/25/17
 * @Version: V1.0
 */
public final class SpeedUtils {

    /**
     * 日志记录
     */
    private final static Logger LOGGER = Logger.getLogger(SpeedUtils.class);

    /**
     * 顺向限速值
     */
    private static Integer FROM_SPEED_LIMIT;

    /**
     * 逆向限速值
     */
    private static Integer TO_SPEED_LIMIT;

    /**
     * 城市道路
     */
    private final static Integer IS_URBAN = 1;

    /**
     * 双方向
     */
    private final static Integer BOTH_DIRECTION = 1;

    /**
     * 顺方向
     */
    private final static Integer FORWARD_DIRECTION = 2;

    /**
     * 逆方向
     */
    private final static Integer REVERSE_DIRECTION = 3;

    /**
     * 基础速度
     */
    private final static Integer BASIC_SPEED = 150;

    /**
     * 默认限速来源
     */
    private final static Integer SPEED_LIMIT_SRC = 9;

    private SpeedUtils(){
    }

    public static void init(){
        FROM_SPEED_LIMIT = TO_SPEED_LIMIT = 0;
    }
    /**
     * 计算道路限速值并更新
     * @param link 道路对象
     */
    public static void updateLinkSpeed(RdLink link){
        init();

        RdLinkSpeedlimit speedlimit = null;
        for (IRow row : link.getSpeedlimits()) {
            RdLinkSpeedlimit it = (RdLinkSpeedlimit) row;
            if (0 == it.getSpeedType()) {
                speedlimit = it;
            }
        }
        if (null == speedlimit) {
            final RdLinkSpeedlimit linkSpeedlimit = new RdLinkSpeedlimit();
            linkSpeedlimit.setLinkPid(link.pid());
            link.setSpeedlimits(new ArrayList<IRow>(){{add(linkSpeedlimit);}});
        }

        int kind = link.getKind();
        int urban = link.getUrban();
        int direct = link.getDirect();
        LOGGER.debug("updateLinkSpeed:[KIND:" + kind + ", URBAN:" + urban + ",DIRECT:" + direct +"]");

        // 非引导道路（9级路）
        if (SpeedEnum.NO_GUIDE_ROAD.getKindOrForm() == kind) {
            fillSpeedLimit(SpeedEnum.NO_GUIDE_ROAD, direct);
        // 步行道路（10级路）
        } else if (SpeedEnum.WALKING_ROAD.getKindOrForm() == kind) {
            fillSpeedLimit(SpeedEnum.WALKING_ROAD, direct);
        // 私道
        } else if (checkLinkForm(link, SpeedEnum.PRIVATE_ROAD)) {
            fillSpeedLimit(SpeedEnum.PRIVATE_ROAD, direct);
        // 步行街属性/ 人渡
        } else if (checkLinkForm(link, SpeedEnum.WALKINT_STREET) || SpeedEnum.PEOPLE_FERRY.getKindOrForm() == kind) {
            fillSpeedLimit(SpeedEnum.WALKINT_STREET, direct);
        // 轮渡
        } else if (SpeedEnum.FERRY.getKindOrForm() == kind) {
            fillSpeedLimit(SpeedEnum.FERRY, direct);
        // 区域内道路属性
        } else if (checkLinkForm(link, SpeedEnum.REGIONAL_ROAD)) {
            fillSpeedLimit(SpeedEnum.REGIONAL_ROAD, direct);
        } else if (SpeedEnum.HIGH_SPEED_ROAD.getKindOrForm() != kind && SpeedEnum.CITY_HIGH_SPEED.getKindOrForm() != kind) {
            int laneNum = link.getLaneNum();
            // 3 4 6 7 级道路
            if (checkLinkKind(kind)) {
                if (0 == laneNum) {
                    FROM_SPEED_LIMIT = calcSpeedLimit(link.getLaneLeft(), direct, urban);
                    TO_SPEED_LIMIT = calcSpeedLimit(link.getLaneRight(), direct, urban);
                    // 校验限速来源与限速值关系
                    checkLimitSrc(speedlimit);
                } else {
                    // 双方向道路车道数为laneNum的一半
                    if (BOTH_DIRECTION == direct) {
                        laneNum = 0 == laneNum % 2 ? laneNum / 2 : 1 + laneNum / 2;
                    }

                    int speedLimit = calcSpeedLimit(laneNum, direct, urban);
                    FROM_SPEED_LIMIT = REVERSE_DIRECTION != direct ? speedLimit : 0;
                    TO_SPEED_LIMIT = FORWARD_DIRECTION != direct ? speedLimit : 0;
                }
            // 8级道路
            } else if (8 == kind) {
                if (REVERSE_DIRECTION != direct) {
                    FROM_SPEED_LIMIT = BASIC_SPEED;
                }
                if (FORWARD_DIRECTION != direct) {
                    TO_SPEED_LIMIT = BASIC_SPEED;
                }
            }
        } else {
            return;
        }
        updateData(speedlimit);
    }

    /**
     * 更新限速信息
     * @param speedlimit 道路限速信息
     */
    private static void updateData(RdLinkSpeedlimit speedlimit) {
        LOGGER.debug("updateData:[FROM_SPEED_LIMIT:" + FROM_SPEED_LIMIT + ", TO_SPEED_LIMIT:" + TO_SPEED_LIMIT + "]");
        speedlimit.setFromSpeedLimit(FROM_SPEED_LIMIT);
        speedlimit.setToSpeedLimit(TO_SPEED_LIMIT);
        speedlimit.setFromLimitSrc(SPEED_LIMIT_SRC);
        speedlimit.setToLimitSrc(SPEED_LIMIT_SRC);

        int minSpeedlimit = FROM_SPEED_LIMIT < TO_SPEED_LIMIT ?
                0 == FROM_SPEED_LIMIT ? TO_SPEED_LIMIT : FROM_SPEED_LIMIT :
                0 == TO_SPEED_LIMIT ? FROM_SPEED_LIMIT : TO_SPEED_LIMIT;

        speedlimit.setSpeedClass(calcSpeedClass(minSpeedlimit));

        if (0 == FROM_SPEED_LIMIT) {
            speedlimit.setFromLimitSrc(0);
        }
        if (0 == TO_SPEED_LIMIT) {
            speedlimit.setToLimitSrc(0);
        }
    }

    /**
     * 计算限速等级
     * @param minSpeedlimit 较低道路限速值
     * @return 限速等级
     */
    private static int calcSpeedClass(int minSpeedlimit) {
        int speedClass = 0;
        if (minSpeedlimit > 1300) {
            speedClass = 1;
        } else if (minSpeedlimit > 1000) {
            speedClass = 2;
        } else if (minSpeedlimit > 900) {
            speedClass = 3;
        } else if (minSpeedlimit > 700) {
            speedClass = 4;
        } else if (minSpeedlimit > 500) {
            speedClass = 5;
        } else if (minSpeedlimit > 300) {
            speedClass = 6;
        } else if (minSpeedlimit > 110) {
            speedClass = 7;
        } else if (minSpeedlimit > 0) {
            speedClass = 8;
        } else {
            LOGGER.error("calcSpeedClass:[minSpeedLimit is error, minSpeedlimit:" + minSpeedlimit + "]");
        }
        LOGGER.debug("calcSpeedClass:[speedClass:" + speedClass + "]");
        return speedClass;
    }

    /**
     * 3）	对于双方向link，若存在非“未调查”、“匝道未调查”的速度限制来源，则速度限制等级值标识赋“手工赋值”；否则，赋“程序赋值”
     * @param speedlimit LINK限速对象
     */
    private static void checkLimitSrc(RdLinkSpeedlimit speedlimit) {
        int fromLimitSrc = speedlimit.getFromLimitSrc();
        int toLimitSrc = speedlimit.getToLimitSrc();

        LOGGER.debug("checkLimitSrc:[fromLimitSrc" + fromLimitSrc + ", toLimitSrc:" + toLimitSrc
                + ",FROM_SPEED_LIMIT:" + FROM_SPEED_LIMIT +", TO_SPEED_LIMIT:" + TO_SPEED_LIMIT + "]");

        if (1 == fromLimitSrc && 9 == toLimitSrc) {
            TO_SPEED_LIMIT = FROM_SPEED_LIMIT;
        } else if (1 == toLimitSrc && 9 == fromLimitSrc) {
            FROM_SPEED_LIMIT = TO_SPEED_LIMIT;
        }

        LOGGER.debug("checkLimitSrc:[FROM_SPEED_LIMIT:" + FROM_SPEED_LIMIT +", TO_SPEED_LIMIT:" + TO_SPEED_LIMIT + "]");
    }

    /**
     * 计算道路限速值
     * @param laneNum 车道数
     * @param direct 道路方向
     * @param urban 是否城市道路
     * @return 计算后限速值
     */
    private static int calcSpeedLimit(Integer laneNum, Integer direct, Integer urban) {
        int speedlimit = 0;
        if (1 == laneNum) {
            if (BOTH_DIRECTION == direct) {
                speedlimit = 500;
            } else {
                speedlimit = 600;
            }
        } else if (2 == laneNum) {
            if (BOTH_DIRECTION == direct) {
                speedlimit = 600;
            } else {
                speedlimit = 700;
            }
        } else if (3 == laneNum) {
            if (BOTH_DIRECTION == direct) {
                speedlimit = 700;
            } else {
                speedlimit = 800;
            }
        } else if (4 == laneNum) {
            speedlimit = 800;
        }
        speedlimit = IS_URBAN == urban ? speedlimit - 100 : speedlimit;

        LOGGER.debug("calcSpeedLimit:[speedlimit:" + speedlimit + "]");

        return speedlimit;
    }

    /**
     * 校验道路种别
     * @param kind 道路种别
     * @return true:3 4 6 7级道路, false:其他级别道路
     */
    private static boolean checkLinkKind(Integer kind) {
        return 3 == kind || 4 == kind || 6 == kind || 7 == kind;
    }

    /**
     * 校验道路形态
     * @param link 道路
     * @param speedEnum 道路形态
     * @return true:包含对应形态, false:不包含
     */
    private static boolean checkLinkForm(RdLink link, SpeedEnum speedEnum) {
        boolean flag = false;
        List<IRow> forms = link.getForms();
        for (IRow row : forms) {
            RdLinkForm form = (RdLinkForm) row;
            if (speedEnum.getKindOrForm() == form.getFormOfWay()) {
                flag = true;
                break;
            }
        }
        LOGGER.debug("checkLinkForm:[flag:" + flag + "]");

        return flag;
    }

    /**
     * 计算限速值
     * @param speedEnum 道路形态信息
     * @param direct 道路方向
     */
    private static void fillSpeedLimit(SpeedEnum speedEnum, Integer direct) {
        if (FORWARD_DIRECTION != direct) {
            TO_SPEED_LIMIT = speedEnum.getSpeedValue();
        }
        if (REVERSE_DIRECTION != direct) {
            FROM_SPEED_LIMIT = speedEnum.getSpeedValue();
        }
        LOGGER.debug("fillSpeedLimit:[FROM_SPEED_LIMIT:" + FROM_SPEED_LIMIT + ", TO_SPEED_LIMIT:" + TO_SPEED_LIMIT + "]");
    }

    private enum SpeedEnum {

        /**
         * NO_GUIDE_ROAD        非引导道路   KIND
         * WALKING_ROAD         步行道路     KIND
         * PRIVATE_ROAD         私道        FORM
         * WALKINT_STREET       步行街      FORM
         * PEOPLE_FERRY         人渡        KIND
         * FERRY                轮渡        KIND
         * REGIONAL_ROAD        区域内道路   FORM
         * HIGH_SPEED_ROAD      高速道路     KIND
         * CITY_HIGH_SPEED      城市高速     KIND
         */
        NO_GUIDE_ROAD(9, 150), WALKING_ROAD(10, 100), PRIVATE_ROAD(18, 150), WALKINT_STREET(20, 100), PEOPLE_FERRY(11, 100),
        FERRY(13, 150), REGIONAL_ROAD(52, 150), HIGH_SPEED_ROAD(1, 0), CITY_HIGH_SPEED(2, 0);

        SpeedEnum(Integer kindOrForm, Integer speedValue) {
            this.kindOrForm = kindOrForm;
            this.speedValue = speedValue;
        }

        /**
         * 种别OR形态值
         */
        private Integer kindOrForm;

        /**
         * 对应限速值
         */
        private Integer speedValue;

        /**
         * Getter method for property <tt>kindOrForm</tt>.
         *
         * @return property value of kindOrForm
         */
        public Integer getKindOrForm() {
            return kindOrForm;
        }

        /**
         * Getter method for property <tt>speedValue</tt>.
         *
         * @return property value of speedValue
         */
        public Integer getSpeedValue() {
            return speedValue;
        }
    }
}

