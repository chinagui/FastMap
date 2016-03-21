package com.navinfo.navicommons.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author liuqing
 */
public class SystemGlobals implements VariableStore {

    public static void main(String args[]) {
        SystemGlobals.getValue("1");
    }

    private static SystemGlobals globals = new SystemGlobals();

    private Properties defaults = new Properties();

    private VariableExpander expander = new VariableExpander(this, "${", "}");

    private static final Logger logger = Logger.getLogger(SystemGlobals.class);

    static {

        SystemGlobals.initGlobals("/SystemGlobals.properties");
    }

    private SystemGlobals() {
    }

    /**
     * Initialize the global configuration
     *
     * @param file The application path (normally the path to the webapp base dir
     */
    public static void initGlobals(String file) {
        try {
            globals = new SystemGlobals();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            if (is == null) {
                is = SystemGlobals.class.getResourceAsStream(file);
            }
            buildSystem(is);
        } catch (IOException e) {
            logger.error(e);
        }

    }

    public static void reset() {
        globals.defaults.clear();
    }

    private static void buildSystem(InputStream input) throws IOException {
        if (input != null) {
            try {
                Properties pro = new Properties();
                pro.load(input);
                globals.defaults.putAll(pro);
            } catch (IOException e) {
                throw e;
            } finally {
                input.close();
            }
        } else {
            logger.warn("SystemGlobals.properties not found,create a empty Properties");
            globals.defaults.putAll(new Properties());
        }
    }

    /**
     * Gets the value of some property
     *
     * @param field The property name to retrieve the value
     * @return String with the value, or <code>null</code> if not found
     * @see #setValue(String, String)
     */
    public static String getValue(String field) {
        return globals.getVariableValue(field);
    }

    public static String getValue(String field, String defaule) {
        String value = getValue(field);
        if (StringUtils.isBlank(value)) {
            return defaule;
        }
        return value;
    }

    /**
     * Retrieve an integer-valued configuration field
     *
     * @param field Name of the configuration option
     * @return The value of the configuration option
     * @throws NullPointerException when the field does not exists
     */
    public static int getIntValue(String field) {
        return Integer.parseInt(getValue(field));
    }

    public static int getIntValue(String field, int defVal) {
        return Integer.parseInt(getValue(field, "" + defVal));
    }

    /**
     * Retrieve an boolean-values configuration field
     *
     * @param field name of the configuration option
     * @return The value of the configuration option
     * @throws NullPointerException when the field does not exists
     */
    public static boolean getBoolValue(String field) {
        return "true".equals(getValue(field));
    }

    /**
     * Retrieve an boolean-values configuration field
     *
     * @param field
     * @param df
     * @return
     */
    public static boolean getBoolValue(String field, boolean df) {
        String value = getValue(field);
        if (StringUtils.isBlank(value)) {
            return df;
        }
        return getBoolValue(field);
    }

    /**
     * Return the value of a configuration value as a variable. Variable
     * expansion is performe on the result.
     *
     * @param field The field name to retrieve
     * @return The value of the field if present or null if not
     */

    public String getVariableValue(String field) {
        String preExpansion = this.defaults.getProperty(field);

        if (preExpansion == null) {
            return null;
        }

        return expander.expandVariables(preExpansion);
    }

    /**
     * Retrieve an iterator that iterates over all known configuration keys
     *
     * @return An iterator that iterates over all known configuration keys
     */
    public static Iterator fetchConfigKeyIterator() {
        return globals.defaults.keySet().iterator();
    }

    public static Set<Map.Entry<Object, Object>> fetchConfigEntrySet() {
        return globals.defaults.entrySet();
    }

    public static Properties getConfigData() {
        return new Properties(globals.defaults);
    }
}