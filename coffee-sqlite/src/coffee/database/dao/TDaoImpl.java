package coffee.database.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

import coffee.database.Pager;
import coffee.database.core.Configuration;
import coffee.database.core.DBUtils;

public class TDaoImpl implements TDao {
	/**
	 * 当前数据库连接
	 */
	protected Connection conn;

	private static Logger log = Logger.getLogger("jdbc");

	static {
		log.setLevel(Level.INFO);
	}

	/**
	 * 获取当前的connection链接
	 */
	@Override
	public Connection currentConnection() {
		return conn;
	}

	/**
	 * 删除实体
	 * 
	 * @param clazz
	 *            ： 实体类型
	 * @param id
	 *            ： 主键Id
	 */
	@Override
	public <T> void delete(long id, Class<T> clazz) throws SQLException {
		try {
			String primaryKey = DBUtils.getPrimaryKeyName(clazz);
			String sql = "delete from " + DBUtils.getTableName(clazz)
					+ " where  " + primaryKey + "=" + id;
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 批量删除实体
	 * 
	 * @param clazz
	 *            ： 实体类型
	 * @param ids
	 *            ：主键数据
	 */
	@Override
	public <T> void deleteBatch(String[] ids, Class<T> clazz)
			throws SQLException {
		if (ids == null || ids.length == 0) {
			return;
		}
		try {
			String sql = "delete from " + DBUtils.getTableName(clazz)
					+ " where " + DBUtils.getPrimaryKeyName(clazz) + "=?";
			conn.setAutoCommit(false);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			for (String id : ids) {
				int idInt = 0;
				try {
					idInt = Integer.parseInt(id);
				} catch (Exception e) {
					continue;
				}
				if (idInt != 0) {
					pstmt.setInt(1, idInt);
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
			conn.commit();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取离线数据集

	@Override
	public CachedRowSet queryForResultSet(String sql) throws SQLException {
		// CachedRowSet crs = new CachedRowSetImpl();
		// Statement stmt = conn.createStatement();
		// ResultSet rs = stmt.executeQuery(sql);
		// crs.populate(rs);
		// rs.close();
		// stmt.close();
		return null;
	}

	/**
	 * 执行指定sql语句
	 */
	@Override
	public int executeUpdate(String sql) throws SQLException {
		int value = 0;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 指定指定文件的脚本
	 * 
	 * @param scriptPath
	 */
	@Override
	public void executeScript(String scriptPath) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(scriptPath));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			String[] sqls = sb.toString().split(";");
			Statement stmt = this.conn.createStatement();
			try {
				this.conn.setAutoCommit(false);
				for (String sql : sqls) {
					stmt.addBatch(sql);
				}
				stmt.executeBatch();
				this.conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				this.conn.rollback();
			}
			stmt.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回 Integer Long String 等基本数据类型的包装类型
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> queryForColumnList(String sql, Class<T> clazz)
			throws SQLException {
		List<T> ls = new ArrayList<T>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				T t = null;
				if (clazz.getSimpleName().equals("Integer")) {
					t = (T) Integer.valueOf(rs.getInt(1));
				} else {
					t = (T) rs.getString(1);
				}
				ls.add(t);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	@Override
	public <T> T queryForColumn(String sql, Class<T> clazz) throws SQLException {
		List<T> ls = this.queryForColumnList(sql, clazz);
		if (ls.size() > 0) {
			return ls.get(0);
		}
		return null;
	}

	/**
	 * 查询，返回实体对象
	 */
	@Override
	public <T> T queryForEntity(Object id, Class<T> clazz) throws SQLException {
		T t = null;
		try {
			t = clazz.newInstance();
			String sql = "select * from " + DBUtils.getTableName(clazz)
					+ " where " + DBUtils.getPrimaryKeyName(clazz) + "= " + id;
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			List<T> ls = DBUtils.processResultSetToList(rs, clazz);
			if (ls == null || ls.size() == 0) {
				t = null;
			} else {
				t = ls.get(0);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

	/**
	 * 查询返回list
	 */
	@Override
	public <T> List<T> queryForList(String sql, Class<T> clazz)
			throws SQLException {
		List<T> ls = new ArrayList<T>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ls = DBUtils.processResultSetToList(rs, clazz);
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ls;
	}

	// 分页查询
	@Override
	public <T> List<T> queryForList(String sql, long start, int size,
			Class<T> clazz) throws SQLException {
		List<T> ls = new ArrayList<T>();
		try {
			Statement stmt = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt.setMaxRows((int) (start + size));
			ResultSet rs = stmt.executeQuery(sql);
			// 设置分页
			rs.first();
			rs.relative((int) start - 1);
			// 处理resultSet 实现分页查询
			ls = DBUtils.processResultSetToList(rs, clazz);
			rs.close();
			stmt.close();
		} catch (Exception e) {
			System.out.println(sql);
			e.printStackTrace();
		}
		return ls;
	}

	/**
	 *	 
	 */
	@Override
	public Object[][] queryForArray(String sql) {
		Object[][] arr = null;
		try {
			Statement stmt = conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stmt.executeQuery(sql);
			rs.last();
			// 记录总数
			int recordCount = rs.getRow();
			int columnCount = rs.getMetaData().getColumnCount();
			arr = new Object[recordCount][columnCount];
			rs.beforeFirst();
			while (rs.next()) {
				for (int i = 0; i < arr.length; i++) {
					for (int j = 0; j < columnCount; j++) {
						arr[i][j] = rs.getObject(j + 1);
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return arr;
	}

	/**
	 * 插入实体
	 */
	@Override
	public <T> void insert(T t) throws SQLException {
		if (t == null) {
			throw new SQLException("插入数据失败，实体为null");
		}
		try {
			String sql = DBUtils.getInsertSql(t, Configuration.dialect);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
			log.info(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入实体；以String形式返回生成的主键
	 * 
	 * @param t
	 *            ：实体对象
	 * @param autoGeneratedKeys
	 *            ：是否返回主键
	 */
	public <T> String insert(T t, boolean autoGeneratedKeys)
			throws SQLException {
		Object pk = null;
		if (t == null) {
			throw new SQLException("插入数据失败，实体为null");
		}
		try {
			String sql = DBUtils.getInsertSql(t, Configuration.dialect);
			Statement stmt = conn.createStatement();
			stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				pk = rs.getString(1);
			}
			stmt.close();
			rs.close();
			log.info(sql);
		} catch (Exception e) {
			e.printStackTrace();
			conn.rollback();
		}
		return pk.toString();
	}

	/**
	 * 批量插入
	 * 
	 * @param entities
	 *            : 实体列表
	 * @throws SQLException
	 */
	@Override
	public <T> void insert(List<T> entities) throws SQLException {
		conn.setAutoCommit(false);
		Statement stmt = conn.createStatement();
		int index = 0;
		for (T t : entities) {
			String sql = null;
			try {
				sql = DBUtils.getInsertSql(t, Configuration.dialect);
				stmt.addBatch(sql);
				if (index++ > 10000) {
					stmt.executeBatch();
					index = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		stmt.executeBatch();
		conn.commit();
		stmt.close();
	}

	/**
	 * 更新实体
	 */
	@Override
	public <T> void update(T t) throws SQLException {
		try {
			String sql = DBUtils.getUpdateSql(t, Configuration.dialect);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> Pager<T> queryForPager(String sql, int offset, int size,
			Class<T> clazz) throws SQLException {
		String countSql = sql.replaceAll("select\\s*.*? from",
				"select count(*) from").replaceAll("\\s?order\\s+?by.+", "");
		if (offset < 0) {
			offset = 0;
		}
		Pager<T> pager = new Pager<T>();
		pager.setItems(this.queryForList(sql, offset, size, clazz));
		pager.setCount(this.queryForColumnList(countSql, Integer.class).get(0));
		pager.setCurpage(offset / size + 1);
		pager.setOffset(offset);
		return pager;
	}

	/**
	 * 关闭数据库连接
	 */
	@Override
	public void close() throws SQLException {
		try {
			this.conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

}
