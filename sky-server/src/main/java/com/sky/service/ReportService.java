package com.sky.service;

import com.sky.vo.TurnoverReportVO;
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

}
