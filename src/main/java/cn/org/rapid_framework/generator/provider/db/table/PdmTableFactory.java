package cn.org.rapid_framework.generator.provider.db.table;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import cn.org.rapid_framework.generator.GeneratorProperties;
import cn.org.rapid_framework.generator.provider.db.table.model.Column;
import cn.org.rapid_framework.generator.provider.db.table.model.Table;
import cn.org.rapid_framework.generator.util.BeanHelper;
import cn.org.rapid_framework.generator.util.FileHelper;
import cn.org.rapid_framework.generator.util.GLogger;
import cn.org.rapid_framework.generator.util.XMLHelper;
import cn.org.rapid_framework.generator.util.typemapping.JdbcTypeUtils;

public class PdmTableFactory {
	private DbHelper dbHelper = new DbHelper();
	private Connection connection;
	private static PdmTableFactory instance = null;

	private void loadJdbcDriver() {
		String driver = GeneratorProperties.getRequiredProperty("jdbc.driver");
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("not found jdbc driver class:[" + driver + "]", e);
		}
	}

	public static synchronized PdmTableFactory getInstance() {
		if (instance == null)
			instance = new PdmTableFactory();
		return instance;
	}

	public String getCatalog() {
		return GeneratorProperties.getNullIfBlank("jdbc.catalog");
	}

	public String getSchema() {
		return GeneratorProperties.getNullIfBlank("jdbc.schema");
	}

	public static void main(String[] args) throws Exception {
		String fileName = "支付网关.pdm";
		DocumentFactory df = DocumentFactory.getInstance();
		SAXReader reader = new SAXReader(df);
		Document doc = reader.read(new File(fileName));
		List<Table> tables = new ArrayList<Table>();
		List<Node> nodeList = doc.selectNodes("//c:Tables/o:Table");
		//System.out.println(nodeList);

		for (Node tableElement : nodeList) {
			if (tableElement instanceof Element) {
				Node codeNode = tableElement.selectSingleNode("./a:Code");
				Table table = new Table();
				table.setSqlName(codeNode.getStringValue());
				//System.out.println(table);
				tables.add(table);
				List<Node> columnsList = tableElement.selectNodes("./c:Columns/o:Column");
				List<Element> keyList = tableElement.selectNodes("./c:Keys/o:Key/c:Key.Columns/o:Column");
				Set<String> keySet = new HashSet<String>();
				for (Element keyElement : keyList) {
					keySet.add(keyElement.attributeValue("Ref"));
				}
				for (Node columnNode : columnsList) {
					if (columnNode instanceof Element) {
						Element columnElement = (Element) columnNode;
						String id = columnElement.attributeValue("Id");
						int sqlType = 0;
						int decimalDigits = 0;
						int size = 0;
						boolean isPk = false;
						boolean isNullable = false;
						boolean isIndexed = false;
						boolean isUnique = false;
						String defaultValue = "";
						String remarks = "";

						Node sqlTypeNameNode = columnElement.selectSingleNode("./a:DataType");
						Node lengthNode = columnElement.selectSingleNode("./a:Length");
						Node precisionNode = columnElement.selectSingleNode("./a:Precision");
						Node sqlCodeNode = columnElement.selectSingleNode("./a:Code");
						Node nullableNode = columnElement.selectSingleNode("./a:Mandatory");
						Node remarkNode = columnElement.selectSingleNode("./a:Comment");
						Node defaultValueNode = columnElement.selectSingleNode("./a:DefaultValue");

						String sqlTypeName = sqlTypeNameNode.getStringValue();
						sqlType = JdbcTypeUtils.nameToSqlType(sqlTypeName);
						String sqlName = sqlCodeNode.getStringValue();
						if(keySet.contains(id)){
							isPk = true;
						}
						if (lengthNode != null) {
							size = Integer.parseInt(lengthNode.getStringValue());
						}
						if (nullableNode != null && "1".equals(nullableNode.getStringValue())) {
							isNullable = true;
						}
						if (remarkNode != null) {
							remarks = remarkNode.getStringValue();
						}
						if (precisionNode != null) {
							decimalDigits = Integer.parseInt(precisionNode.getStringValue());
						}
						if (defaultValueNode != null) {
							defaultValue = defaultValueNode.getStringValue();
						}

						Column column = new Column(table, sqlType, sqlTypeName, sqlName, size,
								decimalDigits, isPk, isNullable, isIndexed, isUnique, defaultValue,
								remarks);
						BeanHelper.copyProperties(column,
								TableOverrideValuesProvider.getColumnOverrideValues(table, column));
						table.addColumn(column);
					}
				}
			}
		}
		System.out.println(tables);
	}

	public Connection getConnection() throws SQLException {
		String fileName = "";
		DocumentFactory df = DocumentFactory.getInstance();
		SAXReader reader = new SAXReader(df);
		Document doc;
		try {
			doc = reader.read(new File(fileName));
			List nodeList = doc.selectNodes("");
			for (Object e : nodeList) {
				System.out.println(e);
			}
		} catch (DocumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if ((this.connection == null) || (this.connection.isClosed())) {
			loadJdbcDriver();
			this.connection = DriverManager.getConnection(
					GeneratorProperties.getRequiredProperty("jdbc.url"),
					GeneratorProperties.getRequiredProperty("jdbc.username"),
					GeneratorProperties.getProperty("jdbc.password"));
		}
		return this.connection;
	}

	public List getAllTables() throws Exception {
		String fileName = "支付网关.pdm";
		DocumentFactory df = DocumentFactory.getInstance();
		SAXReader reader = new SAXReader(df);
		Document doc = reader.read(new File(fileName));
		List<Table> tables = new ArrayList<Table>();
		List<Node> nodeList = doc.selectNodes("//c:Tables/o:Table");
		//System.out.println(nodeList);

		for (Node tableElement : nodeList) {
			if (tableElement instanceof Element) {
				Node codeNode = tableElement.selectSingleNode("./a:Code");
				Table table = new Table();
				table.setSqlName(codeNode.getStringValue());
				//System.out.println(table);
				tables.add(table);
				List<Node> columnsList = tableElement.selectNodes("./c:Columns/o:Column");
				List<Element> keyList = tableElement.selectNodes("./c:Keys/o:Key/c:Key.Columns/o:Column");
				Set<String> keySet = new HashSet<String>();
				for (Element keyElement : keyList) {
					keySet.add(keyElement.attributeValue("Ref"));
				}
				for (Node columnNode : columnsList) {
					if (columnNode instanceof Element) {
						Element columnElement = (Element) columnNode;
						String id = columnElement.attributeValue("Id");
						int sqlType = 0;
						int decimalDigits = 0;
						int size = 0;
						boolean isPk = false;
						boolean isNullable = false;
						boolean isIndexed = false;
						boolean isUnique = false;
						String defaultValue = "";
						String remarks = "";

						Node sqlTypeNameNode = columnElement.selectSingleNode("./a:DataType");
						Node lengthNode = columnElement.selectSingleNode("./a:Length");
						Node precisionNode = columnElement.selectSingleNode("./a:Precision");
						Node sqlCodeNode = columnElement.selectSingleNode("./a:Code");
						Node nullableNode = columnElement.selectSingleNode("./a:Mandatory");
						Node remarkNode = columnElement.selectSingleNode("./a:Comment");
						Node defaultValueNode = columnElement.selectSingleNode("./a:DefaultValue");

						String sqlTypeName = sqlTypeNameNode.getStringValue();
						sqlType = JdbcTypeUtils.nameToSqlType(sqlTypeName);
						String sqlName = sqlCodeNode.getStringValue();
						if(keySet.contains(id)){
							isPk = true;
						}
						if (lengthNode != null) {
							size = Integer.parseInt(lengthNode.getStringValue());
						}
						if (nullableNode != null && "1".equals(nullableNode.getStringValue())) {
							isNullable = true;
						}
						if (remarkNode != null) {
							remarks = remarkNode.getStringValue();
						}
						if (precisionNode != null) {
							decimalDigits = Integer.parseInt(precisionNode.getStringValue());
						}
						if (defaultValueNode != null) {
							defaultValue = defaultValueNode.getStringValue();
						}

						Column column = new Column(table, sqlType, sqlTypeName, sqlName, size,
								decimalDigits, isPk, isNullable, isIndexed, isUnique, defaultValue,
								remarks);
						BeanHelper.copyProperties(column,
								TableOverrideValuesProvider.getColumnOverrideValues(table, column));
						table.addColumn(column);
					}
				}
			}
		}
		System.out.println(tables);
		return tables;
	}

	public Table getTable(String tableName) throws Exception {
		Table t = _getTable(tableName);
		if ((t == null) && (!tableName.equals(tableName.toUpperCase()))) {
			t = _getTable(tableName.toUpperCase());
		}
		if ((t == null) && (!tableName.equals(tableName.toLowerCase()))) {
			t = _getTable(tableName.toLowerCase());
		}

		if (t == null) {
			throw new RuntimeException("not found table with give name:"
					+ tableName
					+ ((this.dbHelper.isOracleDataBase()) ? " \n databaseStructureInfo:"
							+ getDatabaseStructureInfo() : ""));
		}
		return t;
	}

	private Table _getTable(String tableName) throws SQLException {
		if ((tableName == null) || (tableName.trim().length() == 0)) {
			throw new IllegalArgumentException("tableName must be not empty");
		}
		Connection conn = getConnection();
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet rs = dbMetaData.getTables(getCatalog(), getSchema(), tableName, null);
		if (rs.next()) {
			Table table = createTable(conn, rs);
			return table;
		}
		return null;
	}

	private Table createTable(Connection conn, ResultSet rs) throws SQLException {
		String realTableName = null;
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			String schemaName = (rs.getString("TABLE_SCHEM") == null) ? "" : rs
					.getString("TABLE_SCHEM");
			realTableName = rs.getString("TABLE_NAME");
			String tableType = rs.getString("TABLE_TYPE");
			String remarks = rs.getString("REMARKS");
			if ((remarks == null) && (this.dbHelper.isOracleDataBase())) {
				remarks = getOracleTableComments(realTableName);
			}

			Table table = new Table();
			table.setSqlName(realTableName);
			table.setRemarks(remarks);

			if (("SYNONYM".equals(tableType)) && (this.dbHelper.isOracleDataBase())) {
				table.setOwnerSynonymName(getSynonymOwner(realTableName));
			}

			retriveTableColumns(table);

			table.initExportedKeys(conn.getMetaData());
			table.initImportedKeys(conn.getMetaData());
			BeanHelper.copyProperties(table,
					TableOverrideValuesProvider.getTableOverrideValues(table.getSqlName()));
			return table;
		} catch (SQLException e) {
			throw new RuntimeException("create table object error,tableName:" + realTableName, e);
		}
	}

	private List getAllTables(Connection conn) throws SQLException {
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet rs = dbMetaData.getTables(getCatalog(), getSchema(), null, null);
		List tables = new ArrayList();
		while (rs.next()) {
			tables.add(createTable(conn, rs));
		}
		return tables;
	}

	private String getSynonymOwner(String synonymName) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String ret = null;
		try {
			ps = getConnection().prepareStatement(
					"select table_owner from sys.all_synonyms where table_name=? and owner=?");
			ps.setString(1, synonymName);
			ps.setString(2, getSchema());
			rs = ps.executeQuery();
			if (rs.next()) {
				ret = rs.getString(1);
			} else {
				String databaseStructure = getDatabaseStructureInfo();
				throw new RuntimeException("Wow! Synonym " + synonymName
						+ " not found. How can it happen? " + databaseStructure);
			}
		} catch (SQLException e) {
			String databaseStructure = getDatabaseStructureInfo();

			throw new RuntimeException("Exception in getting synonym owner " + databaseStructure);
		} finally {
			this.dbHelper.close(rs, ps, new Statement[0]);
		}
		return ret;
	}

	private String getDatabaseStructureInfo() {
		ResultSet schemaRs = null;
		ResultSet catalogRs = null;
		String nl = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer(nl);

		sb.append("Configured schema:").append(getSchema()).append(nl);
		sb.append("Configured catalog:").append(getCatalog()).append(nl);
		try {
			schemaRs = getMetaData().getSchemas();
			sb.append("Available schemas:").append(nl);
			while (schemaRs.next())
				sb.append("  ").append(schemaRs.getString("TABLE_SCHEM")).append(nl);
		} catch (SQLException e2) {
			GLogger.warn("Couldn't get schemas", e2);
			sb.append("  ?? Couldn't get schemas ??").append(nl);
		} finally {
			this.dbHelper.close(schemaRs, null, new Statement[0]);
		}
		try {
			catalogRs = getMetaData().getCatalogs();
			sb.append("Available catalogs:").append(nl);
			while (catalogRs.next())
				sb.append("  ").append(catalogRs.getString("TABLE_CAT")).append(nl);
		} catch (SQLException e2) {
			GLogger.warn("Couldn't get catalogs", e2);
			sb.append("  ?? Couldn't get catalogs ??").append(nl);
		} finally {
			this.dbHelper.close(catalogRs, null, new Statement[0]);
		}
		return sb.toString();
	}

	private DatabaseMetaData getMetaData() throws SQLException {
		return getConnection().getMetaData();
	}

	private void retriveTableColumns(Table table) throws SQLException {
		GLogger.trace("-------setColumns(" + table.getSqlName() + ")");

		List primaryKeys = getTablePrimaryKeys(table);
		table.setPrimaryKeyColumns(primaryKeys);

		List indices = new LinkedList();

		Map uniqueIndices = new HashMap();

		Map uniqueColumns = new HashMap();
		ResultSet indexRs = null;
		try {
			if (table.getOwnerSynonymName() != null) {
				indexRs = getMetaData().getIndexInfo(getCatalog(), table.getOwnerSynonymName(),
						table.getSqlName(), false, true);
			} else {
				indexRs = getMetaData().getIndexInfo(getCatalog(), getSchema(), table.getSqlName(),
						false, true);
			}
			while (indexRs.next()) {
				String columnName = indexRs.getString("COLUMN_NAME");
				if (columnName != null) {
					GLogger.trace("index:" + columnName);
					indices.add(columnName);
				}

				String indexName = indexRs.getString("INDEX_NAME");
				boolean nonUnique = indexRs.getBoolean("NON_UNIQUE");

				if ((!nonUnique) && (columnName != null) && (indexName != null)) {
					List l = (List) uniqueColumns.get(indexName);
					if (l == null) {
						l = new ArrayList();
						uniqueColumns.put(indexName, l);
					}
					l.add(columnName);
					uniqueIndices.put(columnName, indexName);
					GLogger.trace("unique:" + columnName + " (" + indexName + ")");
				}
			}
		} catch (Throwable t) {
		} finally {
			this.dbHelper.close(indexRs, null, new Statement[0]);
		}

		List columns = getTableColumns(table, primaryKeys, indices, uniqueIndices, uniqueColumns);

		for (Iterator i = columns.iterator(); i.hasNext();) {
			Column column = (Column) i.next();
			table.addColumn(column);
		}

		if (primaryKeys.size() == 0)
			GLogger.warn("WARNING: The JDBC driver didn't report any primary key columns in "
					+ table.getSqlName());
	}

	private List getTableColumns(Table table, List primaryKeys, List indices, Map uniqueIndices,
			Map uniqueColumns) throws SQLException {
		List columns = new LinkedList();
		ResultSet columnRs = getColumnsResultSet(table);

		while (columnRs.next()) {
			int sqlType = columnRs.getInt("DATA_TYPE");
			String sqlTypeName = columnRs.getString("TYPE_NAME");
			String columnName = columnRs.getString("COLUMN_NAME");
			String columnDefaultValue = columnRs.getString("COLUMN_DEF");

			String remarks = columnRs.getString("REMARKS");
			if ((remarks == null) && (this.dbHelper.isOracleDataBase())) {
				remarks = getOracleColumnComments(table.getSqlName(), columnName);
			}

			boolean isNullable = 1 == columnRs.getInt("NULLABLE");
			int size = columnRs.getInt("COLUMN_SIZE");
			int decimalDigits = columnRs.getInt("DECIMAL_DIGITS");

			boolean isPk = primaryKeys.contains(columnName);
			boolean isIndexed = indices.contains(columnName);
			String uniqueIndex = (String) uniqueIndices.get(columnName);
			List columnsInUniqueIndex = null;
			if (uniqueIndex != null) {
				columnsInUniqueIndex = (List) uniqueColumns.get(uniqueIndex);
			}

			boolean isUnique = (columnsInUniqueIndex != null) && (columnsInUniqueIndex.size() == 1);
			if (isUnique) {
				GLogger.trace("unique column:" + columnName);
			}
			Column column = new Column(table, sqlType, sqlTypeName, columnName, size,
					decimalDigits, isPk, isNullable, isIndexed, isUnique, columnDefaultValue,
					remarks);

			BeanHelper.copyProperties(column,
					TableOverrideValuesProvider.getColumnOverrideValues(table, column));
			columns.add(column);
		}
		columnRs.close();
		return columns;
	}

	private ResultSet getColumnsResultSet(Table table) throws SQLException {
		ResultSet columnRs = null;
		if (table.getOwnerSynonymName() != null)
			columnRs = getMetaData().getColumns(getCatalog(), table.getOwnerSynonymName(),
					table.getSqlName(), null);
		else {
			columnRs = getMetaData()
					.getColumns(getCatalog(), getSchema(), table.getSqlName(), null);
		}
		return columnRs;
	}

	private List<String> getTablePrimaryKeys(Table table) throws SQLException {
		List primaryKeys = new LinkedList();
		ResultSet primaryKeyRs = null;
		if (table.getOwnerSynonymName() != null) {
			primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), table.getOwnerSynonymName(),
					table.getSqlName());
		} else {
			primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), getSchema(),
					table.getSqlName());
		}
		while (primaryKeyRs.next()) {
			String columnName = primaryKeyRs.getString("COLUMN_NAME");
			GLogger.trace("primary key:" + columnName);
			primaryKeys.add(columnName);
		}
		primaryKeyRs.close();
		return primaryKeys;
	}

	private String getOracleTableComments(String table) {
		String sql = "SELECT comments FROM user_tab_comments WHERE table_name='" + table + "'";
		return this.dbHelper.queryForString(sql);
	}

	private String getOracleColumnComments(String table, String column) {
		String sql = "SELECT comments FROM user_col_comments WHERE table_name='" + table
				+ "' AND column_name = '" + column + "'";
		return this.dbHelper.queryForString(sql);
	}

	class DbHelper {
		DbHelper() {
		}

		public void close(ResultSet rs, PreparedStatement ps, Statement[] statements) {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
				for (Statement s : statements)
					s.close();
			} catch (Exception e) {
			}
		}

		public boolean isOracleDataBase() {
			boolean ret = false;
			try {
				ret = PdmTableFactory.this.getMetaData().getDatabaseProductName().toLowerCase()
						.indexOf("oracle") != -1;
			} catch (Exception ignore) {
			}
			return ret;
		}

		public String queryForString(String sql) {
			Statement s = null;
			ResultSet rs = null;
			try {
				s = PdmTableFactory.this.getConnection().createStatement();
				rs = s.executeQuery(sql);
				if (rs.next()) {
					String str = rs.getString(1);
					return str;
				}
				String str = null;

				return str;
			} catch (SQLException e) {
				e.printStackTrace();

				return null;
			} finally {
				close(rs, null, new Statement[] { s });
			}

		}
	}

	public static class TableOverrideValuesProvider {
		private static Map getTableOverrideValues(String tableSqlName) {
			XMLHelper.NodeData nd = getTableConfigXmlNodeData(tableSqlName);
			return (nd == null) ? new HashMap() : nd.getElementMap("sqlName");
		}

		private static Map getColumnOverrideValues(Table table, Column column) {
			XMLHelper.NodeData root = getTableConfigXmlNodeData(table.getSqlName());
			if (root != null) {
				for (XMLHelper.NodeData item : root.childs) {
					if (item.nodeName.equalsIgnoreCase(column.getSqlName())) {
						return item.getElementMap("sqlName");
					}
				}
			}
			return new HashMap();
		}

		private static XMLHelper.NodeData getTableConfigXmlNodeData(String tableSqlName) {
			XMLHelper.NodeData nd = getTableConfigXmlNodeData0(tableSqlName);
			if (nd == null) {
				nd = getTableConfigXmlNodeData0(tableSqlName.toLowerCase());
				if (nd == null) {
					nd = getTableConfigXmlNodeData0(tableSqlName.toUpperCase());
				}
			}
			return nd;
		}

		private static XMLHelper.NodeData getTableConfigXmlNodeData0(String tableSqlName) {
			try {
				File file = FileHelper.getFileByClassLoader("generator_config/table/"
						+ tableSqlName + ".xml");
				GLogger.debug("getTableConfigXml() load nodeData by tableSqlName:" + tableSqlName
						+ ".xml");
				return new XMLHelper().parseXML(file);
			} catch (Exception e) {
				GLogger.debug("not found config xml for table:" + tableSqlName + ", exception:" + e);
			}
			return null;
		}
	}
}