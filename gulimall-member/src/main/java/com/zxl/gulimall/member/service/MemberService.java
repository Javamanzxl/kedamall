package com.zxl.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxl.common.to.MemberRegisterTo;
import com.zxl.common.utils.PageUtils;
import com.zxl.gulimall.member.entity.MemberEntity;
import com.zxl.gulimall.member.vo.MemberLoginVo;
import com.zxl.gulimall.member.vo.WeiboUserVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zxl
 * @email 1050295916@qq.com
 * @date 2024-10-22 10:43:29
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterTo registerTo);


    MemberEntity login(MemberLoginVo member);

    MemberEntity ouathLogin(WeiboUserVo weiboUser);
}

