package com.navinfo.navicommons.database.sql;

import java.sql.Connection;


public interface ConnectionManager
{

    Connection getConnection() throws PersistenceException;

    boolean closeConnection(Connection conn);

}
