package uk.gov.hmcts.reform.dataextractor;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import uk.gov.hmcts.reform.mi.micore.component.HealthCheck;

@Slf4j
@SpringBootApplication(scanBasePackages = "uk.gov.hmcts.reform", exclude = {DataSourceAutoConfiguration.class})
public class DataExtractorApplication implements ApplicationRunner {

    @Autowired
    private ExtractionComponent extractionComponent;

    @Autowired
    private HealthCheck healthCheck;

    @Autowired
    private TelemetryClient client;

    @Value("${smoke.test.enabled:false}")
    private boolean smokeTest;

    @Value("${telemetry.wait.period:10000}")
    private int waitPeriod;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            if (smokeTest) {
                healthCheck.check();
            } else {
                extractionComponent.execute();
            }
        } catch (Exception e) {
            log.error("Error executing integration service", e);
            throw e;
        } finally {
            client.flush();
            waitTelemetryGracefulPeriod();
        }
    }

    private void waitTelemetryGracefulPeriod() throws InterruptedException {
        Thread.sleep(waitPeriod);
    }

    public static void main(String[] args) {
        SpringApplication.run(DataExtractorApplication.class);
    }

}
