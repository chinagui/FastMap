package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;

public abstract class AbstactBo {
	protected Connection conn;
	protected boolean isLock;
}
