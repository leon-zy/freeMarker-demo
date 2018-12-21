package com.example.frameworkNew;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import org.hibernate.internal.util.ConfigHelper;
import org.springframework.stereotype.Service;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Serializable;

/**
 * DynamicQueryStatementResolver 查询模型解析器
 * Created by leon_zy on 2018/11/16
 */
@Service
public class DynamicQueryStatementResolver implements EntityResolver, Serializable {
    private static final long serialVersionUID = -8839526094335769114L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicQueryStatementResolver.class);
    private static final String HOP_DYNAMIC_STATEMENT = "http://www.insaic.com/dtd/";

    public InputSource resolveEntity(String publicId, String systemId) {
        InputSource source = null; // returning null triggers default behavior
        if (systemId != null) {
            LOGGER.debug("trying to resolve system-id [" + systemId + "]");
            if (systemId.startsWith(HOP_DYNAMIC_STATEMENT)) {
                LOGGER.debug("recognized hop dynamic statement namespace; attempting to resolve on classpath under com/insaic/rescue/dao/");
                source = resolveOnClassPath(publicId, systemId, HOP_DYNAMIC_STATEMENT);
            }
        }
        return source;
    }

    private InputSource resolveOnClassPath(String publicId, String systemId, String namespace) {
        InputSource source = null;
        String path = "template/ftl/" + systemId.substring(namespace.length());
        InputStream dtdStream = resolveInHibernateNamespace(path);
        if (dtdStream == null) {
            LOGGER.debug("unable to locate [" + systemId + "] on classpath");
            if (systemId.substring(namespace.length()).indexOf("2.0") > -1) {
                LOGGER.error("Don't use old DTDs, read the Hibernate 3.x Migration Guide!");
            }
        } else {
            LOGGER.debug("located [" + systemId + "] in classpath");
            source = new InputSource(dtdStream);
            source.setPublicId(publicId);
            source.setSystemId(systemId);
        }
        return source;
    }

    protected InputStream resolveInHibernateNamespace(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }

    protected InputStream resolveInLocalNamespace(String path) {
        try {
            return ConfigHelper.getUserResourceAsStream(path);
        } catch (Throwable t) {
            return null;
        }
    }
}