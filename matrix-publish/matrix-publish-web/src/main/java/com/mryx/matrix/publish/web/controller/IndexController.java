package com.mryx.matrix.publish.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Index
 *
 * @author supeng
 * @date 2018/09/03
 */
@RestController
@RequestMapping("/api/matrix")
public class IndexController {

    @GetMapping(path = "/healthcheck")
    public Object healthcheck(){
        return "success";
    }
}
