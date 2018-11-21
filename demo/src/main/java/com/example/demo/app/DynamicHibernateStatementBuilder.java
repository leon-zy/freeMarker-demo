package com.example.demo.app;

import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Map;

/**
 * 动态sql/hql语句组装器
 * 
 * @author 41162
 *
 */
public interface DynamicHibernateStatementBuilder {
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
