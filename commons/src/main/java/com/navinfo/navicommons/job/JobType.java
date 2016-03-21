package com.navinfo.navicommons.job;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-8
 */
public enum JobType
{
    imp,
    exp;

    public static boolean validate(String type)
    {
        return (imp.name().equals(type)
                || exp.name().equals(type));
    }
}
