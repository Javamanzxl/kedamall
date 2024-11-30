package com.zxl.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zxl.common.to.MemberRegisterTo;
import com.zxl.gulimall.member.dao.MemberLevelDao;
import com.zxl.gulimall.member.entity.MemberLevelEntity;
import com.zxl.gulimall.member.exception.PhoneExistException;
import com.zxl.gulimall.member.exception.UserNameExistException;
import com.zxl.gulimall.member.vo.MemberLoginVo;
import com.zxl.gulimall.member.vo.WeiboUserVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.tomcat.util.security.MD5Encoder;
import org.bouncycastle.jcajce.provider.digest.MD5;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxl.common.utils.PageUtils;
import com.zxl.common.utils.Query;

import com.zxl.gulimall.member.dao.MemberDao;
import com.zxl.gulimall.member.entity.MemberEntity;
import com.zxl.gulimall.member.service.MemberService;

import javax.annotation.Resource;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    private MemberDao memberDao;
    @Resource
    private MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册功能
     * @param registerTo
     */
    @Override
    public void register(MemberRegisterTo registerTo) {
        MemberEntity memberEntity = new MemberEntity();
        //设置默认等级
        MemberLevelEntity level = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(level.getId());
        //用户名、手机号唯一性检查
        checkUserNameUnique(registerTo.getUserName());
        memberEntity.setUsername(registerTo.getUserName());
        checkPhoneUnique(registerTo.getPhone());
        memberEntity.setMobile(registerTo.getPhone());
        memberEntity.setNickname(registerTo.getUserName());
        //密码加密处理：盐值加密,即在原来密码的基础上再加一些别的内容
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(passwordEncoder.encode(registerTo.getPassword()));
        memberDao.insert(memberEntity);
    }

    /**
     * 检查手机号是否唯一
     * @param phone
     */
    private void checkPhoneUnique(String phone) {
        Integer countPhone = memberDao.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getMobile, phone));
        if(countPhone>0){
            throw new PhoneExistException();
        }
    }

    /**
     * 检查用户名是否唯一
     * @param userName
     */
    private void checkUserNameUnique(String userName) {
        Integer countUserName = memberDao.selectCount(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getUsername, userName));
        if(countUserName>0){
            throw new UserNameExistException();
        }
    }

    /**
     * 登录功能
     * @param member
     * @return
     */
    @Override
    public MemberEntity login(MemberLoginVo member) {
        MemberEntity memberEntity = memberDao.selectOne(new LambdaQueryWrapper<MemberEntity>()
                .eq(MemberEntity::getUsername, member.getLoginAccount())
                .or().eq(MemberEntity::getMobile,member.getLoginAccount())
        );
        if(memberEntity==null){
            //没有注册，登录失败
            return null;
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String password = memberEntity.getPassword();
        if(!passwordEncoder.matches(member.getPassword(),password)){
            //密码不正确，登陆失败
            return null;
        }
        return memberEntity;
    }

    /**
     * 社交用户注册和登录
     * @param weiboUser
     * @return
     */
    @Override
    public MemberEntity ouathLogin(WeiboUserVo weiboUser) {
        //注册登录合并逻辑
        //1.判断当前社交用户是否已经登陆过该系统
        String uid = weiboUser.getUid();
        MemberEntity member = memberDao.selectOne(new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getSocialUid, uid));
        if(member!=null){
            //注册过，更新令牌和过期时间
            MemberEntity update = new MemberEntity();
            update.setAccessToken(weiboUser.getAccess_token());
            update.setExpiresIn(weiboUser.getExpires_in());
            update.setId(member.getId());
            memberDao.updateById(update);
            member.setAccessToken(weiboUser.getAccess_token());
            member.setExpiresIn(weiboUser.getExpires_in());
            return member;
        }else{
            //没有查到相关信息，要进行注册
            MemberEntity register = new MemberEntity();
            register.setSocialUid(weiboUser.getUid());
            register.setExpiresIn(weiboUser.getExpires_in());
            register.setAccessToken(weiboUser.getAccess_token());
            //查询当前社交用户的社交信息(不麻烦了设置默认值)
            register.setUsername(UUID.randomUUID().toString().substring(0,10));
            register.setNickname(UUID.randomUUID().toString().substring(0,10));
            //设置默认等级
            MemberLevelEntity level = memberLevelDao.getDefaultLevel();
            register.setLevelId(level.getId());
            memberDao.insert(register);
            return register;
        }
    }
}