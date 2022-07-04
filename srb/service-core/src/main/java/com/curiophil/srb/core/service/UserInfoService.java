package com.curiophil.srb.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.curiophil.srb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.curiophil.srb.core.pojo.query.UserInfoQuery;
import com.curiophil.srb.core.pojo.vo.LoginVO;
import com.curiophil.srb.core.pojo.vo.RegisterVO;
import com.curiophil.srb.core.pojo.vo.UserIndexVO;
import com.curiophil.srb.core.pojo.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author chq
 * @since 2022-05-06
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);

    IPage<UserInfo> listPage(Page<UserInfo> pageParam, UserInfoQuery userInfoQuery);

    void lock(Long id, Integer status);

    boolean checkMobile(String mobile);

    UserIndexVO getIndexUserInfo(Long userId);

    String getMobileByBindCode(String bindCode);

}
