package coffee.database;

import java.sql.SQLException;

import coffee.database.dao.TDaoImpl;
/**
 * 相对于TDaoImpl
 * Session主要增加了对Connection的管理(创建、关闭,开启关闭事务)
 * 
 * @author coffee
 * 20122012-11-7上午11:27:12
 */
public class Session extends TDaoImpl{
	/**
	 * 创建Connection
	 * 设置数据库dialect
	 * 进行数据库操作之前必须open连接
	 */
	public void open(){
		//创建一个新连接
		super.conn = SqlConnection.get();
	}
	/**
	 * 开启事务
	 */
	public void beginTransaction(){
		try {
			super.conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 提交事务
	 */
	public void commit(){
		try {
			super.conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 事务回滚
	 */
	public void roolback(){
		try {
			super.conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 *  关闭Connection
	 */
	public void close(){
		try {
			super.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
