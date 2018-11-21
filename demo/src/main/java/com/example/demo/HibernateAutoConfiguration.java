package com.example.demo;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManagerFactory;

//@Configuration
//@AutoConfigureAfter(JpaBaseConfiguration.class)
public class HibernateAutoConfiguration {
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Bean
	public SessionFactory sessionFactory() {
		if (entityManagerFactory == null)
			return null;
		if (entityManagerFactory.unwrap(SessionFactory.class) == null) {
			throw new NullPointerException("factory is not a hibernate factory");
		}
		return entityManagerFactory.unwrap(SessionFactory.class);
	}
	// @Autowired
	// private EntityManagerFactory entityManagerFactory;
	//
	// @Bean
	// public SessionFactory getSessionFactory() {
	// return entityManagerFactory.unwrap(SessionFactory.class);
	// }

}
