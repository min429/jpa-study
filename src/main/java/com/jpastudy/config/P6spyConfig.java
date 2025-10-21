package com.jpastudy.config;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import jakarta.annotation.PostConstruct;

@Configuration
public class P6spyConfig implements MessageFormattingStrategy {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";

    private static final List<String> SQL_KEYWORDS = Arrays.asList(
        // DML
        "select", "insert", "update", "delete", "values", "into",
        // DDL
        "create", "alter", "drop", "truncate",
        // Clauses & Joins
        "from", "where", "join", "on", "left", "right", "inner", "outer", "full",
        "group by", "order by", "having", "limit", "offset", "union", "distinct",
        // Conditions
        "and", "or", "not", "in", "like", "between", "is", "null", "exists", "case", "when", "then", "else", "end"
    );

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
        String prepared, String sql, String url) {
        sql = formatSql(category, sql);
        Date currentDate = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd HH:mm:ss");

        return formatter.format(currentDate)
            + " | connectionId: " + connectionId
            + " | OperationTime: " + elapsed + "ms"
            + sql;
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.isBlank()) {
            return sql;
        }
        if (Category.STATEMENT.getName().equals(category)) {
            String tmpsql = sql.trim().toLowerCase(Locale.ROOT);
            if (tmpsql.startsWith("create") || tmpsql.startsWith("alter") || tmpsql.startsWith("comment")) {
                sql = FormatStyle.DDL.getFormatter().format(sql);
            } else {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
            }
            sql = highlightSql(sql);
            sql = "|\n Hibernate FormatSql(P6Spy sql, Hibernate format): " + sql + "\n";
        }
        return sql;
    }

    private String highlightSql(String sql) {
        return SQL_KEYWORDS_PATTERN.matcher(sql).replaceAll(match ->
            ANSI_BLUE + match.group(1) + ANSI_RESET
        );
    }

    private static final Pattern SQL_KEYWORDS_PATTERN = Pattern.compile(
        "\\b(" +
            SQL_KEYWORDS.stream()
                .map(Pattern::quote)
                .sorted(Comparator.comparingInt(String::length).reversed()) // 긴 단어가 우선 매칭 (예: "order by"가 "or"보다 먼저)
                .collect(Collectors.joining("|"))
            + ")\\b",
        Pattern.CASE_INSENSITIVE
    );
}
