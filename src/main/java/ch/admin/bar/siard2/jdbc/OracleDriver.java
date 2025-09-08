/*======================================================================
OracleDriver implements a wrapped MSSQL Oracle.
Version     : $Id: $
Application : SIARD2
Description : OracleDriver implements a wrapped Oracle Driver.
Platform    : Java 7   
------------------------------------------------------------------------
Copyright  : 2016, Enter AG, Rueti ZH, Switzerland
Created    : 16.06.2016, Simon Jutz
======================================================================*/

package ch.admin.bar.siard2.jdbc;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Properties;
import ch.enterag.utils.jdbc.BaseDriver;
import ch.enterag.utils.logging.IndentLogger;

/*====================================================================*/
/** OracleDriver implements a wrapped Oracle Driver.
 * @author Simon Jutz
 */
public class OracleDriver
	extends BaseDriver
	implements Driver
{
  static {
    System.setProperty("oracle.jdbc.getObjectReturnsXMLType", "false");
    // Avoid Oracle Net network statistics which triggers oracle.net.nt.Clock initialization
    System.setProperty("oracle.net.networkStatistics", "false");
    // As a fallback, disable Oracle Net NIO path (uses TimeoutSocketChannel) to avoid Timer usage
    System.setProperty("oracle.net.useNIO", "false");
      System.setProperty("oracle.jdbc.disableMBeanRegistration", "true");
  }
	/** logger */
	private static IndentLogger _il = IndentLogger.getIndentLogger(OracleDriver.class.getName());
  /** protocol sub scheme for Oracle JDBC URL */
  public static final String sORACLE_SCHEME = "oracle";
  /** URL prefix for Oracle JDBC URL */
  public static final String sORACLE_URL_PREFIX = sJDBC_SCHEME+":"+sORACLE_SCHEME+":";
  /** URL for database name.
   * @param sDatabaseName host:port:sid, e.g. localhost:1521:orcl
   *                      or host:port/service 
   * @return JDBC URL for thin driver.
   */
  public static String getUrl(String sDatabaseName)
  {
    String sUrl = sDatabaseName;
    if (!sUrl.startsWith(sORACLE_URL_PREFIX))
      sUrl = sORACLE_URL_PREFIX + "thin:@"+sDatabaseName;
    return sUrl;
  } /* getUrl */

  /** Idempotent registration guard */
  private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

  /** Ensure the wrapper driver is registered exactly once in this JVM. */
  public static void ensureRegistered()
  {
    if (REGISTERED.compareAndSet(false, true))
    {
      safeRegister();
    }
  }

  /** Explicitly unregister the wrapper and restore the plain Oracle driver. */
  public static void unregister()
  {
    // Deregister our wrapper
    try
    {
      Enumeration<Driver> drivers = DriverManager.getDrivers();
      while (drivers.hasMoreElements())
      {
        Driver d = drivers.nextElement();
        if (d.getClass() == OracleDriver.class)
        {
          try { DriverManager.deregisterDriver(d); } catch (SQLException ignore) {}
        }
      }
      // Re-register plain Oracle driver to keep DriverManager usable in shared JVMs
      try
      {
        Class<?> cls = Class.forName("oracle.jdbc.OracleDriver");
        Constructor<?> ctor = cls.getDeclaredConstructor();
        ctor.setAccessible(true);
        Driver oracle = (Driver)ctor.newInstance();
        DriverManager.registerDriver(oracle);
      }
      catch (Throwable t)
      {
        // Non-fatal: environments using only DataSource don't require DriverManager fallback
      }
    }
    finally
    {
      REGISTERED.set(false);
    }
  }

  /** Perform robust registration: remove existing drivers, then register our wrapper. */
  private static void safeRegister()
  {
    try
    {
      // Remove any previously registered Oracle or wrapper drivers (possibly from other classloaders)
      Enumeration<Driver> enumDriver = DriverManager.getDrivers();
      while (enumDriver.hasMoreElements())
      {
        Driver d = enumDriver.nextElement();
        String name = d.getClass().getName();
        if (name.equals("oracle.jdbc.OracleDriver") || d.getClass() == OracleDriver.class)
        {
          try { DriverManager.deregisterDriver(d); } catch (SQLException ignore) {}
        }
      }

      // Register wrapper and map the underlying Oracle driver via BaseDriver
      BaseDriver.register(new OracleDriver(), "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:orcl");
    }
    catch (Exception e)
    {
      REGISTERED.set(false);
      throw new Error(e);
    }
  }

  /*------------------------------------------------------------------*/
  /** {@inheritDoc}*/
  @Override
  public boolean acceptsURL(String url) throws SQLException
  {
    _il.enter(url);
    boolean bAccepts = url.startsWith("jdbc:oracle:");
    _il.exit(bAccepts);
    return bAccepts;
  } /* acceptsUrl */
  
	/*------------------------------------------------------------------*/
	/** {@inheritDoc}
	 * returns the appropriately wrapped Oracle Connection.
	 */
	@Override
	public Connection connect(String url, Properties info)
		throws SQLException
	{
		_il.enter(url, info);
    Connection conn = super.connect(url, info);
		if (conn != null)
		conn = new OracleConnection(conn); 
    _il.exit(conn);
    return conn;
	} /* connect */
	  
} /* class OracleDriver */
