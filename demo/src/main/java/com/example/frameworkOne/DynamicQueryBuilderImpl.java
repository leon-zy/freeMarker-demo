package com.example.frameworkOne;

import com.insaic.base.utils.StringUtil;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.Validate;
import org.dom4j.Attribute;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DefaultDynamicHibernateStatementBuilder 默认的加载器-将指定配置文件中的sql/hql语句加载到内存中
 * Created by leon_zy on 2018/11/16
 */
@Service
public class DynamicQueryBuilderImpl implements DynamicQueryBuilder, ResourceLoaderAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicQueryBuilderImpl.class);
    private ResourceLoader resourceLoader;
    private Configuration freemarkerConfiguration;
    @Autowired
    @Qualifier("dynamicQueryStatementResolver")
    private EntityResolver entityResolver;
    //文件名称
    private String[] configLocations = new String[0];
    //查询语句名称缓存，不允许重复
    private Set<String> nameCache = new HashSet<String>();
    //模板缓存
    private static Map<String, StatementTemplate> templateCache;

    @Override public Map<String, StatementTemplate> loadAllMateDataMap() {
        return templateCache;
    }

    private void init() throws IOException {
        if(null == this.resourceLoader){
            this.setResourceLoader(new PathMatchingResourcePatternResolver());
        }
        if(configLocations.length == 0){
            this.setConfigLocations(Collections.singletonList("classpath*:/query/*.xml").toArray(new String[1]));
        }
        boolean flag = this.resourceLoader instanceof ResourcePatternResolver;
        for (String file : configLocations) {
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
            Map<String, String> namedHQLQueries = new HashMap<>();
            Map<String, String> namedSQLQueries = new HashMap<>();
            inputSource = new InputSource(resource.getInputStream());
            XmlDocument metadataXml = readMappingDocument(entityResolver, inputSource,
                    new OriginImpl("file", resource.getFilename()));
            if (isDynamicStatementXml(metadataXml)) {
                final Document doc = metadataXml.getDocumentTree();
                final Element dynamicHibernateStatement = doc.getRootElement();
                Iterator rootChildren = dynamicHibernateStatement.elementIterator();
                Map<String, String> includeQueryMap = new HashMap<>();
                while (rootChildren.hasNext()) {
                    final Element element = (Element) rootChildren.next();
                    final String elementName = element.getName();
                    if ("sql-query".equals(elementName)) {
                        putStatementToCacheMap(resource, element, namedSQLQueries, includeQueryMap);
                    } else if ("hql-query".equals(elementName)) {
                        putStatementToCacheMap(resource, element, namedHQLQueries, includeQueryMap);
                    }
                }
                if(!includeQueryMap.isEmpty()){
                    String sqlQueryText;
                    String hqlQueryText;
                    String includeQueryName;
                    for(String sqlQueryName : includeQueryMap.keySet()){
                        sqlQueryText = namedSQLQueries.get(sqlQueryName);
                        hqlQueryText = namedHQLQueries.get(sqlQueryName);
                        includeQueryName = includeQueryMap.get(sqlQueryName);
                        if(StringUtil.isNotBlank(sqlQueryText)){
                            namedSQLQueries.put(sqlQueryName, sqlQueryText + namedSQLQueries.get(includeQueryName));
                        }
                        if(StringUtil.isNotBlank(hqlQueryText)){
                            namedHQLQueries.put(sqlQueryName, sqlQueryText + namedHQLQueries.get(includeQueryName));
                        }
                    }
                }
                this.afterPropertiesSet(namedHQLQueries, namedSQLQueries);
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

    private void putStatementToCacheMap(Resource resource, final Element element, Map<String, String> statementMap, Map<String, String> includeQueryMap)
            throws Exception {
        String sqlQueryName = element.attribute("name").getText();
        Validate.notEmpty(sqlQueryName);
        if (nameCache.contains(sqlQueryName)) {
            throw new RuntimeException("重复的sql-query/hql-query语句定义在文件:" + resource.getURI() + "中，必须保证name的唯一.");
        }
        nameCache.add(sqlQueryName);
        String queryText = element.getText();
        Attribute sqlIncludeQuery = element.attribute("include");
        if(null != sqlIncludeQuery){
            includeQueryMap.put(sqlQueryName, sqlIncludeQuery.getText());
        }
        statementMap.put(sqlQueryName, queryText);
    }

    private static boolean isDynamicStatementXml(XmlDocument xmlDocument) {
        return "dynamic-query".equals(xmlDocument.getDocumentTree().getRootElement().getName());
    }

    public XmlDocument readMappingDocument(EntityResolver entityResolver, InputSource source, Origin origin) {
        return legacyReadMappingDocument(entityResolver, source, origin);
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
            //saxReader.setFeature( "http://apache.org/xml/features/validation/dynamic", true);
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

    private void afterPropertiesSet(Map<String,String> namedHQLQueries, Map<String,String> namedSQLQueries) throws Exception {
        if(null == templateCache || templateCache.isEmpty()){
            if(null == this.freemarkerConfiguration){
                this.freemarkerConfiguration = new Configuration();
            }
            templateCache = new HashMap<>();
            this.freemarkerConfiguration.setNumberFormat("#");
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            this.freemarkerConfiguration.setTemplateLoader(stringLoader);
            this.handlerStatementTemplate(stringLoader, namedHQLQueries, StatementTemplate.TYPE.HQL);
            this.handlerStatementTemplate(stringLoader, namedSQLQueries, StatementTemplate.TYPE.SQL);
        }
    }

    private void handlerStatementTemplate(StringTemplateLoader stringLoader, Map<String,String> namedQueries, StatementTemplate.TYPE type) throws IOException {
        for(Map.Entry<String, String> entry : namedQueries.entrySet()){
            stringLoader.putTemplate(entry.getKey(), entry.getValue());
            templateCache.put(entry.getKey(), new StatementTemplate(type, new Template(entry.getKey(),new StringReader(entry.getValue()),this.freemarkerConfiguration)));
        }
    }

    public Configuration getFreemarkerConfiguration() {
        return freemarkerConfiguration;
    }

    public void setFreemarkerConfiguration(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void setConfigLocations(String[] configLocations) {
        this.configLocations = configLocations;
    }

    public void initialize(){
        try {
            this.init();
        } catch (IOException e) {
            LOGGER.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    public void destroy(){

    }
}