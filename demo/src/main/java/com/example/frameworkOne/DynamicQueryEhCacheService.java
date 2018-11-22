package com.example.frameworkOne;

import com.alibaba.fastjson.JSONObject;
import com.insaic.base.Constant;
import com.insaic.base.exception.BusinessException;
import com.insaic.base.exception.ExceptionUtil;
import com.insaic.base.utils.StringUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.SystemException;
import java.net.InetAddress;
import java.util.Map;

/**
 * RescueEhCacheService
 * Created by leon_zy on 2018/11/2
 */
@Service
public class DynamicQueryEhCacheService {
    private final static Logger logger = LoggerFactory.getLogger(DynamicQueryEhCacheService.class);

    @Autowired
    private DynamicQueryBuilder dynamicQueryBuilder;

    @Autowired
    protected CacheManager cacheManager;

    private boolean readyFlag = false;

    /**
     * 处理缓存数据
     */
    public void processMataDataCache() {
        this.handlerMataDataCache();
    }

    /**
     * 获取缓存数据的map
     * @param queryId 缓存类型
     * @return map
     */
    public StatementTemplate getStatementTemplate(String queryId) {
        StatementTemplate result = null;
        if(StringUtil.isNotBlank(queryId)){
            String cacheName = this.getCacheName();
            Cache cache = this.getCacheByName(cacheName);
            if(null != cache){
                Element element = cache.get(queryId);
                if (element != null) {
                    result = (StatementTemplate) element.getObjectValue();
                }
            }
            if(null == result){
                logger.info("From the cache the query result is empty, bought from database queries begin");
                Map<String, StatementTemplate> dataMap = this.handlerMataDataCache();
                if(null != dataMap && !dataMap.isEmpty()){
                    result = dataMap.get(queryId);
                }
                logger.info("From the cache the query result is empty, bought from database queries result is {}", JSONObject.toJSONString(result));
            }
        }
        return result;
    }

    /**
     * 获取缓存数据的总数据
     * @return map
     */
    private Map<String, StatementTemplate> handlerMataDataCache(){
        Map<String, StatementTemplate> dataMap = null;
        if(!this.getReadyFlag()){
            String cacheName = this.getCacheName();
            Cache cache = this.getCacheByName(cacheName);
            if (cache != null) {
                logger.info("********开始加载" + cacheName + "緩存！********");
                dataMap = dynamicQueryBuilder.loadAllMateDataMap();
                if (null != dataMap && !dataMap.isEmpty()) {
                    for (String key : dataMap.keySet()) {
                        Element element = new Element(key, dataMap.get(key));
                        cache.put(element);
                        logger.info("********缓存{}加载成功！value={}********", key, JSONObject.toJSONString(dataMap.get(key)));
                    }
                }
                logger.info("********" + cacheName + "緩存加载成功！********");
            }else{
                throw ExceptionUtil.handleException(new SystemException("未配置加载缓存" + cacheName + "，请检查ehcache配置"));
            }
            this.setReadyFlag(true);
        }
        return dataMap;
    }

    /**
     * 按大类刷新缓存
     * @param cacheName 缓存名称
     * @return str
     */
    protected String resetCacheByCategory(String cacheName) {
        String ipPort = getCurrentIpPort();
        try {
            if (StringUtil.isNotBlank(cacheName)) {
                this.handlerMataDataCache();
            }else{
                throw ExceptionUtil.handleException(new BusinessException("ip端口为：" + ipPort + "的服务，刷新缓存失败，有入参为空。"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return e.getMessage();
        }
        return ipPort + " success";
    }

    /**
     * 获取当前服务的ip和端口
     * @return str
     */
    private String getCurrentIpPort() {
        String ipPort = System.getProperty(Constant.APP_SERVER_ID);
        if (StringUtil.isBlank(ipPort)) {
            try {
                ipPort = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                ExceptionUtil.handleException(e);
            }
        } else {
            ipPort = ipPort.replace('_', ':');
        }

        return ipPort;
    }

    private CacheManager getCacheManager() {
        return cacheManager;
    }

    private Cache getCacheByName(String cacheName) {
        return cacheManager.getCache(cacheName);
    }

    /**
     * 设置初始化标识
     */
    private void setReadyFlag(boolean readyFlag) {
        this.readyFlag = readyFlag;
    }

    /**
     * 获取初始化标识
     * @return boolean
     */
    private boolean getReadyFlag() {
        return readyFlag;
    }

    /**
     * 加载当前所有基础数据
     * @return map
     */
    private String getCacheName() {
        return "DynamicQueryEhCache";
    }
}