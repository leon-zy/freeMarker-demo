package com.example.framework.service;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QueryBuilderImpl
 * Created by leon_zy on 2018/11/19
 */
public class QueryBuilderImpl implements InitializingBean, Serializable {
    private final Logger logger = LoggerFactory.getLogger(QueryBuilderImpl.class);
    private static final long serialVersionUID = 255907691101573453L;

    @Autowired
    private FreeMarkerConfigurationFactoryBean freemarkerConfiguration;

    private List<String> configLocations;

    private DynamicQueryStatementResolver dynamicQueryStatementResolver;

    private DynamicQueryBuilder dynamicStatementBuilder;

    private Configuration configuration;
    /**
     * 模板缓存
     */
    protected Map<String, StatementTemplate> templateCache;

    public void initialize() {
        try {
            this.afterPropertiesSet();
        } catch (Exception e) {
            logger.error("QueryBuilderImpl异常:", e);
        }
    }

    public void destroy() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        configuration = this.getFreemarkerConfiguration().getObject();
        templateCache = new HashMap<>();
        dynamicStatementBuilder = (DynamicQueryBuilder) configuration.getSharedVariable("fragment");
        if(this.dynamicStatementBuilder == null){
            this.dynamicStatementBuilder = new DynamicQueryBuilderImpl();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            dynamicStatementBuilder.setResourceLoader(resolver);
        }
        dynamicStatementBuilder.init();
        Map<String,String> namedHQLQueries = dynamicStatementBuilder.getNamedHQLQueries();
        Map<String,String> namedSQLQueries = dynamicStatementBuilder.getNamedSQLQueries();
        configuration.setNumberFormat("#");
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        for(Map.Entry<String, String> entry : namedHQLQueries.entrySet()){
            stringLoader.putTemplate(entry.getKey(), entry.getValue());
            templateCache.put(entry.getKey(), new StatementTemplate(StatementTemplate.TYPE.HQL,new Template(entry.getKey(),new StringReader(entry.getValue()),configuration)));
        }
        for(Map.Entry<String, String> entry : namedSQLQueries.entrySet()){
            stringLoader.putTemplate(entry.getKey(), entry.getValue());
            templateCache.put(entry.getKey(), new StatementTemplate(StatementTemplate.TYPE.SQL,new Template(entry.getKey(),new StringReader(entry.getValue()),configuration)));
        }
        configuration.setTemplateLoader(stringLoader);
    }

    public void setFreemarkerConfiguration(FreeMarkerConfigurationFactoryBean freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public List<String> getConfigLocations() {
        return configLocations;
    }

    public FreeMarkerConfigurationFactoryBean getFreemarkerConfiguration() {
        return freemarkerConfiguration;
    }

    public void setConfigLocations(List<String> configLocations) {
        this.configLocations = configLocations;
    }
}