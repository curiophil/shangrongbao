package com.curiophil.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.curiophil.common.exception.Assert;
import com.curiophil.common.result.ResponseEnum;
import com.curiophil.common.util.MD5;
import com.curiophil.srb.base.util.JwtUtils;
import com.curiophil.srb.core.mapper.UserAccountMapper;
import com.curiophil.srb.core.mapper.UserLoginRecordMapper;
import com.curiophil.srb.core.pojo.entity.UserAccount;
import com.curiophil.srb.core.pojo.entity.UserInfo;
import com.curiophil.srb.core.mapper.UserInfoMapper;
import com.curiophil.srb.core.pojo.entity.UserLoginRecord;
import com.curiophil.srb.core.pojo.query.UserInfoQuery;
import com.curiophil.srb.core.pojo.vo.LoginVO;
import com.curiophil.srb.core.pojo.vo.RegisterVO;
import com.curiophil.srb.core.pojo.vo.UserIndexVO;
import com.curiophil.srb.core.pojo.vo.UserInfoVO;
import com.curiophil.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author chq
 * @since 2022-05-06
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private UserAccountMapper userAccountMapper;

    @Resource
    private UserLoginRecordMapper userLoginRecordMapper;


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(RegisterVO registerVO) {

        //判断用户是否被注册
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", registerVO.getMobile());
        Integer count = baseMapper.selectCount(queryWrapper);
        //MOBILE_EXIST_ERROR(-207, "手机号已被注册"),
        Assert.isTrue(count == 0, ResponseEnum.MOBILE_EXIST_ERROR);

        //插入用户基本信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setName(registerVO.getMobile());
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setPassword(MD5.encrypt(registerVO.getPassword()));
        userInfo.setStatus(UserInfo.STATUS_NORMAL); //正常
        //设置一张静态资源服务器上的头像图片
        userInfo.setHeadImg("https://curiophil-srb-file.oss-cn-nanjing.aliyuncs.com/doraemon.jpg");
        baseMapper.insert(userInfo);

        //创建会员账户
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        userAccountMapper.insert(userAccount);
    }

    @Transactional( rollbackFor = {Exception.class})
    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        Integer userType = loginVO.getUserType();

        //获取会员
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        queryWrapper.eq("user_type", userType);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);

        //用户不存在
        //LOGIN_MOBILE_ERROR(-208, "用户不存在"),
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);

        //校验密码
        //LOGIN_PASSWORD_ERROR(-209, "密码不正确"),
        Assert.equals(MD5.encrypt(password), userInfo.getPassword(), ResponseEnum.LOGIN_PASSWORD_ERROR);

        //用户是否被禁用
        //LOGIN_DISABLED_ERROR(-210, "用户已被禁用"),
        Assert.equals(userInfo.getStatus(), UserInfo.STATUS_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);

        //记录登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(userInfo.getId());
        userLoginRecord.setIp(ip);
        userLoginRecordMapper.insert(userLoginRecord);

        //生成token
        String token = JwtUtils.createToken(userInfo.getId(), userInfo.getName());
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setToken(token);
        userInfoVO.setName(userInfo.getName());
        userInfoVO.setNickName(userInfo.getNickName());
        userInfoVO.setHeadImg(userInfo.getHeadImg());
        userInfoVO.setMobile(userInfo.getMobile());
        userInfoVO.setUserType(userType);

        return userInfoVO;
    }

    @Override
    public IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery) {

        String mobile = userInfoQuery.getMobile();
        Integer status = userInfoQuery.getStatus();
        Integer userType = userInfoQuery.getUserType();

        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        if(userInfoQuery == null){
            return baseMapper.selectPage(pageParam, null);
        }

        userInfoQueryWrapper
                .eq(StringUtils.isNotBlank(mobile), "mobile", mobile)
                .eq(status != null, "status", userInfoQuery.getStatus())
                .eq(userType != null, "user_type", userType);
        return baseMapper.selectPage(pageParam, userInfoQueryWrapper);
    }

    @Override
    public void lock(Long id, Integer status) {
        //        UserInfo userInfo = this.getById(id);//select
        //        userInfo.setStatus(1);
        //        this.updateById(userInfo);//update
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        baseMapper.updateById(userInfo);
    }


    @Override
    public boolean checkMobile(String mobile) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", mobile);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public UserIndexVO getIndexUserInfo(Long userId) {

        //用户信息
        UserInfo userInfo = baseMapper.selectById(userId);

        //账户信息
        QueryWrapper<UserAccount> userAccountQueryWrapper = new QueryWrapper<>();
        userAccountQueryWrapper.eq("user_id", userId);
        UserAccount userAccount = userAccountMapper.selectOne(userAccountQueryWrapper);

        //登录信息
        QueryWrapper<UserLoginRecord> userLoginRecordQueryWrapper = new QueryWrapper<>();
        userLoginRecordQueryWrapper
                .eq("user_id", userId)
                .orderByDesc("id")
                .last("limit 1");
        UserLoginRecord userLoginRecord = userLoginRecordMapper.selectOne(userLoginRecordQueryWrapper);
//        result.put("userLoginRecord", userLoginRecord);

        //组装结果数据
        UserIndexVO userIndexVO = new UserIndexVO();
        userIndexVO.setUserId(userInfo.getId());
        userIndexVO.setUserType(userInfo.getUserType());
        userIndexVO.setName(userInfo.getName());
        userIndexVO.setNickName(userInfo.getNickName());
        userIndexVO.setHeadImg(userInfo.getHeadImg());
        userIndexVO.setBindStatus(userInfo.getBindStatus());
        userIndexVO.setAmount(userAccount.getAmount());
        userIndexVO.setFreezeAmount(userAccount.getFreezeAmount());
        userIndexVO.setLastLoginTime(userLoginRecord.getCreateTime());

        return userIndexVO;
    }

    @Override
    public String getMobileByBindCode(String bindCode) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("bind_code", bindCode);
        UserInfo userInfo = baseMapper.selectOne(userInfoQueryWrapper);
        return userInfo.getMobile();
    }
}
