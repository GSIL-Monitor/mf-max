package com.mryx.matrix.process.provider;

import com.mryx.matrix.process.DemoService;
import com.mryx.matrix.process.domain.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Project Dubbo provider
 *
 * @author supeng
 * @date 2018/09/02
 */
@Service("demoService")
public class DemoServiceImpl implements DemoService{

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public List<Project> getList() {
        LOGGER.info("getList params = {}","test");
        return new ArrayList<>();
    }
}
