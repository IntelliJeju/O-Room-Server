package com.savit.card;

import com.savit.card.service.AsyncCardApprovalService;
import com.savit.card.service.CardApprovalService;
import com.savit.config.RootConfig;
import com.savit.config.SchedulerConfig;
import com.savit.budget.service.BudgetMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SchedulerConfig.class})
@TestPropertySource("classpath:application-test.properties")
class AsyncCardApprovalServiceTest {

}
