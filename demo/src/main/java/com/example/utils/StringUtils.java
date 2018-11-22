package com.example.utils;

import java.beans.PropertyDescriptor;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * StringUtils
 * Created by leon_zy on 2018/11/22
 */
public class StringUtils {
    public static final String SYMBOL_DASH = "_";
    public static final String SYMBOL_COLON = ":";
    public static final String SYMBOL_COMMA = ",";
    public static final String SYMBOL_PERCENT = "%";
    public static final String SYMBOL_DOT = ".";
    public static final String SYMBOL_VERTICAL = "|";

    public StringUtil() {
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isBlank(String value) {
        return null == value || value.length() == 0 || "".equals(value) || "".equals(value.trim());
    }

    public static String getRandomString(int length) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; ++i) {
            sb.append(Character.toString((char)((int)(Math.random() * 95.0D + 32.0D))));
        }

        return sb.toString();
    }

    public static String[] subString(String src, int length, int max) {
        String[] result = new String[max];

        try {
            char[] charArray = src.toCharArray();
            StringBuilder sb = new StringBuilder();
            int count = 0;
            int arrayCount = 0;

            for(int i = 0; i < charArray.length; ++i) {
                char ch = charArray[i];
                int chLength = Character.toString(ch).getBytes("UTF-8").length;
                count += chLength;
                if (count > length) {
                    result[arrayCount] = sb.toString();
                    ++arrayCount;
                    if (arrayCount > max - 1) {
                        break;
                    }

                    sb = new StringBuilder();
                    count = chLength;
                }

                sb.append(ch);
            }

            if (arrayCount < max) {
                result[arrayCount] = sb.toString();
            }

            return result;
        } catch (UnsupportedEncodingException var11) {
            throw new RuntimeException("UnsupportedEncoding!", var11);
        }
    }

    public static String after(String src, String dest) {
        int pos = src.indexOf(dest);
        if (pos == -1) {
            return src;
        } else {
            String last = src.substring(pos + 1);
            return last;
        }
    }

    public static String before(String src, String dest) {
        int pos = src.indexOf(dest);
        if (pos == -1) {
            return src;
        } else {
            String first = src.substring(0, pos);
            return first;
        }
    }

    public static String upperFirst(String value) {
        String first = value.substring(0, 1);
        String last = value.substring(1);
        return first.toUpperCase() + last;
    }

    public static String lowerFirst(String value) {
        String first = value.substring(0, 1);
        String last = value.substring(1);
        return first.toLowerCase() + last;
    }

    public static Map<String, String> toMap(String value) {
        return toMap(value, "|");
    }

    public static Map<String, String> toMap(String value, String splitCharacter) {
        Map<String, String> result = new HashMap();
        if (isNotBlank(value)) {
            String[] splitArray = value.split("\\" + splitCharacter);
            String[] var4 = splitArray;
            int var5 = splitArray.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String s = var4[var6];
                String[] split = s.split("=");
                result.put(split[0], String.valueOf(split[1]));
            }
        }

        return result;
    }

    public static List<String> split(String text, int length) {
        ArrayList result = new ArrayList();

        try {
            if (text != null) {
                char[] charArray = text.toCharArray();
                StringBuilder sb = new StringBuilder();
                int count = 0;

                for(int i = 0; i < charArray.length; ++i) {
                    char ch = charArray[i];
                    int chLength = Character.toString(ch).getBytes("UTF-8").length;
                    if (count + chLength > length) {
                        result.add(sb.toString());
                        sb = new StringBuilder();
                        count = 0;
                    }

                    count += chLength;
                    sb.append(ch);
                }

                result.add(sb.toString());
            }

            return result;
        } catch (UnsupportedEncodingException var9) {
            throw new RuntimeException("UnsupportedEncoding!", var9);
        }
    }

    public static <T> String joinEntityPropertyString(String splitString, String propertyName, Collection<T> entities) {
        StringBuilder result = new StringBuilder();
        if (entities != null && entities.size() > 0) {
            Iterator var4 = entities.iterator();

            while(var4.hasNext()) {
                T t = var4.next();
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(t.getClass(), propertyName);

                try {
                    Object value = pd.getReadMethod().invoke(t);
                    if (value != null && isNotBlank(value.toString())) {
                        if (result.length() != 0) {
                            result.append(",");
                        }

                        result.append(value);
                    }
                } catch (IllegalArgumentException var9) {
                    throw new RuntimeException("IllegalArgumentException!", var9);
                } catch (IllegalAccessException var10) {
                    throw new RuntimeException("IllegalAccessException!", var10);
                } catch (InvocationTargetException var11) {
                    throw new RuntimeException("InvocationTargetException!", var11);
                }
            }
        }

        return result.toString();
    }

    public static String joinString(String... strs) {
        StringBuilder result = new StringBuilder();
        if (strs != null && strs.length > 0) {
            String[] var2 = strs;
            int var3 = strs.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String str = var2[var4];
                if (isNotBlank(str)) {
                    result.append(str);
                }
            }
        }

        return result.toString();
    }

    public static String joinObject(Object... objs) {
        StringBuilder result = new StringBuilder();
        if (objs != null && objs.length > 0) {
            Object[] var2 = objs;
            int var3 = objs.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Object obj = var2[var4];
                if (obj != null && isNotBlank(obj.toString())) {
                    result.append(obj.toString());
                }
            }
        }

        return result.toString();
    }

    public static String joinByDash(String... strs) {
        return joinBySymbol(strs, "_");
    }

    public static String joinByComma(String... strs) {
        return joinBySymbol(strs, ",");
    }

    public static String joinListByComma(Collection<String> list) {
        return joinListBySymbol(list, ",");
    }

    public static String joinByColon(String... strs) {
        return joinBySymbol(strs, ":");
    }

    public static String joinByVertical(String... strs) {
        return joinBySymbol(strs, "|");
    }

    public static String joinByVertical(Object... objs) {
        return joinObject(objs, "|");
    }

    public static String joinByDot(String... strs) {
        return joinBySymbol(strs, ".");
    }

    public static String joinBySymbol(String[] strs, String regex) {
        StringBuilder result = new StringBuilder();
        if (strs != null && strs.length > 0) {
            int count = 0;
            String[] var4 = strs;
            int var5 = strs.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String str = var4[var6];
                if (isNotBlank(str)) {
                    result.append(count == 0 ? "" : regex).append(str);
                    ++count;
                }
            }
        }

        return result.toString();
    }

    public static String joinObject(Object[] objs, String regex) {
        StringBuilder result = new StringBuilder();
        if (objs != null && objs.length > 0) {
            int count = 0;
            Object[] var4 = objs;
            int var5 = objs.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Object obj = var4[var6];
                if (obj != null && isNotBlank(obj.toString())) {
                    result.append(count == 0 ? "" : regex).append(obj.toString());
                    ++count;
                }
            }
        }

        return result.toString();
    }

    public static String joinListBySymbol(Collection<String> list, String regex) {
        StringBuilder result = new StringBuilder();
        if (list != null && list.size() > 0) {
            int count = 0;
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
                String str = (String)var4.next();
                if (isNotBlank(str)) {
                    result.append(count == 0 ? "" : regex).append(str);
                    ++count;
                }
            }
        }

        return result.toString();
    }

    public static String[] splitByDash(String splitStr) {
        return split(splitStr, "_");
    }

    public static String[] splitByComma(String splitStr) {
        return split(splitStr, ",");
    }

    public static String[] splitByColon(String splitStr) {
        return split(splitStr, ":");
    }

    public static String[] split(String splitStr, String regex) {
        return isBlank(splitStr) ? null : splitStr.split(regex);
    }

    public static String[] split(String splitStr) {
        return split(splitStr, ",");
    }

    public static String[] splitByVerticalLine(String splitStr) {
        return split(splitStr, "\\|");
    }

    public static List<String> splitToList(String splitStr) {
        Object list;
        if (isNotBlank(splitStr)) {
            list = Arrays.asList(split(splitStr));
        } else {
            list = new ArrayList();
        }

        return (List)list;
    }

    private static SimpleDateFormat getCurrentDateFormat(String p) {
        SimpleDateFormat sdf = new SimpleDateFormat(p);
        TimeZone timeZone = TimeZone.getDefault();
        sdf.setTimeZone(timeZone);
        return sdf;
    }

    public static String toString(Object o, String pattern) {
        if (o == null) {
            return null;
        } else if (o.getClass().isArray()) {
            if (o instanceof byte[]) {
                return new String((byte[])((byte[])o));
            } else {
                return o instanceof char[] ? new String((char[])((char[])o)) : "";
            }
        } else {
            return asString(o, pattern);
        }
    }

    public static String asString(Object o, String pattern) {
        if (o == null) {
            return "";
        } else if (o.getClass().isArray()) {
            if (o instanceof byte[]) {
                return new String((byte[])((byte[])o));
            } else {
                return o instanceof char[] ? new String((char[])((char[])o)) : "";
            }
        } else {
            SimpleDateFormat sdf;
            if (o instanceof Calendar) {
                sdf = getCurrentDateFormat(pattern);
                return sdf.format(o);
            } else if (o instanceof Timestamp) {
                sdf = getCurrentDateFormat(pattern);
                return sdf.format(o);
            } else if (o instanceof Time) {
                sdf = getCurrentDateFormat(pattern);
                return sdf.format(o);
            } else if (!(o instanceof Date) && !(o instanceof java.sql.Date)) {
                return !(o instanceof Double) && !(o instanceof BigDecimal) && !(o instanceof Float) ? o.toString() : String.valueOf(o);
            } else {
                sdf = getCurrentDateFormat(pattern);
                return sdf.format(o);
            }
        }
    }

    public static String toString(Object o) {
        String symbol = "####################################.#######################################";
        if (o == null) {
            return "";
        } else if (o instanceof Date) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            return df.format(o);
        } else if (!(o instanceof Double) && !(o instanceof BigDecimal) && !(o instanceof Float)) {
            return o.toString();
        } else {
            DecimalFormat df = new DecimalFormat(symbol);
            return df.format(o);
        }
    }

    public static Object toObject(String o, String type) {
        if (isBlank(o)) {
            return null;
        } else if ("String".equalsIgnoreCase(type)) {
            return o;
        } else if ("Integer".equalsIgnoreCase(type)) {
            return Integer.valueOf(o);
        } else if ("BigInteger".equalsIgnoreCase(type)) {
            return new BigInteger(o);
        } else if ("Long".equalsIgnoreCase(type)) {
            return Long.valueOf(o);
        } else if ("BigDecimal".equalsIgnoreCase(type)) {
            return new BigDecimal(o);
        } else if ("Double".equalsIgnoreCase(type)) {
            return new Double(o);
        } else if ("Float".equalsIgnoreCase(type)) {
            return new Double(o);
        } else if ("Date".equalsIgnoreCase(type)) {
            return getDateFromString(o);
        } else {
            return "Boolean".equalsIgnoreCase(type) ? Boolean.valueOf(o) : o;
        }
    }

    private static Date getDateFromString(String o) {
        if (isBlank(o)) {
            return new Date(System.currentTimeMillis());
        } else {
            SimpleDateFormat df = null;
            if (o.length() == 10) {
                df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            } else {
                if (o.length() != 19 && o.length() != 21) {
                    throw new RuntimeException("不支持的日期时间格式");
                }

                df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            }

            Date d = null;

            try {
                d = new Date(df.parse(o).getTime());
                return d;
            } catch (ParseException var4) {
                throw new RuntimeException(var4);
            }
        }
    }

    public static String formatString(String s) {
        return isBlank(s) ? "" : s;
    }

    public static String reverse(String value) {
        return isBlank(value) ? value : (new StringBuilder(value)).reverse().toString();
    }

    public static String replaceBlank(String str, String replaceStr) {
        return isBlank(str) ? replaceStr : str;
    }
}