package com.zxl.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ：zxl
 * @Description: 自定义校验器
 * @ClassName: ListValueConstraintValidator
 * @date ：2024/10/22 20:14
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {

    private Set<Integer> set = new HashSet<>();
    /**
     * 初始化方法
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        for(int value:values){
            set.add(value);
        }
    }


    /**
     *
     * @param integer 需要校验的值
     * @param constraintValidatorContext 环境信息
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }


}
