package com.example.framework.service;

import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Map;

/**
 * DynamicHibernateStatementBuilder 动态SQL加载器
 * Created by leon_zy on 2018/11/16
 */
public interface DynamicQueryBuilder {
    /**
     * hql语句map
     *
     * @return
     */
    Map<String, String> getNamedHQLQueries();

    /**
     * sql语句map
     *
     * @return
     */
    Map<String, String> getNamedSQLQueries();

    /**
     * 初始化
     *
     * @throws IOException
     */
    void init() throws IOException;

    void setFileNames(String[] fileNames);

    void setResourceLoader(ResourceLoader resourceLoader);
}