package com.sparta.perdayonespoon.service;

import com.sparta.perdayonespoon.domain.Goal;
import com.sparta.perdayonespoon.domain.MsgCollector;
import com.sparta.perdayonespoon.domain.dto.CountDto;
import com.sparta.perdayonespoon.domain.dto.request.GoalDto;
import com.sparta.perdayonespoon.domain.dto.response.GoalRateDto;
import com.sparta.perdayonespoon.domain.dto.response.GoalResponseDto;
import com.sparta.perdayonespoon.jwt.Principaldetail;
import com.sparta.perdayonespoon.repository.GoalRepository;
import com.sparta.perdayonespoon.util.GenerateMsg;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MainService {

    private static Stack<String> socialst = new Stack<>();

    private static Stack<Boolean> goalst = new Stack<>();

    private static double totalcount = 0;

    private static double truecount = 0;

    private static long period=0;

    private final GoalRepository goalRepository;
    public ResponseEntity getGoal(Principaldetail principaldetail) {
        int day = LocalDate.now().getDayOfWeek().getValue();
        List<GoalRateDto> goalRateDtos;
        if(day != 6 && day != 7) {
            LocalDateTime sunday = LocalDateTime.now().minusDays(day);
            LocalDateTime saturday = LocalDateTime.now().plusDays(6-day);
            goalRateDtos = goalRepository.getRateGoal(sunday,saturday,principaldetail.getMember().getSocialId());
            goalRateDtos.forEach(this::setRate);
        }else if(day == 6){
            LocalDateTime sunday = LocalDateTime.now().minusDays(day);
            LocalDateTime saturday = LocalDateTime.now();
            goalRateDtos = goalRepository.getRateGoal(sunday,saturday,principaldetail.getMember().getSocialId());
            goalRateDtos.forEach(this::setRate);
        }else {
            LocalDateTime sunday = LocalDateTime.now();
            LocalDateTime saturday = LocalDateTime.now().plusDays(6);
            goalRateDtos = goalRepository.getRateGoal(sunday,saturday,principaldetail.getMember().getSocialId());
            goalRateDtos.forEach(this::setRate);
        }
        if(!socialst.isEmpty() && goalst.isEmpty() ){
            socialst.clear();
            goalst.clear();
        }
        List<GoalRateDto> goalRateDtoList = goalRateDtos.stream().filter(GoalRateDto::isCheckGoal).collect(Collectors.toList());
        return ResponseEntity.ok(goalRateDtoList);
    }
    // 0 1 2 0 -1 0 1 1 0 -1 0  3 4 5 6
    // 0 2 4 6 8 10 ????????? ?????? //???????????????~ false false ture  {} {}    dfs -> ?????? ?????? ???????????? bfs -> ?????? ?????? ????????????
    // 1 3 5 7 9 11 ????????? ??????
    // 0+1 ???????????? 0 ???????????????
    // LIFO LAST IN FIRST OUT -> STACK -> ????????? ?????? -> dfs -> stack ?????????????????? ???????????? bfs -> queue
    // FIFO FIRST IN FIRST OUT -> QUEUE -> ????????? ??????
    //Todo: true false??? ??? ???????????? ??????????????? ??????????????? ???????????? ????????? ???????????? ???????
    private void setRate(GoalRateDto goalRateDto) {
        goalRateDto.SetTwoField(GenerateMsg.getMsg(HttpServletResponse.SC_OK, "?????? ?????? ????????? ?????????????????????."));
        if (socialst.isEmpty() && goalst.isEmpty()) {
            socialst.push(goalRateDto.getDayString());
            goalst.push(goalRateDto.isCheckGoal());
            totalcount = goalRateDto.getTotalcount();
            if (goalRateDto.isCheckGoal()) {
                truecount = goalRateDto.getTotalcount();
                goalRateDto.setTotalcount((long) totalcount);
                goalRateDto.setRate(Math.round((truecount / totalcount) * 100));
            }
        } else if (socialst.peek().equals(goalRateDto.getDayString()) && goalst.peek() == !goalRateDto.isCheckGoal()) {
            socialst.pop();
            goalst.pop();
            totalcount += goalRateDto.getTotalcount();
            if (goalRateDto.isCheckGoal()) {
                truecount = goalRateDto.getTotalcount();
                goalRateDto.setTotalcount((long) totalcount);
                goalRateDto.setRate(Math.round((truecount / totalcount) * 100));
            }
        } else if (!socialst.peek().equals(goalRateDto.getDayString())) {
            socialst.pop();
            goalst.pop();
            totalcount = goalRateDto.getTotalcount();
            if (goalRateDto.isCheckGoal()) {
                truecount = goalRateDto.getTotalcount();
                goalRateDto.setRate(Math.round((truecount / totalcount) * 100));
//            } else if (socialst.peek().equals("???")) {
//                socialst.pop();
//                goalst.pop();
//                totalcount = goalRateDto.getTotalcount();
//                if (goalRateDto.isCheckGoal()) {
//                    truecount = goalRateDto.getTotalcount();
//                    goalRateDto.setRate(Math.round((truecount / totalcount) * 100));
//                }
//                socialst.push(goalRateDto.getDayString());
//                goalst.push(goalRateDto.isCheckGoal());
//            }
//        if(goalRateDto.isCheckGoal()){
//            truecount = goalRateDto.getTotalcount();
//            goalRateDto.setRate(Math.round((truecount/totalcount)*100));
//            totalcount =0;
//            truecount =0;
//        }
//        if(i != 1) {
//            totalcount += goalRateDto.getTotalcount();
//            if(goalRateDto.isCheckGoal()){
//                truecount = goalRateDto.getTotalcount();
//                goalRateDto.setRate(Math.round((truecount/totalcount)*100));
//                totalcount=0;
//                truecount=0;
//            }
//            i++;
//        } else if(i == 1){
//            totalcount += goalRateDto.getTotalcount();
//            if(goalRateDto.isCheckGoal()){
//                truecount = goalRateDto.getTotalcount();
//                goalRateDto.setRate(Math.round((truecount/totalcount)*100));
//                totalcount=0;
//                truecount=0;
//            }
//            i--;
//        }
            }
        }
    }

    // TODO : ?????? ?????? ??????X ?????? ????????? ????????????
    public ResponseEntity CreateGoal(GoalDto goalDto, Principaldetail principaldetail) {
        if(goalDto.getTitle() == null){
            throw new IllegalArgumentException("????????? ??????????????????");
        } else if (goalDto.getCharacterId() == 0){
            throw new IllegalArgumentException("???????????? ????????? ?????????");
        }
        int x=0;
        List<Goal> goalList = new ArrayList<>();
        if(checkdate(LocalTime.parse(goalDto.time),goalDto.category)){
            while(period -->0){
                goalList.add(Goal.builder()
                        .achievementCheck(goalDto.achievementCheck)
                        .start_date(LocalDateTime.now())
                        .currentdate(LocalDateTime.now().plusDays(x))
                        .end_date(LocalDateTime.now().plusDays(goalDto.category))
                        .privateCheck(goalDto.privateCheck)
                        .socialId(principaldetail.getMember().getSocialId())
                        .time(goalDto.time)
                        .category(goalDto.category)
                        .characterId(goalDto.characterId)
                        .title(goalDto.title)
                        .build());
                x++;
            }
            goalRepository.saveAll(goalList);
            List<GoalResponseDto> goalResponseDtoList = new ArrayList<>();
            goalList.forEach(Goal -> goalResponseDtoList.add(GoalResponseDto.builder()
                    .currentdate(Goal.getCurrentdate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).substring(0,10))
                    .achievementCheck(Goal.isAchievementCheck())
                    .id(Goal.getId())
                    .socialId(Goal.getSocialId())
                    .title(Goal.getTitle())
                    .category(Goal.getCategory())
                    .characterId(Goal.getCharacterId())
                    .end_date(Goal.getEnd_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).substring(0,10))
                    .start_date(Goal.getStart_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).substring(0,10))
                    .privateCheck(Goal.isPrivateCheck())
                    .time(Goal.getTime())
                    .msgDto(GenerateMsg.getMsg(MsgCollector.CREATE_GOALS.getCode(), MsgCollector.CREATE_GOALS.getMsg()))
                    .build()));
            return ResponseEntity.ok(goalResponseDtoList);
        }
            throw new IllegalArgumentException("????????? ?????? 5???????????? ?????? ????????? ???????????????.");
    }

    private boolean checkdate (LocalTime time, long category){
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime end_date = localDateTime.plusDays(category);
        period = Period.between(localDateTime.toLocalDate(),end_date.toLocalDate()).getDays()+1;
        Optional<CountDto> countDto = goalRepository.getCountGoal(localDateTime);
        if(countDto.isPresent()) {
            if (countDto.get().getTotalcount() >= 5) {
                throw new IllegalArgumentException("????????? ????????? ?????? 5???????????? ???????????????. ?????? ??????????????????");
            }
        }
        if(time.getHour() == 0 && time.getMinute() == 0){
            throw new IllegalArgumentException("????????? ????????? ???????????? ????????? ????????? ??????????????????");
        }
        if(localDateTime.getDayOfMonth() == localDateTime.plusHours(time.getHour()).getDayOfMonth()) {
            LocalDateTime localDateTime1 = localDateTime.plusHours(time.getHour());
            if (localDateTime.getDayOfMonth() == localDateTime1.plusMinutes(time.getMinute()).getDayOfMonth()) {
                return true;
            } else
                throw new IllegalArgumentException("????????? ?????? ????????? ????????? ??? ????????????. ?????? ????????? ?????????");
        }
        else
            throw new IllegalArgumentException("????????? ?????? ????????? ????????? ??? ????????????. ?????? ????????? ?????????");
    }

//    public ResponseEntity ChangeGoal(long goalId, Principaldetail principaldetail) {
//        Goal goal = goalRepository.findById(goalId).orElseGet(this::error);
//    }

//    private Goal error() {
//    }
//
//    private void error(Optional<Goal> goal)
}