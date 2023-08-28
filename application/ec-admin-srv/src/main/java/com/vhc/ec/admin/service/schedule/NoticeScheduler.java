package com.vhc.ec.admin.service.schedule;

import com.vhc.ec.admin.dto.ServiceExpiredRequest;
import com.vhc.ec.admin.entity.User;
import com.vhc.ec.admin.repository.ServicePackageOrganizationRepository;
import com.vhc.ec.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoticeScheduler {

    private final UserRepository userRepository;

    private final ServicePackageOrganizationRepository servicePackageOrganizationRepository;

    private final RestTemplate restTemplate;

    @Scheduled(cron = "0 0 1 * * *")
    public void sendServiceExpiredNotification() {
        final var emails = userRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        var services = servicePackageOrganizationRepository.findServiceExpiredHasNotNotice();
        for (var service : services) {
            var req = new ServiceExpiredRequest();
            req.setService(service.getServicePackage().getName());
            req.setOrg(service.getOrganization().getName());
            req.setEmails(emails);

            try {
                restTemplate.postForObject("http://ec-notification-srv/api/v1/internal/notification/serviceExpired", req, Object.class);
                service.setSentExpiredNotification(true);
                servicePackageOrganizationRepository.save(service);
            } catch (Exception e) {
                log.error("error", e);
            }
        }
    }
}
