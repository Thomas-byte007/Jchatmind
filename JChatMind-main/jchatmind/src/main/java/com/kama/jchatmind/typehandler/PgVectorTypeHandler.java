/*
 * PostgreSQL pgvector????? -- MyBatis???????(float[])???
 * pgvector?PostgreSQL?????,?????[0.1,0.2,0.3]??????
 * ???:Java?float[] ? ?? "[1.0,2.0,3.0]" ??? ? ??pgvector?
 * ???:?pgvector??? "[1.0,2.0,3.0]" ? ??? Java float[]
 */
package com.kama.jchatmind.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedJdbcTypes(JdbcType.OTHER)   // pgvector?JDBC???OTHER(?????)
@MappedTypes(float[].class)        // ???Java?float??
public class PgVectorTypeHandler extends BaseTypeHandler<float[]> {

    // ??:float[] ? "[1.0,2.0,3.0]" ? ps.setObject()
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int j = 0; j < parameter.length; j++) {
            sb.append(parameter[j]);
            if (j < parameter.length - 1) sb.append(',');
        }
        sb.append(']');
        ps.setObject(i, sb.toString(), Types.OTHER);
    }

    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    // ??:"[1.0,2.0,3.0]" ? ??[] ? ?,?? ? ??Float.parseFloat ? float[]
    private float[] parse(String vectorText) {
        if (vectorText == null) return null;
        vectorText = vectorText.replace("[", "").replace("]", "");
        if (vectorText.isBlank()) return new float[0];
        String[] parts = vectorText.split(",");
        float[] arr = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Float.parseFloat(parts[i]);
        }
        return arr;
    }
}