package com.navinfo.dataservice.engine.edit.operation;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.iface.IOperator;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

/**
 * 操作类工厂
 */
public class OperatorFactory {

	/**
	 * 操作结果写入数据库
	 * 
	 * @param conn
	 *            数据库连接
	 * @param result
	 *            操作结果
	 * @throws Exception
	 */
	public static void recordData(Connection conn, Result result) throws Exception {
		for (IRow obj : result.getDelObjects()) {

			getOperator(conn, obj).deleteRow();
		}

		for (IRow obj : result.getAddObjects()) {

			getOperator(conn, obj).insertRow();
		}

		for (IRow obj : result.getUpdateObjects()) {

			getOperator(conn, obj).updateRow();
		}
	}

	/**
	 * 根据对象，返回操作类
	 * 
	 * @param conn
	 *            数据库连接
	 * @param obj
	 *            对象
	 * @return
	 * @throws Exception
	 */
	private static IOperator getOperator(Connection conn, IRow obj) throws Exception {
		return new BasicOperator(conn, obj);
		/**
		switch (obj.objType()) {
		case RDLINK:
			return new RdLinkOperator(conn, (RdLink) obj);
		case RDLINKFORM:
			return new RdLinkFormOperator(conn, (RdLinkForm) obj);
		case RDLINKLIMIT:
			return new RdLinkLimitOperator(conn, (RdLinkLimit) obj);
		case RDLINKNAME:
			return new RdLinkNameOperator(conn, (RdLinkName) obj);
		case RDLINKLIMITTRUNK:
			return new RdLinkLimitTruckOperator(conn, (RdLinkLimitTruck) obj);
		case RDLINKSPEEDLIMIT:
			return new RdLinkSpeedlimitOperator(conn, (RdLinkSpeedlimit) obj);
		case RDLINKSIDEWALK:
			return new RdLinkSidewalkOperator(conn, (RdLinkSidewalk) obj);
		case RDLINKWALKSTAIR:
			return new RdLinkWalkstairOperator(conn, (RdLinkWalkstair) obj);
		case RDLINKRTIC:
			return new RdLinkRticOperator(conn, (RdLinkRtic) obj);
		case RDLINKINTRTIC:
			return new RdLinkIntRticOperator(conn, (RdLinkIntRtic) obj);
		case RDLINKZONE:
			return new RdLinkZoneOperator(conn, (RdLinkZone) obj);
		case RDNODE:
			return new RdNodeOperator(conn, (RdNode) obj);
		case RDNODEFORM:
			return new RdNodeFormOperator(conn, (RdNodeForm) obj);
		case RDNODEMESH:
			return new RdNodeMeshOperator(conn, (RdNodeMesh) obj);
		case RDNODENAME:
			return new RdNodeNameOperator(conn, (RdNodeName) obj);
		case RDRESTRICTION:
			return new RdRestrictionOperator(conn, (RdRestriction) obj);
		case RDRESTRICTIONDETAIL:
			return new RdRestrictionDetailOperator(conn, (RdRestrictionDetail) obj);
		case RDRESTRICTIONCONDITION:
			return new RdRestrictionConditionOperator(conn, (RdRestrictionCondition) obj);
		case RDRESTRICTIONVIA:
			return new RdRestrictionViaOperator(conn, (RdRestrictionVia) obj);
		case RDCROSS:
			return new RdCrossOperator(conn, (RdCross) obj);
		case RDCROSSLINK:
			return new RdCrossLinkOperator(conn, (RdCrossLink) obj);
		case RDCROSSNAME:
			return new RdCrossNameOperator(conn, (RdCrossName) obj);
		case RDCROSSNODE:
			return new RdCrossNodeOperator(conn, (RdCrossNode) obj);
		case RDSPEEDLIMIT:
			return new RdSpeedlimitOperator(conn, (RdSpeedlimit) obj);
		case RDBRANCH:
			return new RdBranchOperator(conn, (RdBranch) obj);
		case RDBRANCHDETAIL:
			return new RdBranchDetailOperator(conn, (RdBranchDetail) obj);
		case RDBRANCHNAME:
			return new RdBranchNameOperator(conn, (RdBranchName) obj);
		case RDBRANCHREALIMAGE:
			return new RdBranchRealimageOperator(conn, (RdBranchRealimage) obj);
		case RDBRANCHSCHEMATIC:
			return new RdBranchSchematicOperator(conn, (RdBranchSchematic) obj);
		case RDBRANCHVIA:
			return new RdBranchViaOperator(conn, (RdBranchVia) obj);
		case RDSERIESBRANCH:
			return new RdSeriesbranchOperator(conn, (RdSeriesbranch) obj);
		case RDSIGNASREAL:
			return new RdSignasrealOperator(conn, (RdSignasreal) obj);
		case RDSIGNBOARD:
			return new RdSignboardOperator(conn, (RdSignboard) obj);
		case RDSIGNBOARDNAME:
			return new RdSignboardNameOperator(conn, (RdSignboardName) obj);
		case RDLANECONNEXITY:
			return new RdLaneConnexityOperator(conn, (RdLaneConnexity) obj);
		case RDLANETOPOLOGY:
			return new RdLaneTopologyOperator(conn, (RdLaneTopology) obj);
		case RDLANEVIA:
			return new RdLaneViaOperator(conn, (RdLaneVia) obj);
		case RDGSC:
			return new RdGscOperator(conn, (RdGsc) obj);
		case RDGSCLINK:
			return new RdGscLinkOperator(conn, (RdGscLink) obj);
		case ADFACE:
			return new AdFaceOperator(conn, (AdFace) obj);
		case ADFACETOPO:
			return new AdFaceTopoOperator(conn, (AdFaceTopo) obj);
		case ADADMIN:
			return new AdAdminOperator(conn, (AdAdmin) obj);
		case ADADMINGPART:
			return new AdAdminPartOperator(conn, (AdAdminPart) obj);
		case ADADMINDETAIL:
			return new AdAdminDetailOperator(conn, (AdAdminDetail) obj);
		case ADADMINGROUP:
			return new AdAdminGroupOperator(conn, (AdAdminGroup) obj);
		case ADADMINGNAME:
			return new AdAdminNameOperator(conn, (AdAdminName) obj);
		case ADLINK:
			return new AdLinkOperator(conn, (AdLink) obj);
		case ADLINKMESH:
			return new AdLinkMeshOperator(conn, (AdLinkMesh) obj);
		case ADNODE:
			return new AdNodeOperator(conn, (AdNode) obj);
		case ADNODEMESH:
			return new AdNodeMeshOperator(conn, (AdNodeMesh) obj);
		case IXPOI:
			return new IxPoiOperator(conn, (IxPoi) obj);
		case IXPOIPARENT:
			return new IxPoiParentOperator(conn, (IxPoiParent) obj);
		case IXPOICHILDREN:
			return new IxPoiChildrenOperator(conn, (IxPoiChildren) obj);
		case RWNODE:
			return new RwNodeOperator(conn, (RwNode) obj);
		case RWLINK:
			return new RwLinkOperator(conn, (RwLink) obj);
		case RWLINKNAME:
			return new RwLinkNameOperator(conn, (RwLinkName) obj);

		case IXPOINAME:
			return new IxPoiNameOperator(conn, (IxPoiName) obj);

		case IXPOINAMEFLAG:
			return new IxPoiNameFlagOperator(conn, (IxPoiNameFlag) obj);

		case IXPOIADDRESS:
			return new IxPoiAddressOperator(conn, (IxPoiAddress) obj);

		case IXPOICONTACT:
			return new IxPoiContactOperator(conn, (IxPoiContact) obj);
		case IXPOIFLAG:
			return new IxPoiFlagOperator(conn, (IxPoiFlag) obj);
		case IXPOIENTRYIMAGE:
			return new IxPoiEntryImageOperator(conn, (IxPoiEntryimage) obj);
		case IXPOIICON:
			return new IxPoiIconOperator(conn, (IxPoiIcon) obj);
		case IXPOINAMETONE:
			return new IxPoiNameToneOperator(conn, (IxPoiNameTone) obj);
		case IXPOIPHOTO:
			return new IxPoiPhotoOperator(conn, (IxPoiPhoto) obj);
		case IXPOIAUDIO:
			return new IxPoiAudioOperator(conn, (IxPoiAudio) obj);
		case IXPOIVIDEO:
			return new IxPoiVideoOperator(conn, (IxPoiVideo) obj);
		case IXPOIBUILDING:
			return new IxPoiBuildingOperator(conn, (IxPoiBuilding) obj);
		case IXPOIDETAIL:
			return new IxPoiDetailOperator(conn, (IxPoiDetail) obj);
		case IXPOIBUSINESSTIME:
			return new IxPoiBusinessTimeOperator(conn, (IxPoiBusinessTime) obj);
		case IXPOIINTRODUCTION:
			return new IxPoiIntroductionOperator(conn, (IxPoiIntroduction) obj);
		case IXPOIADVERTISEMENT:
			return new IxPoiAdvertisementOperator(conn, (IxPoiAdvertisement) obj);
		case IXPOIGASSTATION:
			return new IxPoiGasstationOperator(conn, (IxPoiGasstation) obj);
		case IXPOICHARGINGSTATION:
			return new IxPoiChargingStationOperator(conn, (IxPoiChargingStation) obj);
		case IXPOICHARGINGPLOT:
			return new IxPoiChargingPlotOperator(conn, (IxPoiChargingPlot) obj);
		case IXPOICHARGINGPLOTPH:
			return new IxPoiChargingPlotPhOperator(conn, (IxPoiChargingPlotPh) obj);
		case IXPOIPARKING:
			return new IxPoiParkingOperator(conn, (IxPoiParking) obj);
		case IXPOIATTRACTION:
			return new IxPoiAttractionOperator(conn, (IxPoiAttraction) obj);
		case IXPOIHOTEL:
			return new IxPoiHotelOperator(conn, (IxPoiHotel) obj);
		case IXPOIRESTAURANT:
			return new IxPoiRestaurantOperator(conn, (IxPoiRestaurant) obj);
		case IXPOICARRENTAL:
			return new IxPoiCarrentalOperator(conn, (IxPoiCarrental) obj);
		case ZONEFACE:
			return new ZoneFaceOperator(conn, (ZoneFace) obj);
		case ZONEFACETOPO:
			return new ZoneFaceTopoOperator(conn, (ZoneFaceTopo) obj);
		case ZONELINK:
			return new ZoneLinkOperator(conn, (ZoneLink) obj);
		case ZONELINKKIND:
			return new ZoneLinkKindOperator(conn, (ZoneLinkKind) obj);
		case ZONELINKMESH:
			return new ZoneLinkMeshOperator(conn, (ZoneLinkMesh) obj);
		case ZONENODE:
			return new ZoneNodeOperator(conn, (ZoneNode) obj);
		case ZONENODEMESH:
			return new ZoneNodeMeshOperator(conn, (ZoneNodeMesh) obj);
		case LUNODE:
			return new LuNodeOperator(conn, (LuNode) obj);
		case LULINK:
			return new LuLinkOperator(conn, (LuLink) obj);
		case LUFACE:
			return new LuFaceOperator(conn, (LuFace) obj);
		case LUNODEMESH:
			return new LuNodeMeshOperator(conn, (LuNodeMesh) obj);
		case LULINKKIND:
			return new LuLinkKindOperator(conn, (LuLinkKind) obj);
		case LULINKMESH:
			return new LuLinkMeshOperator(conn, (LuLinkMesh) obj);
		case LUFACENAME:
			return new LuFaceNameOperator(conn, (LuFaceName) obj);
		case LUFACETOPO:
			return new LuFaceTopoOperator(conn, (LuFaceTopo) obj);
		case LUFEATURE:
			return new LuFeatureOperator(conn, (LuFeature) obj);
		case RDELECTRONICEYE:
			return new RdElectroniceyeOperator(conn, (RdElectroniceye) obj);
		case RDELECEYEPART:
			return new RdEleceyePartOperator(conn, (RdEleceyePart) obj);
		case RDELECEYEPAIR:
			return new RdEleceyePairOperator(conn, (RdEleceyePair) obj);
		case RDTRAFFICSIGNAL:
			return new RdTrafficsignalOperator(conn, (RdTrafficsignal) obj);
		default:
			return null;
		}
		*/
	}
}
