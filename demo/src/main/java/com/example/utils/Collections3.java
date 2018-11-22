package com.example.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Collections3
 * Created by leon_zy on 2018/11/22
 */
public class Collections3 {
    public Collections3() {
    }

    public static Map extractToMap(Collection collection, String keyPropertyName, String valuePropertyName) {
        HashMap map = new HashMap(collection.size());

        try {
            Iterator var4 = collection.iterator();

            while(var4.hasNext()) {
                Object obj = var4.next();
                map.put(PropertyUtils.getProperty(obj, keyPropertyName), PropertyUtils.getProperty(obj, valuePropertyName));
            }

            return map;
        } catch (Exception var6) {
            throw Reflections.convertReflectionExceptionToUnchecked(var6);
        }
    }

    public static List extractToList(Collection collection, String propertyName) {
        ArrayList list = new ArrayList(collection.size());

        try {
            Iterator var3 = collection.iterator();

            while(var3.hasNext()) {
                Object obj = var3.next();
                list.add(PropertyUtils.getProperty(obj, propertyName));
            }

            return list;
        } catch (Exception var5) {
            throw Reflections.convertReflectionExceptionToUnchecked(var5);
        }
    }

    public static String extractToString(Collection collection, String propertyName, String separator) {
        List list = extractToList(collection, propertyName);
        return StringUtils.join(list, separator);
    }

    public static String convertToString(Collection collection, String separator) {
        return StringUtils.join(collection, separator);
    }

    public static String convertToString(Collection collection, String prefix, String postfix) {
        StringBuilder builder = new StringBuilder();
        Iterator var4 = collection.iterator();

        while(var4.hasNext()) {
            Object o = var4.next();
            builder.append(prefix).append(o).append(postfix);
        }

        return builder.toString();
    }

    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static <T> T getFirst(Collection<T> collection) {
        return isEmpty(collection) ? null : collection.iterator().next();
    }

    public static <T> T getLast(Collection<T> collection) {
        if (isEmpty(collection)) {
            return null;
        } else if (collection instanceof List) {
            List<T> list = (List)collection;
            return list.get(list.size() - 1);
        } else {
            Iterator iterator = collection.iterator();

            Object current;
            do {
                current = iterator.next();
            } while(iterator.hasNext());

            return current;
        }
    }

    public static <T> List<T> union(Collection<T> a, Collection<T> b) {
        List<T> result = new ArrayList(a);
        result.addAll(b);
        return result;
    }

    public static <T> List<T> subtract(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList(a);
        Iterator var3 = b.iterator();

        while(var3.hasNext()) {
            T element = var3.next();
            list.remove(element);
        }

        return list;
    }

    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList();
        Iterator var3 = a.iterator();

        while(var3.hasNext()) {
            T element = var3.next();
            if (b.contains(element)) {
                list.add(element);
            }
        }

        return list;
    }

    public static <T> List<T> copyList(Collection collection, Class<T> clazz) throws IllegalAccessException {
        List destinationList = new ArrayList();
        if (!isEmpty(collection)) {
            DozerBeanMapper doze = new DozerBeanMapper();
            Iterator i$ = collection.iterator();

            while(i$.hasNext()) {
                Object sourceObject = i$.next();
                Object destinationObject = doze.map(sourceObject, clazz);
                if (ObjectUtils.objAllFiledNullFlag(destinationObject).booleanValue()) {
                    destinationList.add(destinationObject);
                }
            }
        }

        return destinationList;
    }
}