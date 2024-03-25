package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    @Override
    public TurnoverReportVO getTurnoverReportVO(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Double> turoverList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turover = orderMapper.sumByMap(map);
            turover  = turover == null ? 0.0 : turover;
            turoverList.add(turover);
        }



        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserReportVO(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //每天新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        //每天总用户数量
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            Integer total = userMapper.countByMap(map);
            totalUserList.add(total);
            map.put("end", endTime);
            Integer new_ = userMapper.countByMap(map);
            newUserList.add(new_);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderReportVO(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //每天新增用户数量
        List<Integer> completeOrderList = new ArrayList<>();
        //每天总用户数量
        List<Integer> totalOrderList = new ArrayList<>();
        for(LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer totalOrder = orderMapper.countByMap(map);
            totalOrder  = totalOrder == null ? 0 : totalOrder;
            map.put("status", Orders.COMPLETED);
            Integer completeOrder = orderMapper.countByMap(map);
            completeOrder  = completeOrder == null ? 0 : completeOrder;

            completeOrderList.add(completeOrder);
            totalOrderList.add(totalOrder);
        }

        Double orderCompletionRate = 0.0;
        Integer totalOrderCount = totalOrderList.stream().reduce(Integer::sum).get();
        Integer completeOrderCount = completeOrderList.stream().reduce(Integer::sum).get();
        if(totalOrderCount != 0){
            orderCompletionRate = completeOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(completeOrderCount)
                .orderCountList(StringUtils.join(completeOrderList, ","))
                .validOrderCountList(StringUtils.join(totalOrderList, ","))
                .build();
    }



    @Override
    public SalesTop10ReportVO getSalesTop10ReportVO(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        List<String> NameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> NumberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(NameList, ","))
                .numberList(StringUtils.join(NumberList, ","))
                .build();
    }


}
