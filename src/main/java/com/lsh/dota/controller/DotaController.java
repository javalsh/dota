package com.lsh.dota.controller;

import com.lsh.dota.service.AnalyseService;
import com.lsh.dota.service.DotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author LSH
 * @Date 2020/10/21 21:14
 */
@RestController
@RequestMapping("/dota")
public class DotaController {

    @Autowired
    private DotaService dotaService;
    @Autowired
    private AnalyseService analyseService;

    @GetMapping("/test")
    public String test(){
        return "success";
    }

    @GetMapping("/recentMatches")
    public String recentMatches(@RequestParam("accountId") String accountId){

        String matches = dotaService.recentMatches(accountId);
        analyseService.analyseMatch(matches);
        return null;
    }

}
