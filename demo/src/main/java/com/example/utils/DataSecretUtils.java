package com.example.utils;

import com.insaic.base.utils.Reflections;
import com.insaic.rescue.annotation.StringValid;
import com.insaic.rescue.constants.RescueConstants;
import freemarker.template.utility.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DataSecretUtils 数据加密解密工具类
 * Created by leon_zy on 2018/10/11
 */
public class DataSecretUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataSecretUtils.class);

    /**
     * 处理数据的加密解密
     * @param secretWay 秘密方式
     * @param secretType 秘密类型
     * @param val 值
     * @return str
     */
    public static String processSecret(String secretWay, String secretType, String val, String key){
        String str = null;
        if(StringUtil.isNotBlank(val)){
            if(RescueConstants.ENCODE.equals(secretWay)){
                str = encodeValue(secretType, val, key);
            }else if(RescueConstants.DECODE.equals(secretWay)){
                str = decodeValue(secretType, val, key);
            }
        }
        return str;
    }

    /**
     * 加密数据
     * @param secretType 加密类型
     * @param val 值
     * @return str
     */
    public static String encodeValue(String secretType, String val, String key){
        String str = null;
        if(StringUtil.isNotBlank(val)) {
            if (RescueConstants.ENCODE_MD5.equals(secretType)) {
                str = MD5Util.MD5(key + val);
            } else if (RescueConstants.ENCODE_CX.equals(secretType)) {
                str = EncryptUtils.encrypt(val, key);
            } else if (RescueConstants.ENCODE_BASE64.equals(secretType)) {

            }
        }
        return str;
    }

    /**
     * 解密数据
     * @param secretType 解密类型
     * @param val 值
     * @return str
     */
    public static String decodeValue(String secretType, String val, String key){
        String str = null;
        if(StringUtil.isNotBlank(val)) {
            if (RescueConstants.DECODE_CX.equals(secretType)) {
                str = EncryptUtils.decrypt(val, key);
            }
        }
        return str;
    }

    /**
     * 确认对象中是否有需要加密解密的属性并赋值
     * @param obj 对象
     */
    public static void setSecretValue(Object obj, Map<String, String> rescueKeysMap){
        try {
            //获取成员变量
            Field[] fields = Reflections.getAllFields(obj.getClass());
            Object val;
            for(Field field : fields){
                //设置属性可访问
                field.setAccessible(true);
                val = field.get(obj);
                if(null != val){
                    //校验对象是否为基础类型
                    if(validFieldBaseFlag(val)){
                        String str = StringUtil.toString(val);
                        //判断成员变量是否有注解
                        if(StringUtil.isNotBlank(str) && field.isAnnotationPresent(StringValid.class)){
                            //获取定义在成员变量中的注解
                            StringValid myAnnotation = field.getAnnotation(StringValid.class);
                            //获取注解设置的属性值
                            String secretWay = myAnnotation.secretWay();
                            String secretType = myAnnotation.secretType();
                            String keyType = myAnnotation.keyType();
                            if(StringUtil.isNotBlank(secretWay) && StringUtil.isNotBlank(secretType)){
                                //将加解密的值可以赋给成员变量
                                field.set(obj, processSecret(secretWay, secretType, str, rescueKeysMap.get(keyType)));
                            }
                        }
                    }else{
                        if(val instanceof List){
                            validObjectSecretValue((List) val, rescueKeysMap);
                        }else if(val instanceof Set){
                            validObjectSecretValue((Set) val, rescueKeysMap);
                        }else if(val instanceof Map){
                            Map map = (Map) val;
                            if(!map.isEmpty()) {
                                for (Object key : map.keySet()) {
                                    setSecretValue(map.get(key), rescueKeysMap);
                                }
                            }
                        }else{
                            setSecretValue(val, rescueKeysMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("setSecretValue出错：" + e.getMessage(), e);
        }
    }

    /**
     * 集合属性加密解密公共方法
     * @param collection 集合
     * @param rescueKeysMap 钥匙
     */
    private static void validObjectSecretValue(Collection collection, Map<String, String> rescueKeysMap){
        if(Collections3.isNotEmpty(collection)){
            for (Object item : collection) {
                if(!validFieldBaseFlag(item)){
                    setSecretValue(item, rescueKeysMap);
                }
            }
        }
    }

    /**
     * 校验对象是否为基础类型
     * @param obj 对象
     * @return boolean
     */
    public static Boolean validFieldBaseFlag(Object obj){
        return obj instanceof String  || obj instanceof Integer
                || obj instanceof Double || obj instanceof Float
                || obj instanceof Long || obj instanceof Boolean
                || obj instanceof Date || obj instanceof BigDecimal;
    }
}