package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReportService {

    /**
     * 统计指定区间内的数据
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverReportVO(
                                         LocalDate begin ,

                                         LocalDate end);


    /**
     * 统计指定区间内的用户数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserReportVO(
            LocalDate begin ,

            LocalDate end);


    /**
     * 统计指定区间内的用订单数据
     * @param begin
     * @param end
     * @return
             */
    OrderReportVO getOrderReportVO(LocalDate begin, LocalDate end);


    /**
     * 销量排名前10
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO getSalesTop10ReportVO(LocalDate begin, LocalDate end);
}
