package com.zxl.common.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author ：zxl
 * @Description: 自定义校验注解
 * @ClassName: ListValue
 * @date ：2024/10/22 20:01
 */

/**
 * ElementType.TYPE：可以用于类、接口和枚举类型。
 * ElementType.FIELD：可以用于字段（包括枚举常量）。
 * ElementType.METHOD：可以用于方法。
 * ElementType.PARAMETER：可以用于方法的参数。
 * ElementType.CONSTRUCTOR：可以用于构造函数。
 * ElementType.LOCAL_VARIABLE：可以用于局部变量。
 * ElementType.ANNOTATION_TYPE：可以用于注解类型。
 * ElementType.PACKAGE：可以用于包。
 * ElementType.TYPE_PARAMETER：可以用于类型参数声明（Java 8新增）。
 * ElementType.TYPE_USE：可以用于使用类型的任何语句中（Java 8新增）。
 */
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    String message() default "{com.zxl.common.valid.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] values() default {};

}
