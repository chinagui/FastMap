package com.navinfo.dataservice.expcore.source.parameter.converter;

import java.util.List;


/**
 * User: liuqing
 * Date: 2010-9-29
 * Time: 13:13:03
 */
public class DoNothingParamConverter implements ParamConverter {

	@Override
    public List<String> convert(List<String> params) {

        return params;
    }
}
