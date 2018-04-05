/* OracleArray wraps a native oracle.sql.ARRAY extending its result
 * to the maximum declared length of the array.
 */
package ch.admin.bar.siard2.jdbc;

import java.sql.*;
import java.util.*;
import ch.enterag.utils.jdbc.*; 
import ch.enterag.sqlparser.identifier.*;

@SuppressWarnings("deprecation")
public class OracleArray
  extends BaseArray
  implements Array
{
  private oracle.sql.ARRAY _oarray = null;
  void wrap(oracle.sql.ARRAY oarray) { _oarray = oarray; }
  private long _lMaxLength = -1;
  private String _sBaseTypeName = null;
  
  public OracleArray(oracle.sql.ARRAY oarray)
    throws SQLException
  {
    super(oarray);
    _oarray = oarray;
    _lMaxLength = _oarray.getDescriptor().getMaxLength();
    QualifiedId qiVarray = new QualifiedId(null,
      _oarray.getDescriptor().getSchemaName(),
      _oarray.getDescriptor().getTypeName());
    String sFullArrayTypeName = OracleMetaColumns.getFullArrayTypeName(_oarray.getConnection(),qiVarray);
    int i = sFullArrayTypeName.lastIndexOf(" ARRAY[");
    _sBaseTypeName = sFullArrayTypeName.substring(0,i).trim();
  }
  
  public OracleArray(oracle.sql.ARRAY oarray, String sBaseTypeName)
    throws SQLException
  {
    super(oarray);
    _oarray = oarray;
    _sBaseTypeName = sBaseTypeName;
    _lMaxLength = _oarray.getDescriptor().getMaxLength();
  }

  @Override
  public String getBaseTypeName() throws SQLException
  {
    return _sBaseTypeName;
  }

  @Override
  public int getBaseType() throws SQLException
  {
    return _oarray.getBaseType();
  }

  private Object[] adjustArray(Object[] oa)
  {
    if (oa.length != _lMaxLength)
      oa = Arrays.copyOf(oa, (int)_lMaxLength);
    return oa;
  } /* adjustArray */
  
  @Override
  public Object getArray() throws SQLException
  {
    return adjustArray((Object[])_oarray.getArray());
  } /* getArray */

  @Override
  public Object getArray(Map<String, Class<?>> map) throws SQLException
  {
    return adjustArray((Object[])_oarray.getArray(map));
  }

  @Override
  public Object getArray(long index, int count) throws SQLException
  {
    return adjustArray((Object[])_oarray.getArray(index,count));
  }

  @Override
  public Object getArray(long index, int count,
    Map<String, Class<?>> map) throws SQLException
  {
    return adjustArray((Object[])_oarray.getArray(index,count,map));
  }

  @Override
  public ResultSet getResultSet() throws SQLException
  {
    // we would have to wrap the ResultSet in order to extend it ... 
    throw new SQLFeatureNotSupportedException("ResultSet interface of arrays is not supported.");
  }

  @Override
  public ResultSet getResultSet(Map<String, Class<?>> map)
    throws SQLException
  {
    // we would have to wrap the ResultSet in order to extend it ... 
    throw new SQLFeatureNotSupportedException("ResultSet interface of arrays is not supported.");
  }

  @Override
  public ResultSet getResultSet(long index, int count)
    throws SQLException
  {
    // we would have to wrap the ResultSet in order to extend it ... 
    throw new SQLFeatureNotSupportedException("ResultSet interface of arrays is not supported.");
  }

  @Override
  public ResultSet getResultSet(long index, int count,
    Map<String, Class<?>> map) throws SQLException
  {
    // we would have to wrap the ResultSet in order to extend it ... 
    throw new SQLFeatureNotSupportedException("ResultSet interface of arrays is not supported.");
  }

  @Override
  public void free() throws SQLException
  {
    _oarray.free();
  }

} /* OracleArray */
