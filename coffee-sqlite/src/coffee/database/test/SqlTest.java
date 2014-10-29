package coffee.database.test;

import java.sql.SQLException;

import coffee.database.Session;
import coffee.database.core.DBUtils;

public class SqlTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// createTableSql();

		UserBean user = new UserBean();
		user.setUsername("coffee");
		user.setPassword("iop");
		insert(user);
	}

	public static void createTableSql() {
		String sql = DBUtils.generateTableSql(UserBean.class);

		System.out.println(sql);
	}

	public static <T> void insert(T obj) {
		try {
			Session session = new Session();
			session.open();
			session.insert(obj);
			session.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
