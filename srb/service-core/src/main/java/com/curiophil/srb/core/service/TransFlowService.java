package com.curiophil.srb.core.service;

import com.curiophil.srb.core.pojo.bo.TransFlowBO;
import com.curiophil.srb.core.pojo.entity.TransFlow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author chq
 * @since 2022-05-06
 */
public interface TransFlowService extends IService<TransFlow> {

    void saveTransFlow(TransFlowBO transFlowBO);

    boolean isSaveTransFlow(String agentBillNo);

    List<TransFlow> selectByUserId(Long userId);
}
