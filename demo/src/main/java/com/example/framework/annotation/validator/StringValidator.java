package com.example.framework.annotation.validator;

import com.insaic.base.utils.StringUtil;
import com.insaic.rescue.annotation.StringValid;
import com.insaic.rescue.enums.Encoding;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * StringValidator
 * Created by leon_zy on 2018/10/8
 */
public class StringValidator implements ConstraintValidator<StringValid,String> {

    private StringValid valid;

    @Override public void initialize(StringValid init) {
        valid = init;
    }

    @Override public boolean isValid(String val, ConstraintValidatorContext constraintValidatorContext) {
        Boolean validFlag = true;
        try{
            if(valid.length() > 0){
                if(valid.isNull()){
                    if(StringUtil.isNotBlank(val)){
                        validFlag = val.getBytes(Encoding.UTF8.getCode()).length <= valid.length();
                    }
                }else{
                    validFlag = StringUtil.isNotBlank(val) && val.getBytes(Encoding.UTF8.getCode()).length <= valid.length();
                }
            }
        } catch (Exception e){
            validFlag = false;
        }
        return validFlag;
    }
}