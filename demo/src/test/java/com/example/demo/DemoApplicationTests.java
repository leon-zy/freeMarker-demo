package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.app.SimpleHibernateDAO;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class DemoApplicationTests {
	private static Logger logger = LoggerFactory.getLogger(DemoApplicationTests.class);
	private SimpleHibernateDAO simpleHibernateDAO;

	@Autowired
	public void setSimpleHibernateDAO(SimpleHibernateDAO simpleHibernateDAO) {
		this.simpleHibernateDAO = simpleHibernateDAO;
	}
	

	@Test
	public void contextLoads() {
		System.out.println("hello world");
	//	simpleHibernateDAO.getSessionFactory();
		Map<String, Object> parameters = new HashMap<String, Object>();
		
//		Map<String,List<Integer>> testList=new HashMap<String,List<Integer>>();
//		testList.put("1993;1130", Arrays.asList(1,2,3));
//		testList.put("1992;0204", Arrays.asList(1,2));
//		
//		parameters.put("testList", testList);
		parameters.put("userid", 1);
	//	parameters.put("password", 123456);
		List a = simpleHibernateDAO.findByNamedQuery("resource.getUser", parameters);
		System.out.println("成功啦");
		System.out.println(Arrays.toString(a.toArray()));
		simpleHibernateDAO.getUsers();
		// DefaultDynamicHibernateStatementBuilder builder = new
		// DefaultDynamicHibernateStatementBuilder();
		// ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		// builder.setResourceLoader(resolver);
		// builder.setFileNames(Arrays.array("classpath:sql/*.sql.xml"));
		// try {
		// builder.init();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}
	

}
