package com.mryx.matrix.process.core.service;

import com.mryx.matrix.process.domain.UserLdap;

import java.util.List;

public interface UserLdapService {
    List<String> getUserName(UserLdap ul);
}