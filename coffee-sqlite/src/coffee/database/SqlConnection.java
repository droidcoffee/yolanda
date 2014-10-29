package coffee.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import coffee.Config;
import coffee.database.core.Configuration;
import coffee.database.core.Configuration.DialectType;

/**
 * 数据库连接类
 * 
 * @author coffee 20122012-11-7上午11:31:24
 */
public class SqlConnection {

	private static String driver = null;
	private static String url = null;
	private static String username = null;
	private static String password = null;

	public static ConnectionPool cp;

	private static void initConnectionPool() throws SQLException {
		try {
			ResourceBundle prop = ResourceBundle.getBundle(Config.DB_JDBC_PROPS);
			url = prop.getString("url");
			username = prop.getString("username");
			password = prop.getString("password");
			driver = prop.getString("driver");
			if (driver.toUpperCase().contains("ORACLE")) {
				Configuration.dialect = DialectType.ORACLE;
			} else if (driver.toUpperCase().contains("MYSQL")) {
				Configuration.dialect = DialectType.MYSQL;
			} else {
				Configuration.dialect = DialectType.HSQLDB;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cp = new ConnectionPool(driver, url, username, password);
	}

	public Connection getConnection() {
		try {
			if (cp == null) {
				initConnectionPool();
			}
			return cp.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Connection get() {
		return new SqlConnection().getConnection();
	}

	// 以下是使用数据源

	// // tomcat 数据源
	// private DataSource ds;
	// // 初始化数据源
	// private void initDataSource() {
	// Context context;
	// try {
	// context = new InitialContext();
	// ds = (DataSource) context.lookup("java:comp/env/jdbc/mysqlds");
	// } catch (NamingException e) {
	// e.printStackTrace();
	// }
	// }
	// public Connection getConnection() {
	// if (ds == null) {
	// initDataSource();
	// }
	// Connection conn = null;
	// try {
	// conn = ds.getConnection();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// return conn;
	// }
}
