package com.example.frameworkNew;

import java.util.Map;

/**
 * DynamicHibernateStatementBuilder 动态SQL加载器
 * Created by leon_zy on 2018/11/16
 */
public interface DynamicQueryBuilder {

    Map<String, StatementTemplate> loadAllMateDataMap();

}