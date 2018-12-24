package com.mryx.matrix.process.core.dao;

import java.util.List;

import com.mryx.matrix.process.domain.BetaDTO;


/**
 * beta对应ip表
 *
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-03 18:01
 **/
public interface BetaDao {

    BetaDTO getById(Integer id);

    int insert(BetaDTO beta);

    int updateById(BetaDTO beta);

    int pageTotal(BetaDTO beta);

    List<BetaDTO> listPage(BetaDTO beta);

    List<BetaDTO> listbyIp(String ip);

    List<BetaDTO> listByCondition(BetaDTO beta);

    int deleteById(BetaDTO beta);

    int pageTotalCanUse(BetaDTO beta);

    List<BetaDTO> listPageCanUse(BetaDTO beta);
}
