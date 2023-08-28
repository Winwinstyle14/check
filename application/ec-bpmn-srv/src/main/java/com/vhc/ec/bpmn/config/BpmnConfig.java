package com.vhc.ec.bpmn.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Configuration
@Data
public class BpmnConfig {

    @Value("${zeebe.bpmn.flow.sign-flow}")
    private String signFlow;
}
