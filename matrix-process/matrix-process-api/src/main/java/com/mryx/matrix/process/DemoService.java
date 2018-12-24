package com.mryx.matrix.process;


import com.mryx.matrix.process.domain.Project;

import java.util.List;

/**
 * 项目的RPC接口
 *
 * @author supeng
 * @date 2018/08/30
 */
public interface DemoService {

    List<Project> getList();
}
