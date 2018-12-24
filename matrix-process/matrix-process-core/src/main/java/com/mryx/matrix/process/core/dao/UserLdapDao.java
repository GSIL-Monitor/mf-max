package com.mryx.matrix.process.core.dao;

import com.mryx.matrix.process.domain.UserLdap;

import java.util.List;

public interface UserLdapDao {
    List<String> getUserName(UserLdap ul);
}