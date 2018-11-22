package com.example.utils;

import com.insaic.base.PageResult;
import com.insaic.base.utils.Collections3;
import com.insaic.base.utils.SpringBeanLocator;
import com.insaic.base.web.mo.WebPageResult;
import com.insaic.common.code.model.PageParamMO;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * QueryPageResultUtils 分页查询utils
 * Created by leon_yan on 2018/4/19
 */
public final class QueryPageResultUtils {

    private static EntityManager em;

    static{
        em = SpringBeanLocator.getBean(EntityManager.class);
    }

    /**
     * 管理分页查询参数
     * @param paramMO 分页参数
     * @return PageResult
     */
    public static PageRequest handlerPageParam(PageParamMO paramMO) {
        Integer pageNum = (null == paramMO.getPageNumber() ? 0 : paramMO.getPageNumber());
        Integer pageSize = (null == paramMO.getPageSize() || 0 == paramMO.getPageSize() ? 10 : paramMO.getPageSize());
        return new PageRequest(pageNum, pageSize);
    }

    /**
     * 管理分页数据，并转成对象的class
     * @param pages 分页
     * @return PageResult
     */
    public static <T> WebPageResult<T> handlerPageResult(Page pages, Class<T> clazz) throws IllegalAccessException {
        WebPageResult<T> result = new WebPageResult<>();
        PageResult<T> pageResult = new PageResult<>();
        if (null != pages) {
            if(Collections3.isNotEmpty(pages.getContent())){
                pageResult.setContent(Collections3.copyList(pages.getContent(), clazz));
            }
            pageResult.setTotal(pages.getTotalElements());
            pageResult.setPageSize(pages.getSize());
            pageResult.setPageNumber(pages.getNumber());
            pageResult.setTotalPage(pages.getTotalPages());
        }
        result.setPageResult(pageResult);
        return result;
    }

    /**
     * 管理分页数据，并转成对象的class，支持跨表字段
     * @return PageResult
     */
    public static <T> WebPageResult<T> getWebPageResult(String sql, Map<String, Object> conditionMap, PageParamMO paramMO, Class<T> clazz) {
        return getWebPageResult(getPageResults(sql, conditionMap, QueryPageResultUtils.handlerPageParam(paramMO), clazz));
    }

    /**
     * 管理分页数据，并转成对象的class，支持跨表字段
     * @param page 分页
     * @return PageResult
     */
    public static <T> WebPageResult<T> getWebPageResult(Page<T> page) {
        WebPageResult<T> result = new WebPageResult<>();
        PageResult<T> pageResult = new PageResult<>();
        if (null != page) {
            pageResult.setContent(page.getContent());
            pageResult.setTotal(page.getTotalElements());
            pageResult.setPageSize(page.getSize());
            pageResult.setPageNumber(page.getNumber());
            pageResult.setTotalPage(page.getTotalPages());
        }
        result.setPageResult(pageResult);
        return result;
    }

    /**
     * 可跨表查询分页数据并赋值到对应的class
     * @param sql 查询语句
     * @param conditionMap 查询参数
     * @param pageRequest 分页参数
     * @param clazz 类
     * @param <T> 泛型
     * @return 分页数据
     */
    public static <T> Page<T> getPageResults(String sql, Map<String, Object> conditionMap, Pageable pageRequest, Class<T> clazz) {
        return findPageResultObjectsBySql(sql, conditionMap, pageRequest, clazz);
    }

    /**
     * 可跨表查询分页数据并赋值到对应的class
     * @param sql 查询语句
     * @param conditionMap 查询参数
     * @param pageRequest 分页参数
     * @param clazz 类
     * @param <T> 泛型
     * @return 分页数据
     */
    private static <T> Page<T> findPageResultObjectsBySql(String sql, Map<String, Object> conditionMap, Pageable pageRequest, Class<T> clazz) {
        if (pageRequest.getOffset() >= 0 && pageRequest.getPageSize() > 0 && sql != null) {
            long total = findCountBySql(sql, conditionMap);
            Query query = em.createNativeQuery(sql);
            query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            setParameters(query, conditionMap);
            query.setFirstResult(pageRequest.getOffset());
            query.setMaxResults(pageRequest.getPageSize());
            List results = CommonDataUtils.getDataResultList(query.getResultList(), clazz);
            return new PageImpl(results, pageRequest, total);
        } else {
            return null;
        }
    }

    /**
     * 查询分页数据总条数
     * @param sql 查询语句
     * @param conditionMap 查询参数
     * @return 分页数据总条数
     */
    private static long findCountBySql(String sql, Map<String, Object> conditionMap) {
        if (sql != null) {
            Object o = findOneResultObjectBySql("select count(1) from ( " + sql + " )", conditionMap);
            return ((BigDecimal)o).longValue();
        } else {
            return 0L;
        }
    }

    /**
     * 查询分页数据总条数
     * @param sql 查询语句
     * @param conditionMap 查询参数
     * @return 分页数据总条数
     */
    private static Object findOneResultObjectBySql(String sql, Map<String, Object> conditionMap) {
        if (sql != null) {
            Query query = em.createNativeQuery(sql);
            setParameters(query, conditionMap);
            return query.getSingleResult();
        } else {
            return null;
        }
    }

    /**
     * 查询分页数据总条数，查询参数赋值
     * @param query 查询
     * @param map 参数值
     */
    private static void setParameters(Query query, Map<String, Object> map) {
        if (map != null && map.size() > 0) {
            for (Object o : map.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                query.setParameter((String) entry.getKey(), entry.getValue());
            }
        }
    }
}