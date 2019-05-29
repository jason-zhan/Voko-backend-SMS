package com.adbest.smsmarketingfront.util;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * QueryDsl 查询工具
 */
public class QueryDslTools {
    
    private BooleanBuilder builder;
    
    public static <T extends Double> void between(BooleanBuilder builder, NumberPath<T> path, T max, T min) {
        if (max != null && min != null && max.compareTo(min) >= 0) {
            builder.and(path.between(min, max));
        }
        if (min != null && max == null) {
            builder.and(path.goe(min));
        }
        if (min == null && max != null) {
            builder.and(path.loe(max));
        }
        if (min == null && max == null) {
            builder.and(path.goe(0));
        }
    }
    
    public void isNull(SimpleExpression se) {
        this.builder.and(se.isNull());
    }
    
    public void isNotNull(SimpleExpression se) {
        this.builder.and(se.isNotNull());
    }
    
    public static <T extends Number & Comparable<?>> void ifTrue(BooleanBuilder builder, Boolean bol, NumberPath<T> path, T trueValue, T falseValue) {
        if (bol != null) {
            if (bol) {
                builder.and(path.eq(trueValue));
            } else {
                builder.and(path.eq(falseValue));
            }
        }
    }
    
    public <T extends Number & Comparable<?>> void ifTrue(Boolean bol, NumberPath<T> path, T trueValue, T falseValue) {
        ifTrue(this.builder, bol, path, trueValue, falseValue);
    }
    
    
    public static <T extends Number & Comparable<?>> void eqNotNull(BooleanBuilder builder, NumberPath<T> path, T value) {
        if (value != null) {
            builder.and(path.eq(value));
        }
    }
    
    public <T extends Number & Comparable<?>> void eqNotNull(NumberPath<T> path, T value) {
        eqNotNull(this.builder, path, value);
    }
    
    public static void eqNotNull(BooleanBuilder builder, BooleanPath path, Boolean value) {
        if (value != null) {
            builder.and(path.eq(value));
        }
    }
    
    public void eqNotNull(BooleanPath path, Boolean value) {
        eqNotNull(this.builder, path, value);
    }
    
    public static <T extends Date> void betweenNotNull(BooleanBuilder builder, DateTimePath<T> path, T from, T to) {
        if (from != null && to != null) {
            builder.and(path.between(from, to));
        }
    }
    
    public <T extends Date> void betweenNotNull(DateTimePath<T> path, T from, T to) {
        betweenNotNull(this.builder, path, from, to);
    }
    
    public static <T extends String> void containsNotEmpty(BooleanBuilder builder, StringPath path, T string, boolean caseSensitive) {
        if (!StringUtils.isEmpty(string)) {
            if (caseSensitive) {
                builder.and(path.contains(string));
            } else {
                builder.and(path.containsIgnoreCase(string));
            }
        }
    }
    
    public <T extends String> void containsNotEmpty(boolean caseSensitive, T string, StringPath path) {
        containsNotEmpty(this.builder, path, string, caseSensitive);
    }
    
    public static <T extends String> void containsNotEmpty(BooleanBuilder builder, boolean caseSensitive, T string, StringPath... paths) {
        if (!StringUtils.isEmpty(string)) {
            List<Predicate> expressionList = new ArrayList<>();
            if (caseSensitive) {
                for (StringPath path : paths) {
                    expressionList.add(path.contains(string));
                }
            } else {
                for (StringPath path : paths) {
                    expressionList.add(path.containsIgnoreCase(string));
                }
            }
            builder.and(ExpressionUtils.anyOf(expressionList));
        }
    }
    
    public <T extends String> void containsNotEmpty(boolean caseSensitive, T string, StringPath... paths) {
        containsNotEmpty(this.builder, caseSensitive, string, paths);
    }
    
    public QueryDslTools(BooleanBuilder builder) {
        this.builder = builder;
    }
    
    public BooleanBuilder getBuilder() {
        return builder;
    }
    
    public void setBuilder(BooleanBuilder builder) {
        this.builder = builder;
    }
}
