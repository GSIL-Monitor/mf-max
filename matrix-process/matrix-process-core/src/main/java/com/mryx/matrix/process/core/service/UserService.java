package com.mryx.matrix.process.core.service;

import java.util.List;
import java.util.Date;
import java.util.Map;

import com.mryx.matrix.process.domain.User;



/**
 * 
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-05 13:18
 **/
public interface UserService {

	User getById(Integer id);

	int insert(User user);

	int updateById(User user);

	int pageTotal(User user);

	List<User> listPage(User user);

	List<User> listByCondition(User user);

}
