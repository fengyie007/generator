package cn.org.rapid_framework.generator.util.typemapping;

public class JdbcTypeUtils {
	public static int nameToSqlType(String name) {
		JdbcType type = null;
		String newName = name.replaceAll("\\([\\d,]*\\)", "");
		if (name.indexOf("(") != -1) {

		}
		if ("VARCHAR2".equals(newName)) {
			type = JdbcType.VARCHAR;
		} else if ("NUMBER".equals(newName)) {
			String tempName = name.replaceAll("\\(\\d+,\\d+\\)", "");
			if (tempName.equals(newName)) {
				type = JdbcType.DOUBLE;
			} else {
				type = JdbcType.INTEGER;
			}
		} else {
			type = JdbcType.valueOf(newName);
		}
		return type.TYPE_CODE;
	}
}
