package com.example.framework.annotation.validator;

import com.insaic.base.utils.StringUtil;
import com.insaic.rescue.annotation.NumberValid;
import org.apache.commons.lang3.math.NumberUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * NumberValidator
 * Created by leon_zy on 2018/10/8
 */
public class NumberValidator implements ConstraintValidator<NumberValid,Number> {

    private NumberValid valid;
    private double max = 0;

    @Override public void initialize(NumberValid init) {
        valid = init;
        max = init.max();
    }

    @Override public boolean isValid(Number val, ConstraintValidatorContext constraintValidatorContext) {
        Boolean validFlag = true;
        try{
            if(valid.isNull()){
                if(null != val){
                    validFlag = validValue(val);
                }
            }else{
                if(null != val){
                    validFlag = validValue(val);
                }else{
                    validFlag = false;
                }
            }
        } catch (Exception e){
            validFlag = false;
        }
        return validFlag;
    }

    private Boolean validValue(Number val){
        Boolean validFlag = true;
        Number num = null;
        if(0 < max){
            if(val instanceof Integer){
                num = NumberUtils.toInt(StringUtil.toString(val));
            }else if(val instanceof Long){
                num = NumberUtils.toLong(StringUtil.toString(val));
            }else if(val instanceof Float){
                num = NumberUtils.toFloat(StringUtil.toString(val));
            }else if(val instanceof Double){
                num = NumberUtils.toDouble(StringUtil.toString(val));
            }
            validFlag = num != null && num.doubleValue() < max;
        }
        return validFlag;
    }
}