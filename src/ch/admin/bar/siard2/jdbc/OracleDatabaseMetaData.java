/*======================================================================
OracleDatabaseMetaData implements wrapped Oracle DatabaseMetaData.
Version     : $Id: $
Application : SIARD2
Description : OracleDatabaseMetaData implements wrapped Oracle DatabaseMetaData.
Platform    : Java 7   
------------------------------------------------------------------------
Copyright  : 2016, Enter AG, Rueti ZH, Switzerland
Created    : 17.06.2016, Simon Jutz
======================================================================*/
package ch.admin.bar.siard2.jdbc;

import java.sql.*;
import java.util.*;

import ch.enterag.utils.jdbc.*;
import ch.enterag.utils.logging.*;
import ch.enterag.sqlparser.*;

/*=====================================================================*/
/** OracleDatabaseMetaData implements wrapped Oracle DatabaseMetaData.
 * @author Simon Jutz
 */
public class OracleDatabaseMetaData
	extends BaseDatabaseMetaData
	implements DatabaseMetaData
{
  /** logger */
  private static IndentLogger _il = IndentLogger.getIndentLogger(OracleDatabaseMetaData.class.getName());
  private static final String sTABLE = "TABLE";
  private static final String sVIEW = "VIEW";
  private static final String sSYNONYM = "SYNONYM";
  private static final String sGLOBAL_TEMPORARY = "GLOBAL TEMPORARY";
  private static final String sSYSTEM_TABLE = "SYSTEM TABLE";
  private static final String[] asTABLE_TYPES = new String[]
  {
    sTABLE,
    sVIEW,
    sSYNONYM,
    sGLOBAL_TEMPORARY,
    sSYSTEM_TABLE
  };
  /* all oracle-maintained users */
  private static final String[] asORACLE_MAINTAINED = new String[]
  {
    "ANONYMOUS",
    "APEX_*",
    "APEX_PUBLIC_USER",
    "APPQOSSYS",
    "AUDSYS",
    "CTXSYS",
    "DBSNMP",
    "DIP",
    "DVF",
    "DVSYS",
    "FLOWS_FILES",
    "GSMADMIN_INTERNAL",
    "GSMCATUSER",
    "GSMUSER",
    "LBACSYS",
    "MDDATA",
    "MDSYS",
    "OJVMSYS",
    "OLAPSYS",
    "ORACLE_OCM",
    "ORDDATA",
    "ORDPLUGINS",
    "ORDSYS",
    "OUTLN",
    "PUBLIC",
    "SI_INFORMTN_SCHEMA",
    "SPATIAL_CSW_ADMIN_USR",
    "SPATIAL_WFS_ADMIN_USR",
    "SYS",
    "SYSBACKUP",
    "SYSDG",
    "SYSKM",
    "SYSTEM",
    "WMSYS",
    "XDB",
    "XS$NULL"
  };
  private static final String sSYS_SCHEMA = "SYS";
  private static final String sTABLE_ALL_USERS = "ALL_USERS";
  private static final String sALIAS_ALL_USERS = "U";
  private static final String sCOLUMN_USERNAME = "USERNAME";
  private static final String sCOLUMN_ORACLE_MAINTAINED = "ORACLE_MAINTAINED";

  /*------------------------------------------------------------------*/
  /** get oracle maintained condition from table ALL_USERS with alias U.
   * @param bOracleMaintained false if condition excludes ORACLE_MAINTAINED. 
   * @return condition
   * @throws SQLException if a database error occurred.
   */
  private String getOracleMaintainedCondition(
    boolean bOracleMaintained)
    throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    /***/
    ResultSet rs = getColumns(null, sSYS_SCHEMA, sTABLE_ALL_USERS, sCOLUMN_ORACLE_MAINTAINED);
    if (rs.next())
    {
      sb.append(sALIAS_ALL_USERS);
      sb.append(".");
      sb.append(sCOLUMN_ORACLE_MAINTAINED);
      sb.append(" = '");
      if (bOracleMaintained)
        sb.append("Y");
      else
        sb.append("N");
      sb.append("'");
    }
    else
    {
    /***/
      for (int i = 0; i < asORACLE_MAINTAINED.length; i++)
      {
        if (i > 0)
        {
          if (bOracleMaintained)
            sb.append(" OR ");
          else
            sb.append(" AND ");
        }
        sb.append("(");
        String sUserMaintained = asORACLE_MAINTAINED[i];
        if (sUserMaintained.endsWith("*"))
        {
          sb.append("SUBSTR(");
          sb.append(sALIAS_ALL_USERS);
          sb.append(".");
          sb.append(sCOLUMN_USERNAME);
          sb.append(",1,");
          sb.append(String.valueOf(sUserMaintained.length()-1));
          sb.append(")");
          if (bOracleMaintained)
            sb.append(" = '");
          else
            sb.append(" <> '");
          sb.append(sUserMaintained.substring(0, sUserMaintained.length()-1));
        }
        else
        {
          sb.append(sALIAS_ALL_USERS);
          sb.append(".");
          sb.append(sCOLUMN_USERNAME);
          if (bOracleMaintained)
            sb.append(" = '");
          else
            sb.append(" <> '");
          sb.append(sUserMaintained);
        }
        sb.append("')");
      }
    /***/
    }
    rs.close();
    /***/
    sb.append(")");
    return sb.toString();
  }
	/*------------------------------------------------------------------*/
	/** constructor
	 * @param dmdWrapped database meta data to be wrapped.
 	 */
	public OracleDatabaseMetaData(DatabaseMetaData dmdWrapped) 
	{
		super(dmdWrapped);
	} /* constructor */
	
	/*------------------------------------------------------------------*/
	/** {@inheritDoc} */
	@Override
	public Connection getConnection() throws SQLException
	{
		return new OracleConnection(super.getConnection());
	}

  /*------------------------------------------------------------------*/
	/** {@inheritDoc} */
	@Override
	public ResultSet getTypeInfo() throws SQLException
	{
		return super.getTypeInfo();
	} /* getTypeInfo */

  /*------------------------------------------------------------------*/
  private String getDataTypeCase(String sDataType, String sTypeCode)
    throws SQLException
  {
    Map<String,Integer> mapDataType = new HashMap<String,Integer>();
    mapDataType.put("ANYDATA",Integer.valueOf(Types.CLOB));
    mapDataType.put("BFILE",Integer.valueOf(Types.BLOB));
    mapDataType.put("BIGINT",Integer.valueOf(Types.BIGINT));
    mapDataType.put("BINARY_DOUBLE",Integer.valueOf(Types.DOUBLE));
    mapDataType.put("BINARY_FLOAT",Integer.valueOf(Types.REAL));
    mapDataType.put("BIT", Integer.valueOf(Types.BOOLEAN));
    mapDataType.put("BLOB",Integer.valueOf(Types.BLOB));
    mapDataType.put("CHAR",Integer.valueOf(Types.CHAR));
    mapDataType.put("CLOB",Integer.valueOf(Types.CLOB));
    mapDataType.put("DATE",Integer.valueOf(Types.TIMESTAMP));
    mapDataType.put("FLOAT",Integer.valueOf(Types.DOUBLE));
    mapDataType.put("INTEGER",Integer.valueOf(Types.INTEGER));
    mapDataType.put("INTERVAL%",Integer.valueOf(Types.OTHER));
    mapDataType.put("LONG",Integer.valueOf(Types.CLOB));
    mapDataType.put("LONG RAW",Integer.valueOf(Types.BLOB));
    mapDataType.put("NCHAR",Integer.valueOf(Types.NCHAR));
    mapDataType.put("NCLOB",Integer.valueOf(Types.NCLOB));
    mapDataType.put("NUMBER",Integer.valueOf(Types.NUMERIC));
    mapDataType.put("NUMERIC",Integer.valueOf(Types.NUMERIC));
    mapDataType.put("NVARCHAR2",Integer.valueOf(Types.NVARCHAR));
    mapDataType.put("ROWID", Integer.valueOf(Types.ROWID));
    mapDataType.put("RAW",Integer.valueOf(Types.VARBINARY));
    mapDataType.put("REAL",Integer.valueOf(Types.REAL));
    mapDataType.put("SMALLINT",Integer.valueOf(Types.SMALLINT));
    mapDataType.put("TIME",Integer.valueOf(Types.TIME));
    mapDataType.put("TIMESTAMP%",Integer.valueOf(Types.TIMESTAMP));
    mapDataType.put("TINYINT",Integer.valueOf(Types.TINYINT));
    mapDataType.put("UROWID", Integer.valueOf(Types.ROWID));
    mapDataType.put("VARCHAR2",Integer.valueOf(Types.VARCHAR));
    mapDataType.put("XMLTYPE",Integer.valueOf(Types.SQLXML));
    mapDataType.put("COLLECTION",Integer.valueOf(Types.ARRAY));
    mapDataType.put("OBJECT",Integer.valueOf(Types.STRUCT));
    StringBuilder sbDataTypeCase = new StringBuilder("  CASE\r\n");
    for (Iterator<String> iterTypeName = mapDataType.keySet().iterator(); iterTypeName.hasNext(); )
    {
      String sTypeName = iterTypeName.next();
      int iDataType = mapDataType.get(sTypeName).intValue();
      sbDataTypeCase.append("    WHEN ");
      if ((iDataType != Types.ARRAY) && (iDataType != Types.STRUCT) && (!sTypeName.equals("ANYDATA")))
        sbDataTypeCase.append(sDataType);
      else
        sbDataTypeCase.append(sTypeCode);
      if (sTypeName.indexOf('%') < 0)
        sbDataTypeCase.append(" = ");
      else
        sbDataTypeCase.append(" LIKE ");
      sbDataTypeCase.append(SqlLiterals.formatStringLiteral(sTypeName));
      sbDataTypeCase.append(" THEN ");
      sbDataTypeCase.append(String.valueOf(iDataType));
      sbDataTypeCase.append("\r\n");
    }
    sbDataTypeCase.append("    ELSE ");
    sbDataTypeCase.append(String.valueOf(Types.OTHER));
    sbDataTypeCase.append("\r\n");
    sbDataTypeCase.append("  END");
    return sbDataTypeCase.toString();
  } /* getDataTypeCase */
  
  /*------------------------------------------------------------------*/
  private String getTypeNameCase(String sDataType, String sTypeOwner, String sTypeName)
  {
    StringBuilder sbTypeNameCase = new StringBuilder("  CASE \r\n");
    sbTypeNameCase.append("    WHEN ");
    sbTypeNameCase.append(sTypeOwner);
    sbTypeNameCase.append(" IS NULL AND ");
    sbTypeNameCase.append(sTypeName);
    sbTypeNameCase.append(" IS NULL THEN ");
    sbTypeNameCase.append(sDataType);
    sbTypeNameCase.append("\r\n");
    sbTypeNameCase.append("    WHEN ");
    sbTypeNameCase.append(sTypeOwner);
    sbTypeNameCase.append(" IS NULL AND NOT ");
    sbTypeNameCase.append(sTypeName);
    sbTypeNameCase.append(" IS NULL THEN '\"' || ");
    sbTypeNameCase.append(sTypeName);
    sbTypeNameCase.append(" || '\"'\r\n");
    sbTypeNameCase.append("    ELSE '\"' || ");
    sbTypeNameCase.append(sTypeOwner);
    sbTypeNameCase.append(" || '\".\"' || ");
    sbTypeNameCase.append(sTypeName);
    sbTypeNameCase.append(" || '\"'\r\n");
    sbTypeNameCase.append("  END");
    return sbTypeNameCase.toString();
  } /* getTypeNameCase */

  /*------------------------------------------------------------------*/
  private String getSizeCase(String sDataType, String sLength, String sPrecision)
  {
    StringBuilder sbColumnSizeCase = new StringBuilder("  CASE \r\n");
    sbColumnSizeCase.append("    WHEN ");
    sbColumnSizeCase.append(sPrecision);
    sbColumnSizeCase.append(" IS NULL THEN CASE \r\n");
    sbColumnSizeCase.append("      WHEN ");
    sbColumnSizeCase.append(sDataType);
    sbColumnSizeCase.append(" = 'NUMBER' THEN 38 \r\n");
    sbColumnSizeCase.append("      ELSE ");
    sbColumnSizeCase.append(sLength);
    sbColumnSizeCase.append("\r\n");
    sbColumnSizeCase.append("    END");
    sbColumnSizeCase.append("    ELSE ");
    sbColumnSizeCase.append(sPrecision);
    sbColumnSizeCase.append("\r\n");
    sbColumnSizeCase.append("  END");
    return sbColumnSizeCase.toString();
  }
  
  private String getNullableCase(String sNullable)
  {
    StringBuilder sbNullableCase = new StringBuilder("  CASE ");
    sbNullableCase.append(sNullable);
    sbNullableCase.append("\r\n");
    sbNullableCase.append("    WHEN 'N' THEN ");
    sbNullableCase.append(String.valueOf(DatabaseMetaData.columnNoNulls));
    sbNullableCase.append("\r\n");
    sbNullableCase.append("    WHEN 'Y' THEN ");
    sbNullableCase.append(String.valueOf(DatabaseMetaData.columnNullable));
    sbNullableCase.append("\r\n");
    sbNullableCase.append("    ELSE ");
    sbNullableCase.append(String.valueOf(DatabaseMetaData.columnNullableUnknown));
    sbNullableCase.append("\r\n");
    sbNullableCase.append("  END");
    return sbNullableCase.toString();
  }
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} 
   * This returns a LONG for the column default. Make sure no other JDBC 
   * method is called between this and the next() statement!
   */
  @Override
	public ResultSet getColumns(
	  String catalog, 
	  String schemaPattern, 
	  String tableNamePattern, 
	  String columnNamePattern)
    throws SQLException
  {
    _il.enter(catalog,schemaPattern,tableNamePattern,columnNamePattern);
    StringBuilder sbCondition = new StringBuilder("C.COLUMN_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(columnNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    sbCondition.append("AND C.TABLE_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(tableNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    if (schemaPattern != null)
    {
      sbCondition.append("AND C.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    
    StringBuilder sbIsNullableCase = new StringBuilder("  CASE C.NULLABLE\r\n");
    sbIsNullableCase.append("    WHEN 'N' THEN 'NO'\r\n");
    sbIsNullableCase.append("    WHEN 'Y' THEN 'YES'\r\n");
    sbIsNullableCase.append("    ELSE ''\r\n");
    sbIsNullableCase.append("  END");
    
    String sSql = "SELECT\r\n"+
      "  CAST(NULL AS VARCHAR2(30))AS TABLE_CAT,\r\n" +
      "  C.OWNER AS TABLE_SCHEM,\r\n" +
      "  C.TABLE_NAME AS TABLE_NAME,\r\n" +
      "  C.COLUMN_NAME AS COLUMN_NAME,\r\n" +
      getDataTypeCase("C.DATA_TYPE","T.TYPECODE") + " AS DATA_TYPE,\r\n" +
      getTypeNameCase("C.DATA_TYPE","C.DATA_TYPE_OWNER","C.DATA_TYPE") + " AS TYPE_NAME,\r\n" +
      getSizeCase("C.DATA_TYPE","C.DATA_LENGTH","C.DATA_PRECISION") + " AS COLUMN_SIZE,\r\n" +
      "  CAST(NULL AS NUMBER) AS BUFFER_LENGTH,\r\n" +
      "  C.DATA_SCALE AS DECIMAL_DIGITS,\r\n" +
      "  10 AS NUM_PREC_RADIX,\r\n" +
      getNullableCase("C.NULLABLE") + " AS NULLABLE,\r\n" +
      "  CC.COMMENTS AS REMARKS,\r\n" +
      "  C.DATA_DEFAULT AS COLUMN_DEF,\r\n" +
      "  CAST(NULL AS NUMBER) AS SQL_DATA_TYPE,\r\n" +
      "  CAST(NULL AS NUMBER) AS SQL_DATETIME_SUB,\r\n" +
      "  C.CHAR_LENGTH AS CHAR_OCTET_LENGTH,\r\n" +
      "  C.COLUMN_ID AS ORDINAL_POSITION,\r\n" +
      sbIsNullableCase.toString() + " AS IS_NULLABLE,\r\n" +
      "  CAST(NULL AS VARCHAR2(30)) AS SCOPE_CATALOG,\r\n" +
      "  CAST(NULL AS VARCHAR2(30)) AS SCOPE_SCHEMA,\r\n" +
      "  CAST(NULL AS VARCHAR2(30)) AS SCOPE_TABLE,\r\n" +
      "  CAST(NULL AS NUMBER) AS SOURCE_DATA_TYPE,\r\n" +
      "  'NO' AS IS_AUTOINCREMENT,\r\n" +
      "  'NO' AS IS_GENERATEDCOLUMN\r\n" +
      "FROM ALL_TAB_COLUMNS C" +
      "  LEFT JOIN ALL_TYPES T" +
      "  ON (C.DATA_TYPE = T.TYPE_NAME AND" +
      "      C.DATA_TYPE_OWNER = T.OWNER)" +
      "  LEFT JOIN ALL_COL_COMMENTS CC" +
      "  ON (C.OWNER = CC.OWNER AND " +
      "      C.TABLE_NAME = CC.TABLE_NAME AND " +
      "      C.COLUMN_NAME = CC.COLUMN_NAME)" +
      "WHERE\r\n" +
      sbCondition.toString() +
      "ORDER BY TABLE_CAT, TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION";
    ResultSet rsColumns = null;
    Connection conn = getConnection();
    _il.event("Unwrapped prepared query: "+sSql);
    PreparedStatement pstmt = conn.unwrap(Connection.class).prepareStatement(sSql,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    rsColumns = new OracleMetaColumns(pstmt.executeQuery(),conn,pstmt,1,2,5,6,7,7,9);
    _il.exit(rsColumns);
    return rsColumns;
  } /* getColumns */
  
	/*------------------------------------------------------------------*/
	/** {@inheritDoc} */
	@Override
	public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types)
		throws SQLException
	{
	  _il.enter(catalog,schemaPattern,typeNamePattern,types);
	  ResultSet rsUdts = null;
	  boolean bEmpty = true;
	  if (types != null)
	  {
	    for (int i = 0; i < types.length; i++)
	    {
	      if (types[i] == Types.STRUCT)
	        bEmpty = false;
	    }
	  }
	  else
	    bEmpty = false;
	  
		StringBuilder sbCondition = new StringBuilder("T.TYPE_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(typeNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    if (schemaPattern != null)
    {
      sbCondition.append(" AND T.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
		if (bEmpty)
      sbCondition.append(" AND T.TYPECODE = NULL\r\n"); // empty result
    else
      sbCondition.append(" AND (T.TYPECODE = 'OBJECT' OR T.TYPECODE = 'ANYDATA')\r\n");
	    
    String sSql = "SELECT\r\n" +
  		"NULL as TYPE_CAT,\r\n" +
			"T.OWNER as TYPE_SCHEM,\r\n" +
			"T.TYPE_NAME AS TYPE_NAME,\r\n" +
			"NULL as CLASS_NAME,\r\n" +
			String.valueOf(Types.STRUCT) + " as DATA_TYPE,\r\n" +
			"NULL as REMARKS,\r\n" +
			"NULL as BASE_TYPE\r\n" +
 		  "FROM ALL_TYPES T\r\n" +
 		  "WHERE " + sbCondition.toString() + "\r\n" + 
 		  "ORDER BY DATA_TYPE, TYPE_CAT, TYPE_SCHEM, TYPE_NAME";
	
	  Statement stmt = getConnection().createStatement();
	  _il.event("Unwrapped query: "+sSql);
	  rsUdts = stmt.unwrap(Statement.class).executeQuery(sSql);
	  _il.exit(rsUdts);
	  return rsUdts;
	} /* getUDTs */

	/*------------------------------------------------------------------*/
	/** {@inheritDoc} */
	@Override
	public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern)
		throws SQLException
	{
	  _il.enter(catalog,schemaPattern,typeNamePattern,attributeNamePattern);
	  ResultSet rsAttributes = null;
    StringBuilder sbCondition = new StringBuilder();
    sbCondition.append("A.TYPE_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(typeNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    if (schemaPattern != null)
    {
      sbCondition.append("AND A.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
	    
		String sSql = "SELECT\r\n" +
			"NULL as TYPE_CAT,\r\n" +
			"A.OWNER as TYPE_SCHEM,\r\n" +
			"A.TYPE_NAME as TYPE_NAME,\r\n" +
			"A.ATTR_NAME as ATTR_NAME,\r\n" +
      getDataTypeCase("A.ATTR_TYPE_NAME","T.TYPECODE") + " AS DATA_TYPE,\r\n" +
			getTypeNameCase("A.ATTR_TYPE_NAME","A.ATTR_TYPE_OWNER","A.ATTR_TYPE_NAME") + " AS ATTR_TYPE_NAME,\r\n" +
			getSizeCase("A.ATTR_TYPE_NAME","A.LENGTH","A.PRECISION") + " as ATTR_SIZE,\r\n" +
			"A.SCALE as DECIMAL_DIGITS,\r\n" +
			"10 as NUM_PREC_RADIX,\r\n" +
      String.valueOf(DatabaseMetaData.attributeNullableUnknown)+" AS NULLABLE,\r\n" +
			"NULL as REMARKS,\r\n" +
			"NULL as ATTR_DEF,\r\n" +
			"NULL as SQL_DATA_TYPE,\r\n" +
			"NULL as SQL_DATETIME_SUB,\r\n" +
			"NULL as CHAR_OCTET_LENGTH,\r\n" +
			"A.ATTR_NO as ORDINAL_POSITION,\r\n" +
			"'' as IS_NULLABLE,\r\n" +
			"NULL as SCOPE_CATALOG,\r\n" +
			"NULL as SCOPE_SCHEMA,\r\n" +
			"NULL as SCOPE_TABLE,\r\n" +
			"NULL as SOURCES_DATA_TYPE\r\n" +
    	"FROM ALL_TYPE_ATTRS A" +
	    "  LEFT JOIN ALL_TYPES T" +
	    "  ON (A.ATTR_TYPE_NAME = T.TYPE_NAME AND" +
	    "      A.ATTR_TYPE_OWNER = T.OWNER)\r\n" +
    	"WHERE " + sbCondition.toString() + 
      "ORDER BY TYPE_CAT, TYPE_SCHEM, TYPE_NAME, ORDINAL_POSITION";
		
	  Statement stmt = getConnection().createStatement();
	  _il.event("Unwrapped query: "+sSql);
	  rsAttributes = stmt.unwrap(Statement.class).executeQuery(sSql);
	  _il.exit(rsAttributes);
	  return new OracleMetaColumns(rsAttributes,getConnection(),stmt, 1,2,5,6,7,7,8);
	} /* getAttributes */

  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getSuperTables(String catalog, String schemaPattern,
    String tableNamePattern) throws SQLException
  {
    ResultSet rs = null;
    try { rs = super.getSuperTables(catalog, schemaPattern, tableNamePattern); }
    catch(SQLException se) { throw new SQLFeatureNotSupportedException("Querying for super tables are not supported in Oracle!",se); }
    return rs;
  } /* getSuperTables */

	/*------------------------------------------------------------------*/
	/** {@inheritDoc} */
	@Override
	public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) 
		throws SQLException
	{
	  _il.enter(catalog,schemaPattern,typeNamePattern);
	  ResultSet rsSuperTypes = null;
    StringBuilder sbCondition = new StringBuilder("SUPERTYPE_NAME IS NOT NULL\r\n");
    sbCondition.append(" AND TYPE_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(typeNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    if (schemaPattern != null)
    {
      sbCondition.append(" AND OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
	    
    String sSql = "SELECT\r\n" +
			"NULL as TYPE_CAT,\r\n" +
			"OWNER as TYPE_SCHEM,\r\n" +
			"TYPE_NAME AS TYPE_NAME,\r\n" +
			"NULL as SUPERTYPE_CAT,\r\n" +
			"SUPERTYPE_OWNER as SUPERTYPE_SCHEM,\r\n" +
			"SUPERTYPE_NAME AS SUPERTYPE_NAME\r\n" +
 		  "FROM ALL_TYPES\r\n" +
 		  "WHERE " + sbCondition.toString() + "\r\n" +
 		  "ORDER BY TYPE_CAT, TYPE_SCHEM, TYPE_NAME, SUPERTYPE_CAT, SUPERTYPE_SCHEM, SUPERTYPE_NAME";
		
		Statement stmt = getConnection().createStatement();
		_il.event("Unwrapped query: "+sSql);
		rsSuperTypes = stmt.unwrap(Statement.class).executeQuery(sSql);
		_il.exit(rsSuperTypes);
		return rsSuperTypes;
	} /* getSuperTypes */	

	/*------------------------------------------------------------------*/
	/** {@inheritDoc} 
	 * Oracle's programmers really botched this one badly. The original
	 * implementation throws exceptions in an incomprehensible way. 
	 * N.B.: call requires an explicit table name, not just a table name 
	 * pattern as in other methods like getUDTs.
	 */
	@Override
	public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate)
		throws SQLException
	{
	  _il.enter(catalog,schema,table,String.valueOf(unique),String.valueOf(approximate));
    ResultSet rsIndexInfo = null;
	  StringBuilder sbCondition = new StringBuilder("I.TABLE_NAME = ");
	  sbCondition.append(SqlLiterals.formatStringLiteral(table));
    sbCondition.append("\r\n");
    if (schema != null)
    {
      sbCondition.append(" AND I.TABLE_OWNER = ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schema));
      sbCondition.append("\r\n");
    }
    if (unique)
      sbCondition.append(" AND I.UNIQUENESS = 'UNIQUE'\r\n");
	  // ignore approximate
    StringBuilder sbCaseIndexType = new StringBuilder(" CASE I.TABLE_TYPE\r\n");
    sbCaseIndexType.append("  WHEN 'TABLE' THEN ");
    sbCaseIndexType.append(String.valueOf(DatabaseMetaData.tableIndexHashed));
    sbCaseIndexType.append("\r\n");
    sbCaseIndexType.append("  WHEN 'CLUSTER' THEN ");
    sbCaseIndexType.append(String.valueOf(DatabaseMetaData.tableIndexClustered));
    sbCaseIndexType.append("\r\n");
    sbCaseIndexType.append("  ELSE ");
    sbCaseIndexType.append(String.valueOf(DatabaseMetaData.tableIndexOther));
    sbCaseIndexType.append("\r\n");
    sbCaseIndexType.append(" END");
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append("  NULL AS TABLE_CAT,\r\n");
    sbSql.append("  I.TABLE_OWNER AS TABLE_SCHEM,\r\n");
    sbSql.append("  I.TABLE_NAME AS TABLE_NAME,\r\n");
    sbSql.append("  CASE I.UNIQUENESS WHEN 'UNIQUE' THEN 0 ELSE 1 END AS NON_UNIQUE,\r\n");
    sbSql.append("  NULL AS INDEX_QUALIFIER,\r\n");
    sbSql.append("  I.INDEX_NAME AS INDEX_NAME,\r\n");
    sbSql.append("  ");
    sbSql.append(sbCaseIndexType.toString());
    sbSql.append(" AS TYPE,\r\n");
    sbSql.append("  C.COLUMN_POSITION AS ORDINAL_POSITION,\r\n");
    sbSql.append("  C.COLUMN_NAME AS COLUMN_NAME,\r\n");
    sbSql.append("  CASE C.DESCEND WHEN 'Y' THEN 'D' ELSE 'A' END AS ASC_OR_DESC,\r\n");
    sbSql.append("  (SELECT COUNT(*) FROM ALL_IND_COLUMNS CI WHERE CI.INDEX_NAME = I.INDEX_NAME AND CI.INDEX_OWNER = I.OWNER) AS CARDINALITY,\r\n");
    sbSql.append("  I.LEAF_BLOCKS AS PAGES,\r\n");
    sbSql.append("  NULL AS FILTER_CONDITION\r\n");
    sbSql.append("FROM ALL_INDEXES I\r\n");
    sbSql.append("  INNER JOIN ALL_IND_COLUMNS C\r\n");
    sbSql.append("  ON (I.INDEX_NAME = C.INDEX_NAME AND I.OWNER = C.INDEX_OWNER)\r\n");
    sbSql.append("WHERE\r\n");
    sbSql.append(sbCondition.toString());
    sbSql.append("ORDER BY NON_UNIQUE, INDEX_NAME, ORDINAL_POSITION");

    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsIndexInfo = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsIndexInfo);
    return rsIndexInfo;
	} /* getIndexInfo */
	
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getProcedureColumns(String catalog,
    String schemaPattern, String procedureNamePattern,
    String columnNamePattern) throws SQLException
  {
    _il.enter(catalog,schemaPattern,procedureNamePattern,columnNamePattern);
    StringBuilder sbCondition = new StringBuilder("A.DATA_LEVEL = 0\r\n");
    if ((schemaPattern != null) && (!schemaPattern.equals("%")))
    {
      sbCondition.append("AND A.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    if ((procedureNamePattern != null) && (!procedureNamePattern.equals("%")))
    {
      sbCondition.append("AND A.OBJECT_NAME LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(procedureNamePattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    if ((columnNamePattern != null) && (!columnNamePattern.equals("%")))
    {
      sbCondition.append("AND A.ARGUMENT_NAME LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(columnNamePattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    StringBuilder sbCaseColumnType = new StringBuilder("  CASE\r\n");
    sbCaseColumnType.append("    WHEN A.POSITION = 0 THEN ");
    sbCaseColumnType.append(String.valueOf(DatabaseMetaData.procedureColumnReturn));
    sbCaseColumnType.append("\r\n");
    sbCaseColumnType.append("    WHEN A.IN_OUT = 'IN' THEN ");
    sbCaseColumnType.append(String.valueOf(DatabaseMetaData.procedureColumnIn));
    sbCaseColumnType.append("\r\n");
    sbCaseColumnType.append("    WHEN A.IN_OUT = 'OUT' THEN ");
    sbCaseColumnType.append(String.valueOf(DatabaseMetaData.procedureColumnOut));
    sbCaseColumnType.append("\r\n");
    sbCaseColumnType.append("    WHEN A.IN_OUT = 'IN/OUT' THEN ");
    sbCaseColumnType.append(String.valueOf(DatabaseMetaData.procedureColumnInOut));
    sbCaseColumnType.append("\r\n");
    sbCaseColumnType.append("    ELSE ");
    sbCaseColumnType.append(String.valueOf(DatabaseMetaData.procedureColumnUnknown));
    sbCaseColumnType.append("\r\n");
    sbCaseColumnType.append("  END");
    StringBuilder sbSpecificName = new StringBuilder("  CASE\r\n");
    sbSpecificName.append("    WHEN P.PROCEDURE_NAME IS NULL THEN P.OBJECT_NAME\r\n");
    sbSpecificName.append("    ELSE P.PROCEDURE_NAME || '.' || P.OBJECT_NAME\r\n");
    sbSpecificName.append("  END");
    
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append("  NULL AS PROCEDURE_CAT,\r\n");
    sbSql.append("  A.OWNER AS PROCEDURE_SCHEM,\r\n");
    sbSql.append("  A.OBJECT_NAME AS PROCEDURE_NAME,\r\n");
    sbSql.append("  A.ARGUMENT_NAME AS COLUMN_NAME,\r\n");
    sbSql.append(sbCaseColumnType.toString());
    sbSql.append(" AS COLUMN_TYPE,\r\n");
    sbSql.append(getDataTypeCase("A.DATA_TYPE","T.TYPECODE"));
    sbSql.append(" AS DATA_TYPE,\r\n");
    sbSql.append(getTypeNameCase("A.DATA_TYPE","A.TYPE_OWNER","A.TYPE_NAME"));
    sbSql.append(" AS TYPE_NAME,\r\n");
    sbSql.append(getSizeCase("A.DATA_TYPE","A.DATA_LENGTH","A.DATA_PRECISION"));
    sbSql.append(" AS PRECISION,\r\n");
    sbSql.append("  A.DATA_LENGTH AS LENGTH,\r\n");
    sbSql.append("  A.DATA_SCALE AS SCALE,\r\n");
    sbSql.append("  A.RADIX AS RADIX,\r\n");
    sbSql.append("  ");
    sbSql.append(String.valueOf(DatabaseMetaData.columnNullableUnknown));
    sbSql.append(" AS NULLABLE,\r\n");
    sbSql.append("  NULL AS REMARKS,\r\n");
    sbSql.append("  A.DEFAULT_VALUE AS COLUMN_DEF,\r\n");
    sbSql.append("  CAST(NULL AS NUMBER) AS SQL_DATA_TYPE,\r\n");
    sbSql.append("  CAST(NULL AS NUMBER) AS SQL_DATETIME_SUB,\r\n");
    sbSql.append("  A.DATA_LENGTH AS CHAR_OCTET_LENGTH,\r\n");
    sbSql.append("  A.POSITION AS ORDINAL_POSITION,\r\n");
    sbSql.append("  NULL AS IS_NULLABLE,");
    sbSql.append(sbSpecificName.toString());
    sbSql.append(" AS SPECIFIC_NAME\r\n");
    sbSql.append("FROM ALL_OBJECTS O\r\n");
    sbSql.append("  INNER JOIN ALL_PROCEDURES P\r\n");
    sbSql.append("  ON (O.OWNER = P.OWNER AND \r\n");
    sbSql.append("      O.OBJECT_NAME = P.OBJECT_NAME AND\r\n");
    sbSql.append("      O.OBJECT_ID = P.OBJECT_ID)\r\n");
    sbSql.append("  INNER JOIN ALL_ARGUMENTS A\r\n");
    sbSql.append("  ON (O.OWNER = A.OWNER AND \r\n");
    sbSql.append("      O.OBJECT_NAME = A.OBJECT_NAME AND\r\n");
    sbSql.append("      O.OBJECT_ID = A.OBJECT_ID)\r\n");
    sbSql.append("  LEFT JOIN ALL_TYPES T\r\n");
    sbSql.append("  ON (A.TYPE_NAME = T.TYPE_NAME AND\r\n");
    sbSql.append("      A.TYPE_OWNER = T.OWNER)\r\n");
    sbSql.append("WHERE\r\n");
    sbSql.append(sbCondition.toString());
    sbSql.append("ORDER BY PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME, SPECIFIC_NAME, ORDINAL_POSITION");
    
    ResultSet rsProcedureColumns = null;
    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsProcedureColumns = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsProcedureColumns);
    return new OracleMetaColumns(rsProcedureColumns,getConnection(),stmt,1,2,6,7,8,9,10);
  } /* getProcedureColumns */

  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getProcedures(String catalog,
    String schemaPattern, String procedureNamePattern)
    throws SQLException
  {
    _il.enter(catalog,schemaPattern,procedureNamePattern);
    StringBuilder sbCondition = new StringBuilder();
    if ((procedureNamePattern == null) || procedureNamePattern.equals("%"))
    {
      sbCondition.append(getOracleMaintainedCondition(false));
      sbCondition.append("\r\n");
    }
    else
    {
      sbCondition.append("P.OBJECT_NAME LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(procedureNamePattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    if (schemaPattern != null)
    {
      sbCondition.append("AND P.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    StringBuilder sbCaseProcType = new StringBuilder("CASE P.OBJECT_TYPE\r\n");
    sbCaseProcType.append(" WHEN 'FUNCTION' THEN ");
    sbCaseProcType.append(String.valueOf(DatabaseMetaData.procedureReturnsResult));
    sbCaseProcType.append("\r\n");
    sbCaseProcType.append(" WHEN 'PROCEDURE' THEN ");
    sbCaseProcType.append(String.valueOf(DatabaseMetaData.procedureNoResult));
    sbCaseProcType.append("\r\n");
    sbCaseProcType.append("  ELSE ");
    sbCaseProcType.append(String.valueOf(DatabaseMetaData.procedureResultUnknown));
    sbCaseProcType.append("\r\n");
    sbCaseProcType.append("END");
    StringBuilder sbSpecificName = new StringBuilder("CASE\r\n");
    sbSpecificName.append(" WHEN P.PROCEDURE_NAME IS NULL THEN P.OBJECT_NAME\r\n");
    sbSpecificName.append(" ELSE P.PROCEDURE_NAME || '.' || P.OBJECT_NAME\r\n");
    sbSpecificName.append("END");
    
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append("NULL AS PROCEDURE_CAT,\r\n");
    sbSql.append("P.OWNER AS PROCEDURE_SCHEM,\r\n");
    sbSql.append("P.OBJECT_NAME AS PROCEDURE_NAME,\r\n");
    sbSql.append("NULL AS RESERVED1,\r\n");
    sbSql.append("NULL AS RESERVED2,\r\n");
    sbSql.append("NULL AS RESERVED3,\r\n");
    sbSql.append("NULL AS REMARKS,\r\n");
    sbSql.append(sbCaseProcType.toString());
    sbSql.append(" AS PROCEDURE_TYPE,\r\n");
    sbSql.append(sbSpecificName.toString());
    sbSql.append(" AS SPECIFIC_NAME\r\n");
    sbSql.append("FROM ALL_PROCEDURES P\r\n");
    sbSql.append("  INNER JOIN "+sTABLE_ALL_USERS+" "+sALIAS_ALL_USERS+"\r\n");
    sbSql.append("  ON (P.OWNER = "+sALIAS_ALL_USERS+"."+sCOLUMN_USERNAME+")\r\n");
    sbSql.append("WHERE\r\n");
    sbSql.append(sbCondition.toString());
    sbSql.append("ORDER BY PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME, SPECIFIC_NAME");
    
    ResultSet rsProcedures = null;
    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsProcedures = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsProcedures);
    return rsProcedures;
  } /* getProcedures */

  /*------------------------------------------------------------------*/
  /** {@inheritDoc}
   * This returns a LONG for the view query. Make sure no other JDBC method
   * is called between this and the next() statement!
   */
  @Override
  public ResultSet getTables(String catalog, String schemaPattern,
    String tableNamePattern, String[] types) throws SQLException
  {
    _il.enter(catalog,schemaPattern,tableNamePattern,types);
    StringBuilder sbCondition = new StringBuilder("O.OBJECT_NAME LIKE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(tableNamePattern));
    sbCondition.append(" ESCAPE ");
    sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
    sbCondition.append("\r\n");
    if (schemaPattern != null)
    {
      sbCondition.append("AND O.OWNER LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    if (types != null)
    {
      boolean bSystem = false;
      boolean bTemporary = false;
      Set<String> setObjectTypes = new HashSet<String>();
      for (int iType = 0; iType < types.length; iType++)
      {
        String sType = types[iType];
        if (sType.equals(sTABLE))
          setObjectTypes.add(sTABLE);
        else if (sType.equals(sVIEW))
          setObjectTypes.add(sVIEW);
        else if (sType.equals(sSYNONYM))
          setObjectTypes.add(sSYNONYM);
        else if (sType.equals(sGLOBAL_TEMPORARY))
        {
          setObjectTypes.add(sTABLE);
          bTemporary = true;
        }
        else if (sType.equals(sSYSTEM_TABLE))
        {
          setObjectTypes.add(sTABLE);
          bSystem = true;
        }
      }
      if (setObjectTypes.size() > 0)
      {
        sbCondition.append(" AND (\r\n");
        boolean bFirst = true;
        for (Iterator<String> iterObjectType = setObjectTypes.iterator(); iterObjectType.hasNext(); )
        {
          if (bFirst)
            bFirst = false;
          else
            sbCondition.append(" OR ");
          String sObjectType = iterObjectType.next();
          sbCondition.append("(O.OBJECT_TYPE = ");
          sbCondition.append(SqlLiterals.formatStringLiteral(sObjectType));
          if (sObjectType.equals("TABLE"))
            sbCondition.append(" AND NOT T.TABLE_NAME IS NULL");
          else if (sObjectType.equals("VIEW"))
            sbCondition.append(" AND NOT V.VIEW_NAME IS NULL");
          sbCondition.append(")\r\n");
        }
        sbCondition.append(")\r\n");
      }
      sbCondition.append(" AND \r\n");
      sbCondition.append(getOracleMaintainedCondition(bSystem));
      sbCondition.append("\r\n");
      if (!bSystem)
      {
        sbCondition.append("AND O.GENERATED = ");
        sbCondition.append(SqlLiterals.formatStringLiteral("N"));
        sbCondition.append("\r\n");
      }
      String sTemporary = "N";
      if (bTemporary)
        sTemporary = "Y";
      sbCondition.append("AND O.TEMPORARY = ");
      sbCondition.append(SqlLiterals.formatStringLiteral(sTemporary));
      sbCondition.append("\r\n");
    }
    
    StringBuilder sbTableTypeCase = new StringBuilder("CASE\r\n");
    sbTableTypeCase.append("  WHEN O.OBJECT_TYPE = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sTABLE));
    sbTableTypeCase.append(" AND O.TEMPORARY = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral("N"));
    sbTableTypeCase.append(" AND ");
    sbTableTypeCase.append(getOracleMaintainedCondition(false));
    sbTableTypeCase.append(" THEN ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sTABLE));
    sbTableTypeCase.append("\r\n");
    sbTableTypeCase.append("  WHEN O.OBJECT_TYPE = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sTABLE));
    sbTableTypeCase.append(" AND O.TEMPORARY = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral("Y"));
    sbTableTypeCase.append(" AND ");
    sbTableTypeCase.append(getOracleMaintainedCondition(false));
    sbTableTypeCase.append(" THEN ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sGLOBAL_TEMPORARY));
    sbTableTypeCase.append("\r\n");
    sbTableTypeCase.append("  WHEN O.OBJECT_TYPE = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sTABLE));
    sbTableTypeCase.append(" AND O.TEMPORARY = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral("N"));
    sbTableTypeCase.append(" AND ");
    sbTableTypeCase.append(getOracleMaintainedCondition(true));
    sbTableTypeCase.append(" THEN ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sSYSTEM_TABLE));
    sbTableTypeCase.append("\r\n");
    sbTableTypeCase.append("  WHEN O.OBJECT_TYPE = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sVIEW));
    sbTableTypeCase.append(" AND O.TEMPORARY = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral("N"));
    sbTableTypeCase.append(" AND ");
    sbTableTypeCase.append(getOracleMaintainedCondition(false));
    sbTableTypeCase.append(" THEN ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sVIEW));
    sbTableTypeCase.append("\r\n");
    sbTableTypeCase.append("  WHEN O.OBJECT_TYPE = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sSYNONYM));
    sbTableTypeCase.append(" AND O.TEMPORARY = ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral("N"));
    sbTableTypeCase.append(" AND ");
    sbTableTypeCase.append(getOracleMaintainedCondition(false));
    sbTableTypeCase.append(" THEN ");
    sbTableTypeCase.append(SqlLiterals.formatStringLiteral(sSYNONYM));
    sbTableTypeCase.append("\r\n");
    sbTableTypeCase.append("ELSE NULL END");
    
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append("  NULL AS TABLE_CAT,\r\n");
    sbSql.append("  O.OWNER AS TABLE_SCHEM,\r\n");
    sbSql.append("  O.OBJECT_NAME AS TABLE_NAME,\r\n");
    sbSql.append(sbTableTypeCase.toString());
    sbSql.append(" AS TABLE_TYPE,\r\n");
    sbSql.append("  TC.COMMENTS AS REMARKS,\r\n");
    sbSql.append("  NULL AS TYPE_CAT,\r\n");
    sbSql.append("  NULL AS TYPE_SCHEM,\r\n");
    sbSql.append("  NULL AS TYPE_NAME,\r\n");
    sbSql.append("  NULL AS SELF_REFERENCING_COL_NAME,\r\n");
    sbSql.append("  NULL AS REF_GENERATION,\r\n");
    sbSql.append("  V.TEXT AS ");
    sbSql.append(_sQUERY_TEXT);
    sbSql.append("\r\n");
    sbSql.append("FROM ALL_OBJECTS O\r\n");
    sbSql.append("  INNER JOIN "+sTABLE_ALL_USERS+" "+sALIAS_ALL_USERS+"\r\n");
    sbSql.append("  ON (O.OWNER = "+sALIAS_ALL_USERS+"."+sCOLUMN_USERNAME+")\r\n");
    sbSql.append("  LEFT JOIN ALL_TABLES T\r\n");
    sbSql.append("  ON (O.OWNER = T.OWNER AND \r\n");
    sbSql.append("      O.OBJECT_NAME = T.TABLE_NAME AND\r\n");
    sbSql.append("      O.OBJECT_TYPE = 'TABLE' AND\r\n");
    sbSql.append("      T.NESTED = 'NO')\r\n");
    sbSql.append("  LEFT JOIN ALL_VIEWS V\r\n");
    sbSql.append("  ON (O.OWNER = V.OWNER AND\r\n");
    sbSql.append("      O.OBJECT_NAME = V.VIEW_NAME AND\r\n");
    sbSql.append("      O.OBJECT_TYPE = 'VIEW')\r\n");
    sbSql.append("  LEFT JOIN ALL_TAB_COMMENTS TC\r\n");
    sbSql.append("  ON (O.OWNER = TC.OWNER AND\r\n");
    sbSql.append("      O.OBJECT_NAME = TC.TABLE_NAME AND\r\n");
    sbSql.append("      O.OBJECT_TYPE = TC.TABLE_TYPE)\r\n");
    sbSql.append("WHERE\r\n");
    sbSql.append(sbCondition.toString());
    sbSql.append("ORDER BY TABLE_TYPE, TABLE_CAT, TABLE_SCHEM, TABLE_NAME");
    ResultSet rsTables = null;
    Connection conn = getConnection();
    _il.event("Unwrapped prepared query: "+sbSql.toString());
    PreparedStatement pstmt = conn.unwrap(Connection.class).prepareStatement(sbSql.toString(),ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
    rsTables = pstmt.executeQuery();
    rsTables = new OracleResultSet(rsTables,conn,pstmt);
    _il.exit(rsTables);
    return rsTables;
  } /* getTables */

  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getTableTypes()
    throws SQLException
  {
    _il.enter();
    ResultSet rsTableTypes = null;
    StringBuilder sbSql = new StringBuilder("SELECT TABLE_TYPE FROM(\r\n");
    for (int iTableType = 0; iTableType < asTABLE_TYPES.length; iTableType++)
    {
      if (iTableType > 0)
        sbSql.append("UNION\r\n");
      sbSql.append("(SELECT ");
      sbSql.append(SqlLiterals.formatStringLiteral(asTABLE_TYPES[iTableType]));
      sbSql.append(" AS TABLE_TYPE FROM DUAL)\r\n");
    }
    sbSql.append(")");
    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsTableTypes = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsTableTypes);
    return rsTableTypes;
  } /* getTableTypes */
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getSchemas()
    throws SQLException
  {
    _il.enter();
    ResultSet rsSchemas = null;
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append(sALIAS_ALL_USERS+"."+sCOLUMN_USERNAME+" AS TABLE_SCHEM,\r\n");
    sbSql.append("NULL AS TABLE_CATALOG\r\n");
    sbSql.append("FROM "+sTABLE_ALL_USERS+" "+sALIAS_ALL_USERS+"\r\n");
    sbSql.append("WHERE ");
    sbSql.append(getOracleMaintainedCondition(false));
    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsSchemas = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsSchemas);
    return rsSchemas;
  } /* getSchemas */
  
  /*------------------------------------------------------------------*/
  /** {@inheritDoc} */
  @Override
  public ResultSet getSchemas(String catalog, String schemaPattern)
    throws SQLException
  {
    _il.enter(catalog,schemaPattern);
    ResultSet rsSchemas = null;
    StringBuilder sbCondition = new StringBuilder();
    if (schemaPattern != null)
    {
      sbCondition.append("USERNAME LIKE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(schemaPattern));
      sbCondition.append(" ESCAPE ");
      sbCondition.append(SqlLiterals.formatStringLiteral(getSearchStringEscape()));
      sbCondition.append("\r\n");
    }
    StringBuilder sbSql = new StringBuilder("SELECT\r\n");
    sbSql.append("USERNAME AS TABLE_SCHEM,\r\n");
    sbSql.append("NULL AS TABLE_CATALOG\r\n");
    sbSql.append("FROM ALL_USERS\r\n");
    if (schemaPattern != null)
    {
      sbSql.append("WHERE ");
      sbSql.append(sbCondition.toString());
    }
    Statement stmt = getConnection().createStatement();
    _il.event("Unwrapped query: "+sbSql.toString());
    rsSchemas = stmt.unwrap(Statement.class).executeQuery(sbSql.toString());
    _il.exit(rsSchemas);
    return rsSchemas;
  } /* getSchemas */
  
} /* class OracleDatabaseMetaData */
