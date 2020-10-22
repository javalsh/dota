package com.lsh.dota.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;


/**
 * @Description TODO
 * @Author LSH
 * @Date 2020/10/21 21:58
 */
@Slf4j
@Service
public class AnalyseService {

    @Value("classpath:/data.json")
    private Resource dota;
    @Autowired
    private OpenDotaService openDotaService;

    public String analyseOneMatch(JSONObject match, String personaname) {
        ObjectMapper resourceMapper = new ObjectMapper();
        JSONObject resource= null;
        try {
            resource = resourceMapper.readValue(dota.getInputStream(), new TypeReference<JSONObject>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        Integer match_id = match.getInteger("match_id");
        Integer game_mode = match.getInteger("game_mode");
        //活动模式不解析
        if (game_mode==15||game_mode==19){
            log.info("'Match ID:%s 为活动模式, 跳过",match_id);
            return null;
        }
        //roll车不解析
        Integer duration = match.getInteger("duration");
        if (duration<=600){
            log.info("Match ID:%s 低于10分钟猜测roll车或者掉线, 跳过",match_id);
        }
        //比赛是否胜利
        boolean win = analyseMatchWinOrLose(match);
        Integer kills = match.getInteger("kills");
        Integer deaths = match.getInteger("deaths");
        Integer assists = match.getInteger("assists");
        String kda = calculateKda(kills, deaths, assists);
        Double kdaDouble = Double.valueOf(kda);
        String emoji = kdaEmoji(kdaDouble);
        //是否夸夸
        //获胜 KDA大于10 或 失败 KDA大于6 夸;获胜 KDA小于4 或者失败KDA小于1 喷;其余随机
        boolean postive;
        if ((win && kdaDouble>10) ||(!win && kdaDouble>6)){
            postive = true;
            //
        }else if((win && kdaDouble<4)||(!win && kdaDouble<1)){
            postive = false;
        }else {
            if (new Random().nextInt(10)==0){
                postive = true;
            }else {
                postive = false;
            }
        }
        //生成标题语句
        StringBuilder printStr = new StringBuilder();
        if (win && postive){
            JSONArray winPostive = resource.getJSONArray("WIN_POSTIVE");
            printStr.append(String.format(randomGet(winPostive), personaname));
            printStr.append(":heart: **+30**\n");
        }else if (win && !postive){
            JSONArray winNegative = resource.getJSONArray("WIN_NEGATIVE");
            printStr.append(String.format(randomGet(winNegative), personaname));
            printStr.append(":heart: **+30**\n");
        }else if (!win && postive){
            JSONArray losePostive = resource.getJSONArray("LOSE_POSTIVE");
            printStr.append(String.format(randomGet(losePostive), personaname));
            printStr.append(":heart: **-30**\n");
        }else {
            JSONArray loseNegative = resource.getJSONArray("LOSE_NEGATIVE");
            printStr.append(String.format(randomGet(loseNegative), personaname));
            printStr.append(":heart: **-30**\n");
        }
        //生成比赛详情
        //计算时间
        long startTimeInt = match.getLong("start_time");
        LocalDateTime startTime = Instant.ofEpochMilli(startTimeInt*1000L).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = dateTimeFormatter.format(startTime);
        printStr.append(String.format("比赛开始时间: %s\n", startTimeStr));


        printStr.append(String.format("比赛持续时间: %s分 %s秒,", duration/60,duration%60));

        Integer heroId = match.getInteger("hero_id");
        JSONArray heroesListChinese = resource.getJSONArray("HEROES_LIST_CHINESE");
        String heroesName = "";
        for (int k = 0; k < heroesListChinese.size(); k++) {
            JSONObject heroesJSONObject = heroesListChinese.getJSONObject(k);
            String id = heroesJSONObject.getString("id");
            if (Integer.valueOf(id).equals(heroId)){
                heroesName = heroesJSONObject.getString("name");
            }
        }

        printStr.append(String.format("使用英雄: %s\n", heroesName));
        printStr.append(String.format("战绩: [%s/%s/%s] \n", match.getInteger("kills"),match.getInteger("deaths"),match.getInteger("assists")));
        printStr.append(String.format("KDA: %s, %s\n", kda,emoji));
        printStr.append(String.format("每分钟金钱数: %s\n", match.getInteger("gold_per_min")));
        printStr.append(String.format("每分钟经验数: %s\n", match.getInteger("xp_per_min")));
        printStr.append(String.format("补刀数: %s\n", match.getInteger("last_hits")));

        Integer gameModeInt = match.getInteger("game_mode");
        Integer lobbyTypeInt = match.getInteger("lobby_type");
        JSONArray gameModeList = resource.getJSONArray("GAME_MODE");
        JSONArray lobbyList = resource.getJSONArray("LOBBY");
        String gameModeStr="",lobbyStr="";
        for (int i = 0; i < gameModeList.size(); i++) {
            JSONObject gameMode = gameModeList.getJSONObject(i);
            String id = gameMode.getString("id");
            if (Integer.valueOf(id).equals(gameModeInt)){
                gameModeStr = gameMode.getString("name");
            }
        }
        for (int j = 0; j < lobbyList.size(); j++) {
            JSONObject lobby = lobbyList.getJSONObject(j);
            String id = lobby.getString("id");
            if (Integer.valueOf(id).equals(lobbyTypeInt)){
                lobbyStr = lobby.getString("name");
            }
        }

        printStr.append(String.format("游戏模式: [%s/%s]\n", gameModeStr,lobbyStr));
        printStr.append(String.format("战绩详情: https://www.dotabuff.com/matches/%s \n", match_id));

        return printStr.toString();
    }

    private String kdaEmoji(Double kda) {
        String emoji = "";
        if (kda > 10){
            emoji = ":thumbsup:";
        }else if (kda<2){
            emoji = ":scream:";
        }else {
            emoji = "";
        }
        return emoji;
    }

    private String calculateKda(Integer kills, Integer deaths, Integer assists) {
        if (deaths==0){
            deaths=1;
        }
        DecimalFormat df = new DecimalFormat("0.0");
        String kda = df.format((float)(kills+assists)/deaths);

        return kda;
    }

    private boolean analyseMatchWinOrLose(JSONObject match) {
        boolean radint = true;
        Integer playerSlot = match.getInteger("player_slot");

        if (playerSlot>=128){
            radint=false;
        }else {
            radint=true;
        }
        if (radint){
            if (match.getBoolean("radiant_win")){
                return true;
            }else {
                return false;
            }
        }else {
            if (match.getBoolean("radiant_win")){
                return false;
            }else {
                return true;
            }
        }
    }

    public String analyseRecentMatch(String accountId) {
        String players = openDotaService.players(accountId);
        JSONObject playsersObject = JSON.parseObject(players);
        JSONObject profile = playsersObject.getJSONObject("profile");
        String personaname = profile.getString("personaname");
        String recentMatches = openDotaService.recentMatches(accountId);
        JSONArray matchesObjects = JSON.parseArray(recentMatches);
        String resoult = analyseOneMatch(matchesObjects.getJSONObject(0),personaname);
        return resoult;
    }
    public String analyseRecentMatchs(String accountId) {
        String players = openDotaService.players(accountId);
        JSONObject playsersObject = JSON.parseObject(players);
        JSONObject profile = playsersObject.getJSONObject("profile");
        String personaname = profile.getString("personaname");
        String recentMatches = openDotaService.recentMatches(accountId);
        JSONArray matchesObjects = JSON.parseArray(recentMatches);
        StringBuilder resoult = new StringBuilder();
        for (int i = 0; i < matchesObjects.size(); i++) {
            JSONObject matches = matchesObjects.getJSONObject(i);
            String analyseOneMatchStr = analyseOneMatch(matches, personaname);
            resoult.append(analyseOneMatchStr);
        }
        return resoult.toString();
    }

    private String randomGet(JSONArray jsonArray) {
        Random random = new Random();
        int n = random.nextInt(jsonArray.size()-1);
        return jsonArray.getString(n);
    }
}
