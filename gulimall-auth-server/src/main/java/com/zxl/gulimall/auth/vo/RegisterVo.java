package com.zxl.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author ：zxl
 * @Description: 注册信息VO
 * @ClassName: RegisterVo
 * @date ：2024/11/26 20:21
 */
@Data
public class RegisterVo {
    @NotEmpty(message = "用户名必须提交")
    @Length(min = 4,max = 20,message = "用户名必须是4-20位")
    private String userName;
    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 20,message = "密码必须是6-20位")
    private String password;
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号格式不正确")
    private String phone;
    @NotEmpty(message = "验证码必须填写")
    private String code;
}
