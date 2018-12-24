package com.mryx.matrix.process.core.service.impl;

import java.util.List;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.mryx.matrix.process.core.dao.UserDao;
import com.mryx.matrix.process.core.service.UserService;

import com.mryx.matrix.process.domain.User;


/**
 * 
 * @author zhaoxl
 * @email zhaoxl02@missfresh.cn
 * @date 2018-09-05 13:18
 **/
@Service("userService")
public class UserServiceImpl implements UserService {

	private static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Resource
	private UserDao userDao;

	@Override
	public User getById(Integer id) {
		return userDao.getById(id);
	}

	@Override
	public int insert(User user) {
		return userDao.insert(user);
	}

	@Override
	public int updateById(User user) {
		return userDao.updateById(user);
	}

	@Override
	public int pageTotal(User user) {
		return userDao.pageTotal(user);
	}

	@Override
	public List<User> listPage(User user) {
		return userDao.listPage(user);
	}

	@Override
	public List<User> listByCondition(User user){
		return userDao.listByCondition(user);
	}

}
