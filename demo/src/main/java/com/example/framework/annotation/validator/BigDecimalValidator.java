package com.example.framework.annotation.validator;

import com.insaic.base.utils.StringUtil;
import com.insaic.rescue.annotation.BigDecimalValid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * BigDecimalValidator
 * Created by leon_zy on 2018/10/8
 */
public class BigDecimalValidator implements ConstraintValidator<BigDecimalValid,BigDecimal> {

    private BigDecimalValid valid;

    private int integer;
    private int decimal;

    @Override public void initialize(BigDecimalValid init) {
        valid = init;
        integer = init.integer();
        decimal = init.decimal();
    }

    @Override public boolean isValid(BigDecimal val, ConstraintValidatorContext constraintValidatorContext) {
        Boolean validFlag = true;
        try{
            if(valid.isNull()){
                if(null != val){
                    validFlag = validValue(val);
                }
            }else{
                validFlag = null != val && validValue(val);
            }
        } catch (Exception e){
            validFlag = false;
        }
        return validFlag;
    }

    private Boolean validValue(BigDecimal val){
        Boolean validFlag = true;
        int scale = val.scale();
        int integralLen = StringUtil.toString(val.setScale(0, BigDecimal.ROUND_DOWN)).length();
        if(integralLen > integer || scale > decimal){
            validFlag = false;
        }
        return validFlag;
    }
}