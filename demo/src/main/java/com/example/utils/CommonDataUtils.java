package com.example.utils;

import com.insaic.base.exception.BusinessException;
import com.insaic.base.utils.Collections3;
import com.insaic.base.utils.DateUtil;
import com.insaic.base.utils.Reflections;
import com.insaic.base.utils.StringUtil;
import com.insaic.common.constants.CodeConstants;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.xml.bind.annotation.XmlElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommonDataUtils 数据库查询赋值工具类
 * Created by leon_yan on 2018/5/30
 */
public final class CommonDataUtils {

    //下划线
    private static final String UNDERLINE_STR = "_";
    private static final String EMPTY_STR = "";
    //中文字符、英文字母和中英特殊符号
    private static final String regEx = "[^0-9]";
    private static final String regEx_prefix_site = "{0}[0-9]{1}";
    private static final String brace_left = "{";
    private static final String brace_right = "}";
    private static final String GET_STR = "get";
    private static final Map<Class, Map<String, Field>> classCatchMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CommonDataUtils.class);

    /**
     * 查询数据库的值转换到对应类的对应属性名上
     * @param dataMaps 查询结果
     * @param clazz 赋值的类
     * @param <T> 泛型
     * @return list
     */
    public static <T> List<T> getDataResultList(List<Map<String, Object>> dataMaps, Class<T> clazz) {
        List<T> results = new ArrayList<>();
        if(null != clazz && Collections3.isNotEmpty(dataMaps)){
            for(Map<String, Object> dataMap : dataMaps){
                if(!dataMap.isEmpty()) {
                    results.add(getDataResult(dataMap, clazz));
                }
            }
        }
        return results;
    }

    /**
     * 转换属性值到对应类的对应属性名上
     * @param dataMap 属性值
     * @param clazz 赋值的类
     * @param <T> 泛型
     * @return T
     */
    public static <T> T getDataResult(Map<String, Object> dataMap, Class<T> clazz) {
        T obj = null;
        try{
            if(null != clazz && null != dataMap && !dataMap.isEmpty()){
                obj = clazz.newInstance();
                Map<String, Field> fieldCatchMap = null == classCatchMap.get(clazz) ? new HashMap<>() : classCatchMap.get(clazz);
                classCatchMap.putIfAbsent(clazz, fieldCatchMap);
                for(String key : dataMap.keySet()){
                    Field f = fieldCatchMap.get(key);
                    //取缓存Field
                    if(null != f){
                        f.setAccessible(true);
                        f.set(obj, replaceObjType(f.getGenericType().toString(), dataMap.get(key)));
                    }else{
                        //若没有缓存则取class中的属性，并放到缓存中
                        for (Field field : Reflections.getAllFields(clazz)) {
                            if(key.equals(getFileAnnotationNameByClass(field, Column.class))
                                    || key.replace(UNDERLINE_STR, EMPTY_STR).toLowerCase().equals(field.getName().replace(UNDERLINE_STR, EMPTY_STR).toLowerCase())){
                                field.setAccessible(true);
                                field.set(obj, replaceObjType(field.getGenericType().toString(), dataMap.get(key)));
                                fieldCatchMap.put(key, field);
                                break;
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            logger.error("getDataResult出错：" + e.getMessage(), e);
            throw new BusinessException("查询异常，请联系管理员。");
        }
        return obj;
    }

    /**
     * 获取属性上指定类型的注解name
     * @param f 属性
     * @param clazz 属性上的注解类
     * @return str
     */
    public static String getFileAnnotationNameByClass(Field f, Class clazz){
        String name = "";
        if(null != clazz){
            //获取属性上的指定类型的注解
            Annotation annotation = f.getAnnotation(clazz);
            if (null != annotation) {
                //强制转化为相应的注解，获取属性上的指定类型的注解的name
                if(clazz.equals(XmlElement.class)){
                    XmlElement obj = (XmlElement)annotation;
                    name = obj.name();
                }else if(clazz.equals(Column.class)){
                    Column obj = (Column)annotation;
                    name = obj.name();
                }
            }
        }
        return name;
    }

    /**
     * java驼峰字段转成数据库下划线字段
     * @param name 字段名称
     * @return str
     */
    public static String underscoreName(String name) {
        StringBuilder result = new StringBuilder();
        if(StringUtil.isNotBlank(name)){
            //将第一个字符处理成大写
            result.append(name.substring(0,1).toUpperCase());
            //循环处理其余字符
            for(int i =1;i < name.length(); i++) {
                String s = name.substring(i, i +1);
                //在大写字母前添加下划线
                if(s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append(UNDERLINE_STR);
                }
                //其他字符直接转成大写
                result.append(s.toUpperCase());
            }
        }
        return  result.toString();
    }

    /**
     * 数据库字段转成java驼峰式
     * @param name 字段名称
     * @return str
     */
    public static String convertToHump(String name) {
        StringBuffer buffer = new StringBuffer();
        if(StringUtil.isNotBlank(name)){
            if(name.contains(UNDERLINE_STR)){
                String[] words = name.toLowerCase().split(UNDERLINE_STR);
                for(int i = 0; i < words.length; i++){
                    String word = words[i];
                    String firstLetter = word.substring(0, 1);
                    String others = word.substring(1);
                    String upperLetter = i != 0 ? firstLetter.toUpperCase() : firstLetter;
                    buffer.append(upperLetter).append(others);
                }
            }else{
                buffer.append(name.toLowerCase());
            }
        }
        return buffer.toString();
    }

    /**
     * 判断对象是否为空
     * @param obj 对象
     * @return boolean
     */
    public static Boolean isBlankOrNull(Object obj){
        return null == obj || "".equals(obj);
    }

    /**
     * 获取属性类型
     * @param type 属性类型
     * @param objVal 属性值
     * @return Object
     */
    public static Object replaceObjType(String type, Object objVal){
        Object val = null;
        switch (type) {
            case "class java.lang.String":
                val = isBlankOrNull(objVal) ? null : String.valueOf(objVal);
                break;
            case "class java.lang.Long":
                val = isBlankOrNull(objVal) ? null : Long.valueOf(String.valueOf(objVal));
                break;
            case "class java.lang.Integer":
                val = isBlankOrNull(objVal) ? null : Integer.valueOf(String.valueOf(objVal));
                break;
            case "class java.lang.Short":
                val = isBlankOrNull(objVal) ? null : Short.valueOf(String.valueOf(objVal));
                break;
            case "class java.lang.Double":
                val = isBlankOrNull(objVal) ? null : Double.valueOf(String.valueOf(objVal));
                break;
            case "class java.lang.Boolean":
                val = isBlankOrNull(objVal) ? null : Boolean.valueOf(String.valueOf(objVal));
                break;
            case "class java.util.Date":
                val = isBlankOrNull(objVal) ? null : DateUtil.stringToDate(String.valueOf(objVal),
                        String.valueOf(objVal).length() > 10 ?  "yyyy-MM-dd HH:mm:ss" : "yyyy-MM-dd");
                break;
            case "class java.math.BigDecimal":
                val = isBlankOrNull(objVal) ? null : new BigDecimal(String.valueOf(objVal));
                break;
        }
        return val;
    }

    public static String replaceProductBrand(String brand, String manufacturer){
        String manufacturerName = CodeConstants.SVW.equals(manufacturer) ? CodeConstants.SVW_NAME
                : CodeConstants.SGM.equals(manufacturer) ? CodeConstants.SGM_NAME
                : CodeConstants.SAIC.equals(manufacturer) ? CodeConstants.EMPTY_STR : CodeConstants.EMPTY_STR;
        return CodeConstants.VW_NAME.equals(brand) ? CodeConstants.SVW_NAME : manufacturerName.concat(brand);
    }

    /**
     * 组装属性为字符串
     * @param files 属性值
     * @return str
     */
    public static String buildFileStr(Object... files){
        StringBuilder str = new StringBuilder();
        if(null != files && files.length > 0){
            for(Object file : files){
                str.append(StringUtil.toString(file));
            }
        }
        return str.toString();
    }

    /**
     * 根据key判断集合中是否有重复数据，排除重复数据
     * @param eo 数据库数据
     * @param map 已处理的map集合
     * @param list 需要保存的
     * @param fileStr key
     * @param <T> 泛型
     */
    public static <T> void addObjectEO(T eo, Map<String, String> map, List<T> list, String fileStr){
        if(StringUtil.isNotBlank(fileStr) && null == map.get(fileStr)){
            list.add(eo);
            map.put(fileStr, fileStr);
        }
    }

    /**
     * 字符串首字母转大写
     * @param str 字符串
     * @return str
     */
    public static String firstLetterUpper(String str) {
        String val = null;
        if(StringUtil.isNotBlank(str)){
            char[] ch = str.toCharArray();
            if (ch[0] >= 'a' && ch[0] <= 'z') {
                ch[0] = (char) (ch[0] - 32);
            }
            val = String.valueOf(ch);
        }
        return val;
    }

    /**
     * 判断错误信息是否存在返回成功或失败
     * @param errorMsg 错误信息
     * @return str
     */
    public static String returnSuccessStr(String errorMsg){
        return StringUtil.isBlank(errorMsg) ? CodeConstants.TRUE_STR : CodeConstants.FALSE_STR;
    }

    /**
     * 半角转全角
     * @param str String
     * @return 全角字符串
     */
    public static String toSBC(String str) {
        char c[] = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 全角转半角
     * @param str String
     * @return 半角字符串
     */
    public static String toDBC(String str) {
        char c[] = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 字符串全角转半角并把中英文和特殊字符转为英文逗号，方便分隔
     * @param str 字符串
     * @return str
     */
    public static String replaceSpecStr(Object str) {
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(toDBC(StringUtil.toString(str)));
        return m.replaceAll(CodeConstants.COMMA_EN).trim();
    }

    /**
     * 属性值转为半角并获取属性指定长度的值
     * @param val 属性值
     * @param len 属性值的长度
     * @return str
     */
    public static String getFileLengthStr(Object val, int len){
        String[] values = replaceSpecStr(val).split(CodeConstants.COMMA_EN);
        return (String) CollectionUtils.find(Arrays.asList(values), o -> StringUtil.toString(o).length() == len);
    }

    /**
     * 属性值转为半角并获取属性指定长度和前缀的值
     * @param val 属性值
     * @param prefix 前缀
     * @param len 属性值的长度
     * @return str
     */
    public static String getFileLengthStrPrefix(Object val, String prefix, int len){
        String str = null;
        String regExStr = MessageFormat.format(regEx_prefix_site, StringUtil.toString(prefix), brace_left + len + brace_right);
        Pattern p = Pattern.compile(regExStr);
        Matcher m = p.matcher(toDBC(StringUtil.toString(val)));
        if(m.find()){
            str = m.group();
        }
        return str;
    }

    /**
     * 字符串全角转半角并把中英文和特殊字符转为英文逗号，方便分隔
     * @param str 字符串
     * @return str
     */
    public static String replaceSpecStrPrefix(Object str, String prefix, int len) {
        String regExStr = MessageFormat.format(regEx_prefix_site, StringUtil.toString(prefix), brace_left + len + brace_right);
        Pattern p = Pattern.compile(regExStr);
        Matcher m = p.matcher(toDBC(StringUtil.toString(str)));
        return m.replaceAll(CodeConstants.COMMA_EN).trim();
    }

    /**
     * 根据属性名和值查询集合中的对象
     * @param list  对象集合
     * @param fileName 属性名称
     * @param val 属性值
     * @return obj
     */
    public static <T> T findObjByPropertyNameValue(List<T> list, String fileName, Object val){
        T result = null;
        String upperName = firstLetterUpper(fileName);
        if(Collections3.isNotEmpty(list) && StringUtil.isNotBlank(upperName)){
            Method method;
            Object value;
            try {
                for (T item : list) {
                    method = item.getClass().getMethod(GET_STR + upperName);
                    value = method.invoke(item);
                    if (StringUtil.toString(value).equals(StringUtil.toString(val))) {
                        result = item;
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("findObjByPropertyNameValue：" + e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 根据属性名和值查询集合中的对象集合
     * @param list  对象集合
     * @param fileName 属性名称
     * @param val 属性值
     * @return list
     */
    public static <T> List<T> findListByPropertyNameValue(List<T> list, String fileName, Object val){
        List<T> result = new ArrayList<>();
        String upperName = firstLetterUpper(fileName);
        if(Collections3.isNotEmpty(list) && StringUtil.isNotBlank(upperName)){
            Method method;
            Object value;
            try {
                for (T item : list) {
                    method = item.getClass().getMethod(GET_STR + upperName);
                    value = method.invoke(item);
                    if (StringUtil.toString(value).equals(StringUtil.toString(val))) {
                        result.add(item);
                    }
                }
            } catch (Exception e) {
                logger.error("findListByPropertyNameValue：" + e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 获取开始时间当天最早的时间
     * @param startDate 开始时间
     * @return 日期
     */
    public static Date getStartDateDayLastTime(Date startDate){
        Date date = startDate;
        if(null!= startDate){
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            date = cal.getTime();
        }
        return date;
    }

    /**
     * 获取结束时间当天最后的时间
     * @param endDate 结束时间
     * @return 日期
     */
    public static Date getEndDateDayLastTime(Date endDate){
        Date date = endDate;
        if(null != endDate){
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            date = cal.getTime();
        }
        return date;
    }
}