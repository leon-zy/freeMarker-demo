package main.java.com.example.demo.app;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.InvalidMappingException;
import org.hibernate.internal.util.xml.ErrorLogger;
import org.hibernate.internal.util.xml.Origin;
import org.hibernate.internal.util.xml.OriginImpl;
import org.hibernate.internal.util.xml.XmlDocument;
import org.hibernate.internal.util.xml.XmlDocumentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 默认的加载器-将指定配置文件中的sql/hql语句加载到内存中
 * 
 * @author 41162
 *
 */
public class DefaultDynamicHibernateStatementBuilder implements DynamicHibernateStatementBuilder, ResourceLoaderAware {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDynamicHibernateStatementBuilder.class);
	private Map<String, String> namedHQLQueries;
	private Map<String, String> namedSQLQueries;
	private String[] fileNames = new String[0];
	private ResourceLoader resourceLoader;
	private EntityResolver entityResolver = new DynamicStatementDTDEntityResolver();
	/**
	 * 查询语句名称缓存，不允许重复
	 */
	private Set<String> nameCache = new HashSet<String>();

	public void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}

	@Override
	public Map<String, String> getNamedHQLQueries() {
		return namedHQLQueries;
	}

	@Override
	public Map<String, String> getNamedSQLQueries() {
		return namedSQLQueries;
	}

	@Override
	public void init() throws IOException {
		namedHQLQueries = new HashMap<String, String>();
		namedSQLQueries = new HashMap<String, String>();
		boolean flag = this.resourceLoader instanceof ResourcePatternResolver;
		for (String file : fileNames) {
			if (flag) {
				Resource[] resources = ((ResourcePatternResolver) this.resourceLoader).getResources(file);
				buildMap(resources);
			} else {
				Resource resource = resourceLoader.getResource(file);
				buildMap(resource);
			}
		}
		// clear name cache
		nameCache.clear();
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	private void buildMap(Resource[] resources)  {
		if (resources == null) {
			return;
		}
		for (Resource resource : resources) {
			buildMap(resource);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private void buildMap(Resource resource) {
		InputSource inputSource = null;
		try {
			inputSource = new InputSource(resource.getInputStream());
			XmlDocument metadataXml = readMappingDocument(entityResolver, inputSource,
					new OriginImpl("file", resource.getFilename()));
			if (isDynamicStatementXml(metadataXml)) {
				final Document doc = metadataXml.getDocumentTree();
				final Element dynamicHibernateStatement = doc.getRootElement();
				Iterator rootChildren = dynamicHibernateStatement.elementIterator();
				while (rootChildren.hasNext()) {
					final Element element = (Element) rootChildren.next();
					final String elementName = element.getName();
					if ("sql-query".equals(elementName)) {
						putStatementToCacheMap(resource, element, namedSQLQueries);
					} else if ("hql-query".equals(elementName)) {
						putStatementToCacheMap(resource, element, namedHQLQueries);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString());
			throw new RuntimeException(e);
		} finally {
			if (inputSource != null && inputSource.getByteStream() != null) {
				try {
					inputSource.getByteStream().close();
				} catch (IOException e) {
					LOGGER.error(e.toString());
					throw new RuntimeException(e);
				}
			}
		}

	}

	private void putStatementToCacheMap(Resource resource, final Element element, Map<String, String> statementMap)
			throws Exception {
		String sqlQueryName = element.attribute("name").getText();
		 Validate.notEmpty(sqlQueryName);
		
		if (nameCache.contains(sqlQueryName)) {
			throw new RuntimeException("重复的sql-query/hql-query语句定义在文件:" + resource.getURI() + "中，必须保证name的唯一.");
		}
		nameCache.add(sqlQueryName);
		String queryText = element.getText();
		statementMap.put(sqlQueryName, queryText);
	}

	private static boolean isDynamicStatementXml(XmlDocument xmlDocument) {
		return "dynamic-hibernate-statement".equals(xmlDocument.getDocumentTree().getRootElement().getName());
	}

	public XmlDocument readMappingDocument(EntityResolver entityResolver, InputSource source, Origin origin) {
		return legacyReadMappingDocument(entityResolver, source, origin);
//		return readMappingDocument( source, origin );
	}

	private XmlDocument legacyReadMappingDocument(EntityResolver entityResolver, InputSource source, Origin origin) {
		// IMPL NOTE : this is the legacy logic as pulled from the old
		// AnnotationConfiguration code

		Exception failure;

		ErrorLogger errorHandler = new ErrorLogger();

		SAXReader saxReader = new SAXReader();
		saxReader.setEntityResolver(entityResolver);
		saxReader.setErrorHandler(errorHandler);
		saxReader.setMergeAdjacentText(true);
		saxReader.setValidation(true);

		Document document = null;
		try {
			// first try with orm 2.1 xsd validation
			setValidationFor(saxReader, "orm_2_1.xsd");
			document = saxReader.read(source);
			if (errorHandler.hasErrors()) {
				throw errorHandler.getErrors().get(0);
			}
			return new XmlDocumentImpl(document, origin.getType(), origin.getName());
		} catch (Exception e) {

			LOGGER.debug("Problem parsing XML using orm 2.1 xsd, trying 2.0 xsd : {}", e.getMessage());

			failure = e;
			errorHandler.reset();

			if (document != null) {
				// next try with orm 2.0 xsd validation
				try {
					setValidationFor(saxReader, "orm_2_0.xsd");
					document = saxReader.read(new StringReader(document.asXML()));
					if (errorHandler.hasErrors()) {
						errorHandler.logErrors();
						throw errorHandler.getErrors().get(0);
					}
					return new XmlDocumentImpl(document, origin.getType(), origin.getName());
				} catch (Exception e2) {

					LOGGER.debug("Problem parsing XML using orm 2.0 xsd, trying 1.0 xsd : {}", e2.getMessage());

					errorHandler.reset();

					if (document != null) {
						// next try with orm 1.0 xsd validation
						try {
							setValidationFor(saxReader, "orm_1_0.xsd");
							document = saxReader.read(new StringReader(document.asXML()));
							if (errorHandler.hasErrors()) {
								errorHandler.logErrors();
								throw errorHandler.getErrors().get(0);
							}
							return new XmlDocumentImpl(document, origin.getType(), origin.getName());
						} catch (Exception e3) {

							LOGGER.debug("Problem parsing XML using orm 1.0 xsd : {}", e3.getMessage());

						}
					}
				}
			}
		}
		throw new InvalidMappingException("Unable to read XML", origin.getType(), origin.getName(), failure);
	}

	private void setValidationFor(SAXReader saxReader, String xsd) {
		try {
			saxReader.setFeature("http://apache.org/xml/features/validation/schema", true);
			// saxReader.setFeature( "http://apache.org/xml/features/validation/dynamic",
			// true );
			if ("orm_2_1.xsd".equals(xsd)) {
				saxReader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
						"http://xmlns.jcp.org/xml/ns/persistence/orm " + xsd);
			} else {
				saxReader.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
						"http://java.sun.com/xml/ns/persistence/orm " + xsd);
			}
		} catch (SAXException e) {
			saxReader.setValidation(false);
		}
	}

}
