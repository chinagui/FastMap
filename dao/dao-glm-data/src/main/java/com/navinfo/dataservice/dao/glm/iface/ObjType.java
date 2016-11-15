package com.navinfo.dataservice.dao.glm.iface;

/**
 * 对象类型
 */
public enum ObjType {

    RDDIRECTROUTE,
    RDDIRECTROUTEVIA,
    RDLINK,
    RDNODE,
    RDRESTRICTION,
    RDLINKFORM,
    RDLINKLIMIT,
    RDLINKNAME,
    RDRESTRICTIONVIA,
    RDRESTRICTIONDETAIL,
    RDRESTRICTIONCONDITION,
    RDNODEFORM,
    RDNODENAME,
    RDNODEMESH,
    RDCROSS,
    RDCROSSLINK,
    RDCROSSNODE,
    RDCROSSNAME,
    RDLINKLIMITTRUNK,
    RDLINKSPEEDLIMIT,
    RDLINKSIDEWALK,
    RDLINKWALKSTAIR,
    RDLINKRTIC,
    RDLINKINTRTIC,
    RDLINKZONE,
    RDSPEEDLIMIT,
    RDLANECONNEXITY,
    RDLANETOPOLOGY,
    RDLANEVIA,
    RDBRANCH,
    RDBRANCHDETAIL,
    RDBRANCHNAME,
    RDBRANCHREALIMAGE,
    RDBRANCHSCHEMATIC,
    RDSIGNBOARD,
    RDSIGNBOARDNAME,
    RDSIGNASREAL,
    RDSERIESBRANCH,
    RDBRANCHVIA,
    RDTRAFFICSIGNAL,
    RDVOICEGUIDE,
    RDVOICEGUIDEDETAIL,
    RDVOICEGUIDEVIA,
    RDWARNINGINFO,
    RDCROSSWALK,
    RDCROSSWALKINFO,
    RDCROSSWALKNODE,
    RDHGWGLIMIT,
    RDMILEAGEPILE,

    ADFACE,
    ADADMIN,
    ADADMINGROUP,
    ADADMINGNAME,
    ADADMINDETAIL,
    ADADMINGPART,
    RDGSC,
    RDGSCLINK,
    ADFACETOPO,
    ADLINK,
    ADLINKMESH,
    ADNODE,
    ADNODEMESH,

    RWLINK,
    RWLINKNAME,
    RWNODE,
    RWNODEMESH,
    RWFEATURE,


    IXPOI,
    IXPOINAME,
    IXPOINAMEFLAG,
    IXPOIADDRESS,
    IXPOICONTACT,
    IXPOIFLAG,
    IXPOIENTRYIMAGE,
    IXPOIICON,
    IXPOINAMETONE,
    IXPOIPHOTO,
    IXPOIAUDIO,
    IXPOIVIDEO,
    IXPOIPARENT,
    IXPOICHILDREN,
    IXPOIBUILDING,
    IXPOIDETAIL,
    IXPOIBUSINESSTIME,
    IXPOIINTRODUCTION,
    IXPOIADVERTISEMENT,
    IXPOIGASSTATION,
    IXPOICHARGINGSTATION,
    IXPOICHARGINGPLOT,
    IXPOICHARGINGPLOTPH,
    IXPOIPARKING,
    IXPOIATTRACTION,
    IXPOIHOTEL,
    IXPOIRESTAURANT,
    IXPOITOURROUTE,
    IXPOIEVENT,
    IXPOICARRENTAL,
    IXPOIOPERATEREF,
    IXPOIUPLOAD,
    IXSAMEPOI,
    IXSAMEPOIPART,

    ZONEFACE,
    ZONEFACETOPO,
    ZONELINK,
    ZONELINKKIND,
    ZONELINKMESH,
    ZONENODE,
    ZONENODEMESH,

    CKEXCEPTION,
    LUNODE,
    LUNODEMESH,
    LULINK,
    LULINKKIND,
    LULINKMESH,
    LUFACE,
    LUFACENAME,
    LUFACETOPO,
    LUFEATURE,

    RDSLOPE,
    RDSLOPEVIA,
    RDGATE,
    RDGATECONDITION,

    RDELECTRONICEYE,
    RDELECEYEPAIR,
    RDELECEYEPART,

    RDSE,
    RDSPEEDBUMP,

    LCNODE,
    LCNODEMESH,
    LCLINK,
    LCLINKKIND,
    LCLINKMESH,
    LCFACE,
    LCFACENAME,
    LCFACETOPO,
    LCFEATURE,

    RDTOLLGATE,
    RDTOLLGATEPASSAGE,
    RDTOLLGATENAME,
    RDTOLLGATEMAPPING,
    RDTOLLGATEFEE,
    RDTOLLGATECOST,

    RDINTER,
    RDSAMENODE,
    RDSAMENODEPART,
    RDSAMELINK,
    RDSAMELINKPART,
    RDINTERLINK,
    RDLANE,
    RDLANECONDITION,
    RDLANETOPODETAIL,
    RDLANETOPOVIA,
    RDINTERNODE,
    RDOBJECT,
    RDOBJECTNODE,
    RDOBJECTLINK,
    RDOBJECTROAD,
    RDOBJECTINTER,
    RDOBJECTNAME,
    RDROAD,
    RDROADLINK,
    RDVARIABLESPEED,
    RDVARIABLESPEEDVIA,
    //在线批处理类型
    FACE,
	TMCPOINT,
	RDTMCLOCATION,
	RDTMCLOCATIONLINK,
	TMCLINE;
}