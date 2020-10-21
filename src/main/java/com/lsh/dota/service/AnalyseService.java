package com.lsh.dota.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author LSH
 * @Date 2020/10/21 21:58
 */
@Service
public class AnalyseService {

    public String analyseOneMatch(JSONObject match){



        return null;
    }

    public String analyseMatch(String matches) {
        JSONArray objects = JSON.parseArray(matches);
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            Integer kills = jsonObject.getInteger("kills");
        }
        return null;
    }
}
