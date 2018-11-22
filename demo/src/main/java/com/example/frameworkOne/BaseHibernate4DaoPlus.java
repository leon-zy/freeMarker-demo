package com.example.frameworkOne;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.google.common.collect.Sets;
import com.insaic.base.dao.jpa.BaseHibernate4Dao;
import com.insaic.base.exception.BusinessException;
import com.insaic.base.exception.SystemException;
import com.insaic.base.utils.Collections3;
import com.insaic.base.utils.DateUtil;
import com.insaic.base.utils.Reflections;
import com.insaic.base.utils.StringUtil;
import com.insaic.base.web.mo.WebPageResult;
import com.insaic.common.code.model.PageParamMO;
import com.insaic.common.constants.CodeMsgConstants;
import com.insaic.common.util.CommonDataUtils;
import com.insaic.common.util.QueryPageResultUtils;
import com.insaic.rescue.annotation.NamedQueryData;
import com.insaic.rescue.constants.RescueConstants;
import com.insaic.rescue.utils.DataSecretUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BaseDAOHibernateImpl
 * Created by leon_zy on 2018/11/16
 */
@Repository
public class BaseHibernate4DaoPlus<E extends Serializable> extends BaseHibernate4Dao<E> {
    private static final Logger logger = LoggerFactory.getLogger(BaseHibernate4DaoPlus.class);
    @PersistenceContext
    private EntityManager em;
    private static SessionFactory sf;
    @Autowired
    private DynamicQueryEhCacheService dynamicQueryEhCacheService;

    private static final String QUERY_ID = "queryId";
    private static final String PARAMETERS = "parameters";
    private static final String RETURN_TYPE = "returnType";
    private static final String TYPE_NAME = "typeName";
    private static final String serialVersionUID = "serialVersionUID";
    private static final String ARRAY_LIST = "ArrayList";
    private static final String HASH_SET = "HashSet";

    /**
     * 查询数据
     * @param params 参数
     * @param <T> 泛型
     * @return t
     */
    public <T> T loadDataByParam(Object... params) {
        String errorMsg;
        Class<?> returnClazz;
        Class<?> returnSubClazz = null;
        Map<String, ?> filedMap;
        try {
            StackTraceElement[] element = Thread.currentThread().getStackTrace();
            filedMap = this.getAnnotationFiledMap(element[2].getClassName(), element[2].getMethodName(), params);
            returnClazz = (Class<?>)filedMap.get(RETURN_TYPE);
            if(List.class.equals(returnClazz) || Set.class.equals(returnClazz)){
                String str = (String)filedMap.get(TYPE_NAME);
                if(StringUtil.isNotBlank(str)){
                    String className = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                    returnSubClazz = Class.forName(className);
                }
            }
        } catch (BusinessException e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg, e);
            throw new BusinessException(errorMsg, e);
        } catch (Exception e) {
            errorMsg = CodeMsgConstants.QUERY_ERROR_MESSAGE;
            logger.error(errorMsg, e);
            throw new BusinessException(errorMsg, e);
        }
        return this.loadDataListByParamList((String)filedMap.get(QUERY_ID), (Map<String, Object>)filedMap.get(PARAMETERS), returnClazz, returnSubClazz);
    }

    /**
     * 分页查询数据
     * @param paramMO 参数
     * @param <T> 泛型
     * @return WebPageResult
     */
    public <T> WebPageResult<T> queryDataByParam(PageParamMO paramMO) {
        String errorMsg;
        Class<?> returnClazz;
        Class<?> returnSubClazz = null;
        Map<String, ?> filedMap;
        try {
            paramMO.setPageNumber(null == paramMO.getPageNumber() ? 0 : paramMO.getPageNumber());
            paramMO.setPageSize(null != paramMO.getPageSize() && 0 != paramMO.getPageSize() ? paramMO.getPageSize() : 10);
            StackTraceElement[] element = Thread.currentThread().getStackTrace();
            filedMap = this.getAnnotationFiledMap(element[2].getClassName(), element[2].getMethodName(), paramMO);
            returnClazz = (Class<?>)filedMap.get(RETURN_TYPE);
            if(WebPageResult.class.equals(returnClazz)){
                String str = (String)filedMap.get(TYPE_NAME);
                if(StringUtil.isNotBlank(str)){
                    String className = str.substring(str.indexOf("<") + 1, str.indexOf(">"));
                    returnSubClazz = Class.forName(className);
                }
            }
        } catch (BusinessException e) {
            errorMsg = e.getMessage();
            logger.error(errorMsg, e);
            throw new BusinessException(errorMsg, e);
        } catch (Exception e) {
            errorMsg = CodeMsgConstants.QUERY_ERROR_MESSAGE;
            logger.error(errorMsg, e);
            throw new BusinessException(errorMsg, e);
        }
        return this.queryDataListByParamList((String)filedMap.get(QUERY_ID), (Map<String, Object>)filedMap.get(PARAMETERS), paramMO, returnSubClazz);
    }

    /**
     * 获取注解的属性值
     * @param className 类名
     * @param methodName 方法名
     * @return 注解
     * @throws Exception 异常
     */
    private Map<String, Object> getAnnotationFiledMap(String className, String methodName, Object... params) throws Exception {
        Map<String, Object> result = new HashMap<>();
        NamedQueryData annotation = null;
        Class<?> parentClass = Class.forName(className);
        Method method;
        Class<?>[] c = null;
        if(null != params){//存在
            int len = params.length;
            c = new Class[len];
            for (int i = 0; i < len; ++i) {
                c[i] = params[i].getClass();
            }
        }
        if(null != c && c.length == 1){
            String clazzName = c[0].getSimpleName();
            Class<?> parameterType = ARRAY_LIST.equals(clazzName) ? List.class : HASH_SET.equals(clazzName) ? Set.class : c[0];
            method = parentClass.getDeclaredMethod(methodName, parameterType);
        }else{
            method = parentClass.getDeclaredMethod(methodName, c);
        }
        // 获取该方法的注解实例
        if (method.isAnnotationPresent(NamedQueryData.class)) {
            annotation = method.getAnnotation(NamedQueryData.class);
        }
        if(null == annotation || StringUtil.isBlank(annotation.queryId())){
            throw new BusinessException("查询注解NamedQueryData的queryId不能为空！");
        }
        result.put(QUERY_ID, annotation.queryId());
        result.put(PARAMETERS, this.handlerParameters(annotation.parameters(), params));
        result.put(RETURN_TYPE, annotation.returnClz().length > 0 ? annotation.returnClz()[0] : method.getReturnType());
        result.put(TYPE_NAME, method.getGenericReturnType().getTypeName());
        return result;
    }

    private Map<String, Object> handlerParameters(String[] parameters, Object... params) throws Exception {
        Map<String, Object> parametersMap = new HashMap<>();
        if(null != params){
            String className;
            for(int i = 0; i < params.length; ++i){
                className = params[i].getClass().getSimpleName();
                if (DataSecretUtils.validFieldBaseFlag(params[i])
                        || ARRAY_LIST.equals(className) || HASH_SET.equals(className)) {
                    parametersMap.put(parameters[i], params[i]);
                }else{
                    for (Field field : Reflections.getAllFields(params[i].getClass())) {
                        field.setAccessible(true);
                        if(!serialVersionUID.equals(field.getName())){
                            parametersMap.put(field.getName(), field.get(params[i]));
                        }
                    }
                }
            }
        }
        return parametersMap;
    }

    /**
     * 查询数据
     * @param queryId 查询sql名称
     * @param parameters 查询参数
     * @param clazz 返回信息类
     * @return  list
     */
    private <T> T loadDataListByParamList(String queryId, Map<String, ?> parameters, Class<?> clazz, Class<?> clazzEntry){
        T result = null;
        List<Map<String, Object>> dataMaps = this.findByNamedQueryList(queryId, parameters);
        if(Collections3.isNotEmpty(dataMaps)){
            if(List.class.equals(clazz) || Set.class.equals(clazz)){
                result = this.handlerCollectionData(dataMaps, clazz, clazzEntry);
            }else if(!clazz.getSimpleName().equals("void")){
                result = this.handlerUniqueData(dataMaps.get(0), clazz);
            }
        }
        return result;
    }

    /**
     * 集合返回类型赋值
     * @param dataMaps 查询值
     * @param clazz 集合类
     * @param clazzEntry 返回类
     * @param <T> 泛型
     * @return 集合
     */
    private <T> T handlerCollectionData(List<Map<String, Object>> dataMaps, Class<?> clazz, Class<?> clazzEntry){
        T result;
        if (validClassTypeFlag(clazzEntry)) {
            Collection coll;
            if(List.class.equals(clazz)){
                coll = new ArrayList();
            }else{
                coll = Sets.newHashSet();
            }
            for(Map<String, Object> map : dataMaps){
                coll.addAll(map.values());
            }
            result = (T)coll;
        }else{
            result = (T)CommonDataUtils.getDataResultList(dataMaps, clazzEntry);
        }
        return result;
    }

    /**
     * 获取唯一数据对象
     * @param dataMap 查询数据
     * @param clazz 返回类
     * @param <T> 泛型
     * @return 唯一数据对象
     */
    private <T> T handlerUniqueData(Map<String, Object> dataMap, Class<?> clazz){
        T result = null;
        if (validClassTypeFlag(clazz)) {
            for(Object obj : dataMap.values()){
                if(null != obj && !"".equals(obj)){
                    result = (T)replaceClassTypeValue(clazz, StringUtil.toString(obj));
                    break;
                }
            }
        }else{
            result = (T)CommonDataUtils.getDataResult(dataMap, clazz);
        }
        return result;
    }

    /**
     * 校验对象是否为基础类型
     * @param clazz 对象
     * @return boolean
     */
    private static Boolean validClassTypeFlag(Class<?> clazz){
        return clazz.equals(String.class) || clazz.equals(Integer.class)
                || clazz.equals(Double.class) || clazz.equals(Float.class)
                || clazz.equals(Long.class) || clazz.equals(Date.class) || clazz.equals(BigDecimal.class);
    }

    /**
     * 校验对象是否为基础类型
     * @param clazz 对象
     * @return boolean
     */
    private static <T> T replaceClassTypeValue(Class<T> clazz, String value){
        T result = null;
        if(StringUtil.isNotBlank(value)){
            if(clazz.equals(String.class)){
                result = (T) value;
            }else if(clazz.equals(Integer.class)){
                result = (T) Integer.valueOf(value);
            }else if(clazz.equals(Double.class)){
                result = (T) Double.valueOf(value);
            }else if(clazz.equals(Float.class)){
                result = (T) Float.valueOf(value);
            }else if(clazz.equals(Long.class)){
                result = (T) Long.valueOf(value);
            }else if(clazz.equals(Date.class)){
                result = (T) DateUtil.stringToDate(value);
            }else if(clazz.equals(BigDecimal.class)){
                result = (T) new BigDecimal(value);
            }
        }
        return result;
    }

    /**
     * 分页查询数据
     * @param queryId 查询sql名称
     * @param parameters 查询参数
     * @param clazzEntry 返回信息类
     * @return  list
     */
    private <T> WebPageResult<T> queryDataListByParamList(String queryId, Map<String, ?> parameters, PageParamMO paramMO, Class<?> clazzEntry){
        PageRequest pageRequest = QueryPageResultUtils.handlerPageParam(paramMO);
        List<BigDecimal> count = this.queryByNamedQueryList(queryId, parameters, null);
        List<Map<String, Object>> dataMaps = this.queryByNamedQueryList(queryId, parameters, pageRequest);
        List results = CommonDataUtils.getDataResultList(dataMaps, clazzEntry);
        Page<T> page = new PageImpl(results, pageRequest, count.get(0).longValue());
        return QueryPageResultUtils.getWebPageResult(page);
    }

    /**
     * 按HQL查询对象列表.
     * @param values 命名参数,按名称绑定.
     */
    @SuppressWarnings("unchecked")
    private <X> List<X> findByHQL(final String hql, final Map<String, ?> values) {
        return createHqlLoad(hql, values).getResultList();
    }

    /**
     * 按SQL查询对象列表.
     * @param sql SQL查询语句
     * @param values 命名参数,按名称绑定.
     */
    @SuppressWarnings("unchecked")
    private <X> List<X> findBySQL(final String sql, final Map<String, ?> values) {
        return createSqlLoad(sql, values).getResultList();
    }

    /**
     * 查询在xxx.hbm.xml中配置的查询语句
     * @param queryName 查询的名称
     * @param parameters 参数
     * @return list
     */
    private <X> List<X> findByNamedQueryList(final String queryName, final Map<String, ?> parameters) {
        StatementTemplate statementTemplate = dynamicQueryEhCacheService.getStatementTemplate(queryName);
        String statement = processTemplate(statementTemplate,parameters);
        if(statementTemplate.getType() == StatementTemplate.TYPE.HQL){
            return this.findByHQL(statement, parameters);
        }else{
            return this.findBySQL(statement, parameters);
        }
    }

    /**
     * 按HQL查询对象列表
     * @param hql HQL查询语句
     * @param values 命名参数,按名称绑定
     * @param pageRequest 分页参数
     */
    @SuppressWarnings("unchecked")
    private <X> List<X> queryByHQL(final String hql, final Map<String, ?> values, Pageable pageRequest) {
        return createHqlQuery(hql, values, pageRequest).getResultList();
    }

    /**
     * 按SQL查询对象列表
     * @param sql SQL查询语句
     * @param values 命名参数,按名称绑定
     * @param pageRequest 分页参数
     */
    @SuppressWarnings("unchecked")
    private <X> List<X> queryBySQL(final String sql, final Map<String, ?> values, Pageable pageRequest) {
        return createSqlQuery(sql, values, pageRequest).getResultList();
    }

    /**
     * 查询在xxx.hbm.xml中配置的查询语句
     * @param queryName 查询的名称
     * @param parameters 参数
     * @param pageRequest 分页参数
     * @return list
     */
    private <X> List<X> queryByNamedQueryList(final String queryName, final Map<String, ?> parameters, Pageable pageRequest) {
        StatementTemplate statementTemplate = dynamicQueryEhCacheService.getStatementTemplate(queryName);
        String statement = processTemplate(statementTemplate,parameters);
        if(null == pageRequest){
            statement = "select count(1) from ( " + statement + " )";
        }
        if(statementTemplate.getType() == StatementTemplate.TYPE.HQL){
            return this.queryByHQL(statement, parameters, pageRequest);
        }else{
            return this.queryBySQL(statement, parameters, pageRequest);
        }
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     * 与find()函数可进行更加灵活的操作.
     * @param queryString 查询hql
     * @param values 命名参数,按名称绑定
     */
    private Query createHqlLoad(final String queryString, final Map<String, ?> values) {
        Query query = this.em.createQuery(queryString);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.handlerQueryParameter(queryString, values, query);
        return query;
    }

    /**
     * 根据查询SQL与参数列表创建Query对象.
     * 与find()函数可进行更加灵活的操作.
     * @param queryString SQL语句
     * @param values 命名参数,按名称绑定.
     */
    private Query createSqlLoad(final String queryString, final Map<String, ?> values) {
        Query query = this.em.createNativeQuery(queryString);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.handlerQueryParameter(queryString, values, query);
        return query;
    }

    /**
     * 根据查询HQL与参数列表创建Query对象.
     * 与find()函数可进行更加灵活的操作.
     * @param values 命名参数,按名称绑定.
     */
    private Query createHqlQuery(final String queryString, final Map<String, ?> values, Pageable pageRequest) {
        Query query = this.em.createQuery(queryString);
        this.handlerQueryPageResult(query, pageRequest);
        this.handlerQueryParameter(queryString, values, query);
        return query;
    }

    /**
     * 根据查询SQL与参数列表创建Query对象.
     * 与find()函数可进行更加灵活的操作.
     * @param queryString SQL语句
     * @param values 命名参数,按名称绑定.
     */
    private Query createSqlQuery(final String queryString, final Map<String, ?> values, Pageable pageRequest) {
        Query query = this.em.createNativeQuery(queryString);
        this.handlerQueryPageResult(query, pageRequest);
        this.handlerQueryParameter(queryString, values, query);
        return query;
    }

    private void handlerQueryPageResult(Query query, Pageable pageRequest){
        SQLQuery sqlQuery = query.unwrap(SQLQuery.class);
        if(null != pageRequest){
            query.setFirstResult(pageRequest.getOffset());
            query.setMaxResults(pageRequest.getPageSize());
            sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        }
    }

    /**
     * 处理查询参数
     * @param queryString 查询sql
     * @param values 值
     * @param query 查询
     */
    private void handlerQueryParameter(final String queryString, final Map<String, ?> values, Query query){
        if (values != null) {
            for(String key : values.keySet()){
                if(null != values.get(key) && queryString.indexOf(RescueConstants.COLON_EN + key) > 0){
                    query.setParameter(key, values.get(key));
                }
            }
        }
    }

    /**
     * 初始化对象.
     * 使用load()方法得到的仅是对象Proxy, 在传到View层前需要进行初始化.
     * 如果传入entity, 则只初始化entity的直接属性,但不会初始化延迟加载的关联集合和属性.
     * 如需初始化关联属性,需执行:
     * Hibernate.initialize(user.getRoles())，初始化User的直接属性和关联集合.
     * Hibernate.initialize(user.getDescription())，初始化User的直接属性和延迟加载的Description属性.
     */
    /*public void initProxyObject(Object proxy) {
        Hibernate.initialize(proxy);
    }*/

    /**
     * Flush当前Session.
     */
    public void flush() {
        getSession().flush();
    }

    /**
     * 获取模板信息
     * @param statementTemplate 模板
     * @param parameters 参数
     * @return str
     */
    private String processTemplate(StatementTemplate statementTemplate,Map<String, ?> parameters){
        StringWriter stringWriter = new StringWriter();
        try {
            statementTemplate.getTemplate().process(parameters, stringWriter);
        } catch (Exception e) {
            logger.error("处理DAO查询参数模板时发生错误：{}",e);
            throw new SystemException(e);
        }
        return stringWriter.toString();
    }

    /**
     * 取得sessionFactory
     */
    private SessionFactory getSessionFactory() {
        if (sf == null) {
            Session session = this.em.unwrap(Session.class);
            sf = session.getSessionFactory();
        }
        return sf;
    }

    /**
     * 取得当前Session
     */
    private Session getSession() {
        return getSessionFactory().openSession();
    }

}