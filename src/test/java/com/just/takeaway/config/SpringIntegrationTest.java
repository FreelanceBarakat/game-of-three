package com.just.takeaway.config;

import com.just.takeaway.TakeawayApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TakeawayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SpringIntegrationTest {
}
