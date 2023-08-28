package com.vhc.ec.contract.util;

import com.vhc.ec.contract.definition.ContractStatus;
import com.vhc.ec.contract.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Job thông báo hợp đồng sắp hết hạn / quá hạn
 */
@Component
@RequiredArgsConstructor
public class ScheduledJob {
    final NotificationService notificationService;

    //Job chạy 9h00 hàng ngày
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleTaskUsingCronExpression() throws InterruptedException {
        System.out.println(new Date() + " ==> Start scheduled contract about exprire !!!");
        notificationService.notificationExpire(ContractStatus.ABOUT_EXPRIRE.getDbVal());
        System.out.println(new Date() + " ==> End scheduled contract about exprire !!!");

        System.out.println(new Date() + " ==> Start scheduled contract exprire !!!");
        notificationService.notificationExpire(ContractStatus.EXPRIRE.getDbVal());
        System.out.println(new Date() + " ==> End scheduled contract exprire !!!");
        
        System.out.println(new Date() + " ==> Start scheduled contract exprire time !!!");
        notificationService.notificationExpireTime();
        System.out.println(new Date() + " ==> End scheduled contract exprire time !!!");
    }
}
