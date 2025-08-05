package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 返回营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();

        List<LocalDate> dates = new ArrayList<>();
        List<Double> turnover = new ArrayList<>();
        dates.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            dates.add(begin);
        }
        String dateList = StringUtils.join(dates, ",");

        for(LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);

            Double sum = ordersMapper.sumByMap(map);
            turnover.add(sum);
        }
        String turnoverList = StringUtils.join(turnover, ",");
        turnoverReportVO.setTurnoverList(turnoverList);
        turnoverReportVO.setDateList(dateList);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        UserReportVO userReportVO = new UserReportVO();
        dates.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dates.add(begin);
        }

        String dateList = StringUtils.join(dates,",");
        for(LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Map map = new HashMap();
            map.put("endTime",endTime);
            Integer totalUserCount = userMapper.countByMap(map);
            map.put("beginTime",beginTime);
            Integer newUserCount = userMapper.countByMap(map);

            newUserList.add(newUserCount);
            totalUserList.add(totalUserCount);
        }
        userReportVO.setDateList(dateList);
        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList,","));
        return userReportVO;
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer allOrderCount = 0;
        Integer allValidOrderCount = 0;
        dates.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dates.add(begin);
        }
        String dateList = StringUtils.join(dates,",");

        for(LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);

            // 获取当日总订单数
            Integer orderCount = ordersMapper.countByMap(map);
            orderCountList.add(orderCount);
            allOrderCount += orderCount;
            // 获取当日有效订单数
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount = ordersMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);
            allValidOrderCount += validOrderCount;
        }
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(dateList);
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList,","));
        orderReportVO.setTotalOrderCount(allOrderCount);
        orderReportVO.setValidOrderCount(allValidOrderCount);
        orderReportVO.setOrderCompletionRate(allValidOrderCount.doubleValue()/allOrderCount.doubleValue());
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO salesTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> list = orderDetailMapper.getTop10(beginTime,endTime);
        List<String> nameList = list.stream().map(GoodsSalesDTO::getName).toList();
        List<Integer> numberList = list.stream().map(GoodsSalesDTO::getNumber).toList();
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(StringUtils.join(nameList,","));
        salesTop10ReportVO.setNumberList(StringUtils.join(numberList,","));
        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        BusinessDataVO vo = workspaceService.getBusinessData
                (LocalDateTime.of(begin,LocalTime.MIN),LocalDateTime.of(end,LocalTime.MAX));

        // 写入Excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try{
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("日期:"+begin+"-"+end);
            sheet.getRow(3).getCell(2).setCellValue(vo.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(vo.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(vo.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(vo.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(vo.getUnitPrice());

            for(int i=0;i<30;i++){
                vo = workspaceService.getBusinessData
                        (LocalDateTime.of(begin,LocalTime.MIN),LocalDateTime.of(begin,LocalTime.MAX));
                sheet.getRow(7+i).getCell(1).setCellValue(begin.toString());
                sheet.getRow(7+i).getCell(2).setCellValue(vo.getTurnover());
                sheet.getRow(7+i).getCell(3).setCellValue(vo.getValidOrderCount());
                sheet.getRow(7+i).getCell(4).setCellValue(vo.getOrderCompletionRate());
                sheet.getRow(7+i).getCell(5).setCellValue(vo.getUnitPrice());
                sheet.getRow(7+i).getCell(6).setCellValue(vo.getNewUsers());
                begin = begin.plusDays(1);
            }
            // 下载至客户端

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
