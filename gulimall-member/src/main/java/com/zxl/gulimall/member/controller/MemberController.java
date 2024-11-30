package com.zxl.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.zxl.common.exception.ErrorCodeEnum;
import com.zxl.common.to.MemberRegisterTo;
import com.zxl.common.to.MemberTo;
import com.zxl.gulimall.member.exception.PhoneExistException;
import com.zxl.gulimall.member.exception.UserNameExistException;
import com.zxl.gulimall.member.vo.MemberLoginVo;
import com.zxl.gulimall.member.vo.WeiboUserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import com.zxl.gulimall.member.entity.MemberEntity;
import com.zxl.gulimall.member.service.MemberService;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.R;

import javax.annotation.Resource;


/**
 * 会员
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:43:29
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Resource
    private MemberService memberService;

    /**
     * 社交账号登录注册
     *
     * @param weiboUser
     * @return
     */
    @PostMapping("/oauth2/login")
    public MemberTo oauthLogin(@RequestBody WeiboUserVo weiboUser) {
        MemberEntity memberEntity = memberService.ouathLogin(weiboUser);
        MemberTo memberTo = new MemberTo();
        BeanUtils.copyProperties(memberEntity,memberTo);
        return memberTo;
    }


    /**
     * 用户端前端注册功能，远程调用
     *
     * @param registerTo
     * @return
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterTo registerTo) {
        try {
            memberService.register(registerTo);
        } catch (PhoneExistException e) {
            return R.error(ErrorCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), ErrorCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UserNameExistException e) {
            return R.error(ErrorCodeEnum.USER_EXIST_EXCEPTION.getCode(), ErrorCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo member) {
        MemberEntity memberEntity = memberService.login(member);
        if (memberEntity == null) {
            return R.error(ErrorCodeEnum.ACCOUNT_PASSWORD_EXCEPTION.getCode(), ErrorCodeEnum.ACCOUNT_PASSWORD_EXCEPTION.getMessage());
        }
        MemberTo memberTo = new MemberTo();
        BeanUtils.copyProperties(memberEntity,memberTo);
        return R.ok().setData(memberTo);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }


    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
