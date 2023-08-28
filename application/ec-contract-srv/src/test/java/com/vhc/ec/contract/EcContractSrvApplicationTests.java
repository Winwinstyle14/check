package com.vhc.ec.contract;

import com.vhc.ec.contract.dto.PkcsCreateTokenRequest;
import com.vhc.ec.contract.dto.PkcsMergeRequest;
import com.vhc.ec.contract.service.ContractService;
import com.vhc.ec.contract.service.ProcessService;
import com.vhc.ec.contract.service.SignService;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

@SpringBootTest
class EcContractSrvApplicationTests {

    @Autowired
    private SignService signService;

    @Autowired
    private ProcessService processService;

    @Autowired
    ContractService contractService;


    @Test
    void contextLoads() throws IOException {
        contractService.getBpmnFlow(16243);
    }

}
