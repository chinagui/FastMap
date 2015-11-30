package com.navinfo.dataservice.expcore.source.parameter.converter;

import java.util.List;


/**
 * User: liuqing
 * Date: 2010-9-29
 * Time: 13:19:53
 */
public interface ParamConverter {

    public List<String> convert(List<String> params);
}
