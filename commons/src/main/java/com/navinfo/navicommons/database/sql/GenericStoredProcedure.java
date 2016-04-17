package com.navinfo.navicommons.database.sql;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * User: liuqing
 * Date: 2010-9-9
 * Time: 16:08:16
 */
public class GenericStoredProcedure extends org.springframework.jdbc.object.StoredProcedure{
    /**
     * Allow use as a bean.
     */
    public GenericStoredProcedure() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Create a new object wrapper for a stored procedure.
     *
     * @param ds   DataSource to use throughout the lifetime
     *             of this object to obtain connections
     * @param name name of the stored procedure in the database
     */
    public GenericStoredProcedure(DataSource ds, String name) {
        super(ds, name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Create a new object wrapper for a stored procedure.
     *
     * @param jdbcTemplate JdbcTemplate which wraps DataSource
     * @param name         name of the stored procedure in the database
     */
    public GenericStoredProcedure(JdbcTemplate jdbcTemplate, String name) {
        super(jdbcTemplate, name);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
