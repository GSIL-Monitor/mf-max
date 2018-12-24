package com.mryx.matrix.process.web.controller;

import com.mryx.matrix.process.core.utils.GitlabUtil;
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
    public Object healthcheck() {
        return "success";
    }

    @GetMapping(path = "/deleteBranch")
    public String deleteBranch(String gitAddress, String branchName) {
        try {
            GitlabUtil.deleteBranch(gitAddress, branchName);
        } catch (Exception e) {
            return null;
        }
        return "success";
    }
}
