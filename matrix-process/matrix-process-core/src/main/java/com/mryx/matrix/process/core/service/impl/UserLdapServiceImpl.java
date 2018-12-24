package com.mryx.matrix.process.core.service.impl;

import com.mryx.matrix.process.core.dao.UserLdapDao;
import com.mryx.matrix.process.core.service.UserLdapService;
import com.mryx.matrix.process.domain.UserLdap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userLdapService")
public class UserLdapServiceImpl implements UserLdapService {
    @Autowired
    private UserLdapDao userLdapDao;
    @Override
    public List<String> getUserName(UserLdap ul) {
        return userLdapDao.getUserName(ul);
    }
}