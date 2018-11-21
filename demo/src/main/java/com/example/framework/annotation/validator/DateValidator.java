package com.example.framework.annotation.validator;

import com.example.framework.annotation.DateValid;
import com.insaic.base.utils.StringUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * StringValidator
 * Created by leon_zy on 2018/10/8
 */
public class DateValidator implements ConstraintValidator<DateValid,String> {

    private DateValid valid;
    private String formatStr;

    @Override public void initialize(DateValid init) {
        valid = init;
        formatStr = init.format();
    }

    @Override public boolean isValid(String val, ConstraintValidatorContext constraintValidatorContext) {
        Boolean validFlag = true;
        try{
            if(valid.isNull()){
                if(StringUtil.isNotBlank(val)){
                    validFlag = validDateFormat(val, formatStr);
                }
            }else{
                validFlag = StringUtil.isNotBlank(val) && validDateFormat(val, formatStr);
            }
        } catch (Exception e){
            validFlag = false;
        }
        return validFlag;
    }

    private boolean validDateFormat(String str, String formatStr) {
        boolean successFlag = true;
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            // 设置lenient为false.
            // 否则SimpleDateFormat会比较宽松地验证日期，如02/29会被接受并转换成03/01
            format.setLenient(false);
            Date date = format.parse(str);
            successFlag = str.equals(format.format(date));
        } catch (Exception e) {
            successFlag = false;
        }
        return successFlag;
    }
}