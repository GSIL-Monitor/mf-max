package com.mryx.matrix.process.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.mryx.matrix.process.core.dao.BetaDao;
import com.mryx.matrix.process.core.service.BetaService;

import com.mryx.matrix.process.domain.BetaDTO;


/**
 * beta对应ip表
 *
 * @author juqing
 * @email jvqing@missfresh.cn
 * @date 2018-09-03 18:01
 **/
@Service("betaService")
public class BetaServiceImpl implements BetaService {


    @Resource
    private BetaDao betaDao;

    @Override
    public BetaDTO getById(Integer id) {
        return betaDao.getById(id);
    }

    @Override
    public int insert(BetaDTO beta) {
        return betaDao.insert(beta);
    }

    @Override
    public int updateById(BetaDTO beta) {
        return betaDao.updateById(beta);
    }

    @Override
    public int deleteById(BetaDTO beta) {
        return betaDao.deleteById(beta);
    }

    @Override
    public int pageTotal(BetaDTO beta) {
        return betaDao.pageTotal(beta);
    }

    @Override
    public int pageTotalCanUse(BetaDTO beta) {
        return betaDao.pageTotalCanUse(beta);
    }

    @Override
    public List<BetaDTO> listPageCanUse(BetaDTO beta) {
        return betaDao.listPageCanUse(beta);
    }

    @Override
    public List<BetaDTO> listPage(BetaDTO beta) {
        return betaDao.listPage(beta);
    }

    @Override
    public List<BetaDTO> listbyIp(String ip) {
        return betaDao.listbyIp(ip);
    }

    @Override
    public List<BetaDTO> listByCondition(BetaDTO beta) {
        return betaDao.listByCondition(beta);
    }

}
