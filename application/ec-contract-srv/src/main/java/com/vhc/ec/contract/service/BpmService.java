package com.vhc.ec.contract.service;

import com.vhc.ec.contract.dto.MessageDto;
import com.vhc.ec.contract.dto.WorkflowDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Gọi tới tài nguyên xử lý quy trình nghiệp vụ,
 * trên hệ thống BPM.
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BpmService {

    private final RestTemplate restTemplate;
    @Value("${vhc.ec.micro-services.bpm.api-url}")
    private String apiUrl;

    /**
     * Gọi dịch vụ khởi tạo quy trình nghiệp vụ,
     * trên hệ thống xử lý quy trình nghiệp vụ
     *
     * @param workflowDto Thông tin của đối tượng đưa vào quy trình xử lý
     * @return Thông báo từ quy trình xử lý trả về
     */
    public Optional<MessageDto> startWorkflow(WorkflowDto workflowDto) {
        log.info("start bpmn: {}" +workflowDto);
        final var request = new HttpEntity<>(workflowDto);

        final var response = restTemplate.postForEntity(
                String.format("%s/internal/bpmn/startSignFlowInstance", apiUrl),
                request, MessageDto.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            final var messageDto = response.getBody();

            if (messageDto != null && !messageDto.isSuccess()) {
                log.error(String.format("can't call bpm workflow api. %s", messageDto.getMessage()));
            }

            return Optional.ofNullable(messageDto);
        }

        return Optional.empty();
    }

}
