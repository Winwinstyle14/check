package com.vhc.ec.customer;

import com.vhc.ec.customer.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

@SpringBootTest
class EcCustomerSrvApplicationTests {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    void contextLoads() {
       organizationRepository.findAllOrgInTree(52).forEach(System.out::println);
    }

}
