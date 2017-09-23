package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.create;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;

public class Operation  implements IOperation{

	private Command command = null;
	
	public Operation(Command command){
		this.command = command;
	}
	
	@Override
	public String run(Result result){
		ScPlateresManoeuvre manoeuvre = new ScPlateresManoeuvre();
		
		manoeuvre.setManoeuvreId(1002001);
		manoeuvre.setGroupId(command.getGroupId());
		manoeuvre.setVehicle(command.getVehicle());
		manoeuvre.setAttribution(command.getAttribution());
		manoeuvre.setRestrict(command.getRestrict());
		manoeuvre.setTempPlate(command.getTempPlate());
		manoeuvre.setTempPlateNum(command.getTempPlateNum());
		manoeuvre.setCharSwitch(command.getCharSwitch());
		manoeuvre.setTailNumber(command.getTailNumber());
		manoeuvre.setEnergyType(command.getEnergyType());
		manoeuvre.setGasEmisstand(command.getGasEmisstand());
		manoeuvre.setSeatnum(command.getSeatnum());
		manoeuvre.setVehicleLength(command.getVehicleLength());
		manoeuvre.setResWeigh(command.getResWeigh());
		manoeuvre.setResAxleLoad(command.getResAxleLoad());
		manoeuvre.setResAxleCount(command.getResAxleCount());
		manoeuvre.setStartDate(command.getStartDate());
		manoeuvre.setEndDate(command.getEndDate());
		manoeuvre.setResDatetype(command.getResDatetype());
		manoeuvre.setTime(command.getTime());
		manoeuvre.setSpecFlag(command.getSpecFlag());
		
		result.insertObject(manoeuvre, ObjStatus.INSERT, String.valueOf(manoeuvre.getManoeuvreId()));
        return null;
	}
}