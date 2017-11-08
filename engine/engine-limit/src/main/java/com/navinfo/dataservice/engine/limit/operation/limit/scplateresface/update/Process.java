package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.update;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;

import java.util.ArrayList;
import java.util.List;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		ScPlateresFaceSearch search = new ScPlateresFaceSearch(this.getConn());

		if (this.getCommand().getIds() != null) {

			List<ScPlateresFace> faces = search.loadByGeometryIds(this.getCommand().getIds());

			this.getCommand().setFaces(faces);
		} else {

			this.getCommand().setFace(search.loadById(getCommand().getGemetryId()));
		}
		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
