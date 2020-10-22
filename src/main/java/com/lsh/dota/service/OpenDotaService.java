package com.lsh.dota.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description TODO
 * @Author LSH
 * @Date 2020/10/21 21:27
 */
@FeignClient(url = "https://api.opendota.com/api",name = "opendota")
public interface OpenDotaService {

    @GetMapping("/players/{account_id}/recentMatches")
    String recentMatches(@PathVariable String account_id);
}
