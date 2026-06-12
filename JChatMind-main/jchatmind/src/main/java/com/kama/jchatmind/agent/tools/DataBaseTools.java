/*
 * ??????? -- Agent???????PostgreSQL?????
 * ????(OPTIONAL):??Agent???allowedTools?????
 * ????:???SELECT??,???????(INSERT/UPDATE/DELETE/DROP?)
 * @Component + @Slf4j:Spring????Bean,Lombok????log??
 */
package com.kama.jchatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ??Tool????????,??Spring?JdbcTemplate??SQL??
 * JdbcTemplate?Spring???JDBC??,????????(????????)
 */
@Component
@Slf4j
public class DataBaseTools implements Tool {

    // JdbcTemplate -- ???????(?? + ?? DB ??),Agent ??????????
    private final JdbcTemplate jdbcTemplate;

    // ?? SQL ?????:?????????,?? "SELECT ...; DROP TABLE ..." ??
    private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
            ";\\s*(DROP|DELETE|INSERT|UPDATE|ALTER|CREATE|TRUNCATE|GRANT|REVOKE)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public DataBaseTools(@Qualifier("restrictedJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() { return "dataBaseTool"; }

    @Override
    public String getDescription() {
        return "????????????????,????? PostgreSQL ??????";
    }

    @Override
    public ToolType getType() { return ToolType.OPTIONAL; }

    // @Tool?????AI,AI??SQL????????
    @org.springframework.ai.tool.annotation.Tool(
            name = "databaseQuery",
            description = "??? PostgreSQL ???????(SELECT)?????????????,?????????????????????,????????????????"
    )
    @Transactional(readOnly = true)
    public String query(String sql) {
        try {
            validateSql(sql);
            List<String> rows = executeQuery(sql);
            return formatQueryResult(rows);
        } catch (SecurityException e) {
            log.warn("SQL ??????: {}", e.getMessage());
            return "??:" + e.getMessage();
        } catch (Exception e) {
            log.error("??????: {}", e.getMessage(), e);
            return "??:???? - " + e.getMessage() + "\nSQL: " + sql;
        }
    }

    /**
     * ?? SQL ??????,??? SELECT ??
     * @throws SecurityException ?? SQL ??????
     */
    private void validateSql(String sql) {
        String trimmedSql = sql.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            throw new SecurityException("??? SELECT ???????? SQL: " + sql);
        }
        if (DANGEROUS_SQL_PATTERN.matcher(sql).find()) {
            throw new SecurityException("????????????,????? SELECT ???");
        }
    }

    /**
     * ?? SQL ????????????
     */
    private List<String> executeQuery(String sql) {
        return jdbcTemplate.query(sql, this::processResultSet);
    }

    /**
     * ?? ResultSet,????????????
     */
    private List<String> processResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        if (columnCount == 0) {
            return List.of("??????(??)");
        }

        TableData tableData = extractTableData(rs, metaData, columnCount);
        return formatAsTable(tableData);
    }

    /**
     * ? ResultSet ???????(?????????)
     */
    private TableData extractTableData(ResultSet rs, ResultSetMetaData metaData, int columnCount) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        List<Integer> columnWidths = new ArrayList<>();

        // ????,?????????
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            columnNames.add(columnName);
            columnWidths.add(columnName.length());
        }

        // ?????,??????
        List<List<String>> dataRows = new ArrayList<>();
        while (rs.next()) {
            List<String> rowData = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                String valueStr = value == null ? "NULL" : value.toString();
                rowData.add(valueStr);

                if (valueStr.length() > columnWidths.get(i - 1)) {
                    columnWidths.set(i - 1, valueStr.length());
                }
            }
            dataRows.add(rowData);
        }

        return new TableData(columnNames, columnWidths, dataRows);
    }

    /**
     * ??????????????(?? + ??? + ???)
     */
    private List<String> formatAsTable(TableData tableData) {
        List<String> rows = new ArrayList<>();
        rows.add(formatTableRow(tableData.columnNames, tableData.columnWidths));
        rows.add(formatTableSeparator(tableData.columnWidths));

        if (tableData.dataRows.isEmpty()) {
            rows.add(formatEmptyRow(tableData.columnWidths));
        } else {
            for (List<String> rowData : tableData.dataRows) {
                rows.add(formatTableRow(rowData, tableData.columnWidths));
            }
        }

        return rows;
    }

    /**
     * ??????(??????)
     */
    private String formatTableRow(List<String> values, List<Integer> widths) {
        StringBuilder row = new StringBuilder();
        row.append("| ");
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            int width = widths.get(i);
            row.append(String.format("%-" + width + "s", value)).append(" | ");
        }
        return row.toString();
    }

    /**
     * ????????
     */
    private String formatTableSeparator(List<Integer> widths) {
        StringBuilder separator = new StringBuilder();
        separator.append("|");
        for (int width : widths) {
            separator.append("-".repeat(width + 2)).append("|");
        }
        return separator.toString();
    }

    /**
     * ???????
     */
    private String formatEmptyRow(List<Integer> widths) {
        int totalWidth = widths.stream().mapToInt(w -> w + 3).sum() - 1;
        return "| " + String.format("%-" + (totalWidth - 2) + "s", "(???)") + " |";
    }

    /**
     * ???????,??????
     */
    private String formatQueryResult(List<String> rows) {
        int dataRowCount = calculateDataRowCount(rows);
        log.info("???? SQL ??,?? {} ???", dataRowCount);
        return "????:\n" + String.join("\n", rows);
    }

    /**
     * ????????(????????)
     */
    private int calculateDataRowCount(List<String> rows) {
        if (rows.size() <= 2) {
            return 0;
        }
        if (rows.get(rows.size() - 1).contains("(???)")) {
            return 0;
        }
        return rows.size() - 2;
    }

    /**
     * ??????,???????????
     */
    private static class TableData {
        final List<String> columnNames;
        final List<Integer> columnWidths;
        final List<List<String>> dataRows;

        TableData(List<String> columnNames, List<Integer> columnWidths, List<List<String>> dataRows) {
            this.columnNames = columnNames;
            this.columnWidths = columnWidths;
            this.dataRows = dataRows;
        }
    }
}
