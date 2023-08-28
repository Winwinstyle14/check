package com.vhc.ec.admin.service.schedule;

import com.vhc.ec.admin.constant.UsageStatus;
import com.vhc.ec.admin.repository.ServicePackageOrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServicePackageScheduler {
    private final ServicePackageOrganizationRepository srvRepo;

    // update trang thai cac goi dich vu theo thoi gian
    @Scheduled(cron = "0 0 0 * * *")
    private void updateStatus() {
        var services = srvRepo.findServiceNeedFinish();
        for (var srv : services) {
            log.info("start update service status : {}", srv.getId());
            srv.setUsageStatus(UsageStatus.FINISHED);
            srvRepo.save(srv);
       }
    }
}
