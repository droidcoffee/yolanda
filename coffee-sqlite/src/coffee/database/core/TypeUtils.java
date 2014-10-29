package coffee.database.core;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;

/**
 * 
 * @author coffee
 */
public class TypeUtils {

	public enum Type {
		Byte, Character, Short, Integer, Long, Float, Double, Boolean, String, Date, BYTE_ARRAY, FormFile, Object
	}

	/**
	 * 获取Field的数据类型
	 * 
	 * @param prop
	 *            :
	 * @return : 返回一个 Type的数据类型
	 */
	public static Type getMappedType(PropertyDescriptor prop) throws Exception {
		return getMappedType(prop.getPropertyType().getSimpleName());
	}

	/**
	 * 返回字段的类型
	 * 
	 * @param field
	 * @return
	 */
	public static Type getMappedType(Field field) throws Exception {
		return getMappedType(field.getType().getSimpleName());
	}

	/**
	 * 支持基本数据类型以及其封装类型
	 */
	public static Type getMappedType(String name) throws Exception {
		name = name.toLowerCase();
		if (name.contains("long")) {
			return Type.Long;
		} else if (name.contains("int")) {
			return Type.Integer;
		} else if (name.contains("date")) {
			return Type.Date;
		} else if (name.contains("string")) {
			return Type.String;
		} else if (name.contains("formfile")) {
			return Type.FormFile;
		}
		return Type.Object;
	}

	/**
	 * 支持基本数据类型以及其封装类型
	 * 
	 * @param clazz
	 *            ： 传入 field.getType对象
	 */
	public static Type getMappedType(Class<?> fieldType) {
		String type = fieldType.getSimpleName().toLowerCase();
		if (type.contains("long")) {
			return Type.Long;
		} else if (type.contains("int")) {
			return Type.Integer;
		} else if (type.contains("float")) {
			return Type.Float;
		} else if (type.contains("double")) {
			return Type.Double;
		} else if (type.contains("date")) {
			return Type.Date;
		} else if (type.contains("string")) {
			return Type.String;
		} else if (type.contains("byte[]")) {
			return Type.BYTE_ARRAY;
		}
		return Type.Object;
	}
}
