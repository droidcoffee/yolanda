package coffee.database.core;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import coffee.database.annotation.Bean;
import coffee.database.annotation.Column;
import coffee.database.annotation.Id;
import coffee.database.annotation.Transient;

/**
 * 数据库的通用工具类
 * 
 * @author coffee
 * 
 *         20122012-11-7上午11:03:08
 */
public class DBUtils extends TUtils {

	/**
	 * 生成建表语句
	 * 
	 * @param <T>
	 * @param beanClass
	 * @return
	 */
	public static <T> String generateTableSql(Class<T> beanClass) {
		Field[] fields = beanClass.getDeclaredFields();
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS " + getTableName(beanClass)
				+ "(\n");
		for (Field field : fields) {
			Transient nullMap = field.getAnnotation(Transient.class);
			if (nullMap != null) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			String columnName = field.getName();
			if (column != null && !"".equals(column.name())) {
				columnName = column.name();
			}
			sql.append("\t" + columnName);
			switch (TypeUtils.getMappedType(field.getType())) {
			case Integer:
			case Long:
				sql.append(" INTEGER");
				Id id = field.getAnnotation(Id.class);
				if (id != null) {
					sql.append(" PRIMARY KEY");
					if (id != null && id.isAuto()) {
						sql.append(" AUTOINCREMENT ");
					}
				}
				break;
			case Float:
				sql.append(" FLOAT ");
				break;
			case Date:
				sql.append(" DATETIME");
				break;
			case String:
				int len = 255;
				if (column != null) {
					len = column.length();
				}
				sql.append(" VARCHAR(" + len + ")");
				break;
			default:
				break;
			}

			// 非基本数据类型 ， 也非String类型
			if (isPrimaryKey(beanClass, field)) {
				sql.append(" PRIMARY KEY");
			}

			sql.append(",\n");
		}
		sql.deleteCharAt(sql.length() - 2);
		sql.append(")\n");
		return sql.toString();
	}

	/**
	 * 获取表名
	 */
	public static <T> String getTableName(Class<T> beanClass) {
		Bean bean = beanClass.getAnnotation(Bean.class);
		if (bean != null) {
			return bean.name();
		} else {
			return beanClass.getSimpleName().toLowerCase();
		}
	}

	/**
	 * 获取主键名
	 * @param beanClass
	 * @return
	 */
	public static <T> String getPrimaryKeyName(Class<T> beanClass)
	{
		for(Field field : beanClass.getDeclaredFields())
		{
			if(isPrimaryKey(beanClass, field))
			{
				return getColumnName(field);
			}
		}
		return "";
	}
	
	/**
	 * 获取列名
	 */
	public static <T> String getColumnName(Field field) {
		Column column = field.getAnnotation(Column.class);
		if (column != null && column.name().length() > 0) {
			return column.name();
		} else {
			return field.getName();
		}
	}

	/**
	 * 获取列的长度
	 * 
	 * @param clazz
	 *            : 类
	 * @param prop
	 *            : 属性
	 */
	public static <T> int getColumnLength(Class<T> clazz, Field field) {
		Column column = field.getAnnotation(Column.class);
		return column.length();
	}

	/**
	 * 判断列是否可为空 return : false-不可为空 ; true-可为空
	 */
	public static <T> boolean isNull(Class<T> clazz, Field field) {
		Column column = field.getAnnotation(Column.class);
		if (column != null) {
			return column.nullable();
		} else {
			return true;
		}
	}

	/**
	 * 判断指定class的prop是否被映射到数据库
	 * 
	 * @return 如果被映射，返回true ； 没被映射， 即 nullMap != null 返回false
	 */
	private static <T> boolean isTransient(Class<T> clazz, Field field) {
		try {
			Transient nullMap = field.getAnnotation(Transient.class);
			if (nullMap != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断某Class的某字段是不是主键 如果该主键必须被Id属性注解
	 * 
	 * @param entityClass
	 *            : 实体类
	 * @param fieldName
	 *            : 字段名
	 */
	private static <T> boolean isPrimaryKey(Class<T> entityClass, Field field) {
		try {
			Id id = field.getAnnotation(Id.class);
			if (id != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断主键是否是自增类型的
	 * 
	 * @param entityClass
	 *            : 实体类
	 * @param fieldName
	 *            : 字段名
	 */
	private static <T> boolean isGenerationTypeAuto(Class<T> entityClass,
			Field field) {
		try {
			if (isPrimaryKey(entityClass, field) == false) {
				return false;
			}
			Id id = field.getAnnotation(Id.class);
			if (id != null && id.isAuto()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 返回更新实体的命令语句
	 * 
	 * @param <T>
	 * @param t
	 * @param token
	 */
	public static <T> String getUpdateSql(T t,
			Configuration.DialectType dialectType) throws Exception {
		StringBuffer sql = new StringBuffer("update ").append(
				DBUtils.getTableName(t.getClass())).append(" set ");
		long id = 0;
		Class<?> clazz = t.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Transient nullMap = fields[i].getAnnotation(Transient.class);
			if (nullMap != null) {
				continue;
			}
			String columnName = getColumnName(fields[i]);
			Object value = getValue(t, fields[i].getName());
			if (DBUtils.isPrimaryKey(t.getClass(), fields[i])) {
				id = Long.valueOf(value.toString());
				continue;
			} else {
				if (value == null) {
					continue;// 忽略空值，如果想赋null值， 字符串写成 fieldName=""; 数值型
								// fieldName=0
				}
				switch (TypeUtils.getMappedType(fields[i].getType())) {
				case Integer:
				case Long:
				case Float:
				case Double:
					sql.append(columnName).append("=").append(value);
					break;
				case Date:
					value = DateUtils.format(value.toString());
					sql.append(columnName).append("='").append(value)
							.append("'");
					break;
				case String:
					sql.append(columnName).append("='").append(value)
							.append("'");
					break;
				default:
					continue;
				}
				if (value != null && fields.length > i) {
					sql.append(",");
				}
			}
		}
		if (sql.toString().endsWith(",")) {// 除去末尾的 ,
			sql.deleteCharAt(sql.length() - 1);
		}
		sql.append(" where "+getPrimaryKeyName(clazz)+" = ").append(id);
		return sql.toString();
	}

	// 获取插入记录的sql语句
	public static <T> String getInsertSql(T t,
			Configuration.DialectType dialectType) throws Exception {
		StringBuffer sql = new StringBuffer("insert into ").append(
				DBUtils.getTableName(t.getClass())).append(" ");

		Field[] fields = t.getClass().getDeclaredFields();
		// k-v 映射的column名字 : 属性 LinkedHashMap 按照插入的顺序排序
		Map<String, Field> propMap = new LinkedHashMap<String, Field>();
		sql.append("(");

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Transient nullMap = field.getAnnotation(Transient.class);
			if (nullMap != null) {
				continue;
			}
			// 非基本数据类型 ， 也非String类型
			if (!field.getType().isPrimitive()
					&& field.getType() != String.class) {
				continue;
			}
			Column column = field.getAnnotation(Column.class);
			String columnName = field.getName();
			if (column != null && !"".equals(column.name())) {
				columnName = column.name();
			}
			sql.append(columnName);
			propMap.put(columnName, fields[i]);
			if (i + 1 < fields.length) {
				sql.append(",");
			}
		}
		if (sql.toString().endsWith(",")) {
			sql.delete(sql.length() - 1, sql.length());
		}
		sql.append(")values(");
		for (String column : propMap.keySet()) {
			Field field = propMap.get(column);
			if (isPrimaryKey(t.getClass(), field)
					&& isGenerationTypeAuto(t.getClass(), field)) {
				sql.append("null");
			} else {
				Object fieldValue = getValue(t, field.getName());
				switch (TypeUtils.getMappedType(field.getType())) {
				case Integer:
				case Long:
				case Float:
				case Double:
					sql.append(fieldValue);
					break;
				case Date:
					fieldValue = DateUtils.format(fieldValue);
					sql.append(null == fieldValue ? "null" : "'" + fieldValue
							+ "'");
					break;
				case String:
					sql.append(null == fieldValue ? "null" : "'" + fieldValue
							+ "'");
					break;
				default:
					continue;
				}
			}
			sql.append(",");
		}
		sql.deleteCharAt(sql.length() - 1);// 除去sql语句后面最后一个 逗号
		sql.append(")");
		return sql.toString();
	}

	public static <T> List<T> processResultSetToList(ResultSet rs,
			Class<T> clazz) throws Exception {
		List<T> ls = new ArrayList<T>();

		Field[] fields = clazz.getDeclaredFields();
		while (rs.next()) {
			T tt = clazz.newInstance();
			for (Field field : fields) {
				try {
					if (isTransient(clazz, field)) {
						continue;
					}
					/**
					 * 如果Oracle数据库中的类型是 number p.getWriteMethod().invoke(tt, new
					 * Object[] { rs.getObject(p.getName()) }) 会报如下错误
					 * java.lang.IllegalArgumentException: argument type
					 * mismatch --- 另外如果mysql数据库中的bigint 在进行setXxxx(Integer
					 * val)时候也会抛出该异常 也会抛出该异常
					 */
					Object value = null;
					try {
						String fieldName = getColumnName(field);
						switch (TypeUtils.getMappedType(field)) {
						case Long:
							value = Long.valueOf(rs.getLong(fieldName));
							break;
						case Integer:
							value = Integer.valueOf(rs.getInt(fieldName));
							break;
						default:
							value = rs.getObject(fieldName);
							break;
						}
					} catch (Exception e) {// 如果仅仅查询Class的部分字段
						if (e.getMessage().matches(
								"Column\\s+'.+?'\\s+not\\s+found.")) {
							switch (TypeUtils.getMappedType(field)) {
							case Long:
							case Integer:
								value = 0;
								break;
							default:
								value = null;
								break;
							}
						}
					}
					field.setAccessible(true);
					field.set(tt, value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
			ls.add(tt);
		}
		return ls;
	}
}
