package com.example.demo.app;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Repository
// @EnableTransactionManagement
public class SimpleHibernateDAO implements InitializingBean {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHibernateDAO.class);
	/**
	 * 模板缓存
	 */
	protected Map<String, StatementTemplate> templateCache;
	protected DynamicHibernateStatementBuilder dynamicStatementBuilder;

	/**
	 * 查询在xxx.hbm.xml中配置的查询语句
	 * 
	 * @param queryName
	 *            查询的名称
	 * @param parameters
	 *            参数
	 * @return
	 */
	public <X> List<X> findByNamedQuery(final String queryName, final Map<String, ?> parameters) {
		StatementTemplate statementTemplate = templateCache.get(queryName);
		String statement = processTemplate(statementTemplate, parameters);
		System.out.println(statement);
		if (statementTemplate.getType() == StatementTemplate.TYPE.HQL) {
			return this.findByHQL(statement);
		} else {
			return this.findBySQL(statement,parameters.values());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		templateCache = new HashMap<String, StatementTemplate>();
		if (this.dynamicStatementBuilder == null) {
			this.dynamicStatementBuilder = new DefaultDynamicHibernateStatementBuilder();
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			dynamicStatementBuilder.setResourceLoader(resolver);
		}
		dynamicStatementBuilder.setFileNames((String[]) Collections.singletonList("classpath*:/query/*.xml").toArray());
		dynamicStatementBuilder.init();
		Map<String, String> namedHQLQueries = dynamicStatementBuilder.getNamedHQLQueries();
		Map<String, String> namedSQLQueries = dynamicStatementBuilder.getNamedSQLQueries();
		Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		configuration.setNumberFormat("#");
		StringTemplateLoader stringLoader = new StringTemplateLoader();
		for (Entry<String, String> entry : namedHQLQueries.entrySet()) {
			stringLoader.putTemplate(entry.getKey(), entry.getValue());
			templateCache.put(entry.getKey(), new StatementTemplate(StatementTemplate.TYPE.HQL,
					new Template(entry.getKey(), new StringReader(entry.getValue()), configuration)));
		}
		for (Entry<String, String> entry : namedSQLQueries.entrySet()) {
			stringLoader.putTemplate(entry.getKey(), entry.getValue());
			templateCache.put(entry.getKey(), new StatementTemplate(StatementTemplate.TYPE.SQL,
					new Template(entry.getKey(), new StringReader(entry.getValue()), configuration)));
		}
		configuration.setTemplateLoader(stringLoader);
	}

	protected String processTemplate(StatementTemplate statementTemplate, Map<String, ?> parameters) {

		StringWriter stringWriter = new StringWriter();

		try {

			statementTemplate.getTemplate().process(parameters, stringWriter);

		} catch (Exception e) {

			LOGGER.error("处理DAO查询参数模板时发生错误：{}", e.toString());

			// throw new ApplicationException(e);

		}

		return stringWriter.toString();

	}

	/**
	 * 按SQL查询对象列表,并将结果集转换成指定的对象列表
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> findBySQL(final String sql, final Object... values) {
		// return createSQLQuery(sql, values).list();
		return createSQLQuery(sql, values).getResultList();
		// return null;
	}

	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	@SuppressWarnings("unchecked")
	public <X> List<X> findByHQL(final String hql, final Object... values) {
		// return createHQLQuery(hql, values).list();
		return null;
	}

	/**
	 * 根据查询SQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作.
	 * 
	 * @param sqlQueryString
	 *            sql语句
	 * 
	 * @param values
	 *            数量可变的参数,按顺序绑定.
	 */
	@Transactional
	public Query createSQLQuery(final String sqlQueryString, final Object... values) {
		// Query query = getSession().createNativeQuery(sqlQueryString);
		//
		// if (values != null) {
		// for (int i = 0; i < values.length; i++) {
		// query.setParameter(i, values[i]);
		// }
		// }
		// return query;
		EntityManager em = entityManagerFactory.createEntityManager();
		// em.getTransaction().begin();
		Query query = em.createNativeQuery(sqlQueryString);
//		if (values != null) {
//			for (int i = 0; i < values.length; i++) {
//				query.setParameter(i, values[i]);
//			}
//		}
		// em.close();
		//query.executeUpdate();
		return query;
	}

	/**
	 * 根据查询HQL与参数列表创建Query对象. 与find()函数可进行更加灵活的操作.
	 * 
	 * @param values
	 *            命名参数,按名称绑定.
	 */

	public Query createHQLQuery(final String queryString, final Object... values) {
		// Query query = getSession().createQuery(queryString);
		// if (values != null) {
		// query.setProperties(values);
		// }
		// return query;
		return null;
	}

	/**
	 * 取得当前Session.
	 */
	public Session getSession() {
		return getSessionFactory().getCurrentSession();
	}

	/**
	 * 取得sessionFactory.
	 */
	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			if (entityManagerFactory.unwrap(SessionFactory.class) == null) {
				throw new NullPointerException("factory is not a hibernate factory");
			}
			sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
		}
		return sessionFactory;
	}

	protected SessionFactory sessionFactory;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Transactional
	public void getUsers() {
		jdbcTemplate.query("select * from t_user", new RowCallbackHandler() {

			@Override
			public void processRow(ResultSet rs) throws SQLException {
				System.out.println(rs.getString("user_name"));
			}
		});
	}

}
