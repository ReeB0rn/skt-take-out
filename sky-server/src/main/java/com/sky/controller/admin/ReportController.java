package com.sky.controller.admin;


import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/admin/report")
@Api(tags="管理端统计接口")
public class ReportController {

    @Autowired
    ReportService reportService;

    @GetMapping("/top10")
    @ApiOperation(value="查询销量排名top10接口")
    public Result<SalesTop10ReportVO> reportTopTen
            (@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
             @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        SalesTop10ReportVO salesTop10ReportVO = reportService.salesTop10Report(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation(value="营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics
            (@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
             @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        TurnoverReportVO vo = reportService.turnoverStatistics(begin,end);
        return Result.success(vo);
    }

    @GetMapping("/userStatistics")
    @ApiOperation(value="用户统计接口")
    public Result<UserReportVO> userStatistics
            (@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){
        UserReportVO vo = reportService.userStatistics(begin,end);
        return Result.success(vo);
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation(value="订单统计接口")
    public Result<OrderReportVO> orderStatistics
            (@DateTimeFormat(pattern="yyyy-MM-dd") LocalDate begin,
             @DateTimeFormat(pattern="yyyy-MM-dd")LocalDate end){

        OrderReportVO vo = reportService.orderStatistics(begin,end);
        return Result.success(vo);
    }

    @GetMapping("/export")
    @ApiOperation(value="导出Excel报表接口")
    public void export(HttpServletResponse response){
        reportService.exportBusinessData(response);
    }
}
