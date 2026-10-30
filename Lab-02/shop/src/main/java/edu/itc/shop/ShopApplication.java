package edu.itc.shop;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import edu.itc.shop.service.ReportBuilder;
import edu.itc.shop.service.WelcomeService;

@SpringBootApplication
@ConfigurationPropertiesScan 
public class ShopApplication {
    private static final Logger log = LoggerFactory.getLogger(ShopApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(ApplicationContext ctx,
                                     ObjectProvider<WelcomeService> welcomeServiceProvider,
                                     ObjectProvider<ReportBuilder> reportBuilderProvider) {
        return args -> {
            // Count and list Notifier beans
            String[] allBeanNames = ctx.getBeanDefinitionNames();
            List<String> notifierBeans = Arrays.stream(allBeanNames)
                    .filter(name -> name.toLowerCase().contains("notifier"))
                    .toList();

            log.info("{} beans; notifiers = {}", allBeanNames.length, notifierBeans);

            // Execute service calls (only when WelcomeService is available)
            welcomeServiceProvider.ifAvailable(ws -> {
                ws.welcome("ann", false);
                ws.welcome("bob", true);
            });

            // Verify prototype instances (only when ReportBuilder is available)
            reportBuilderProvider.ifAvailable(rb -> {
                ReportBuilder r1 = reportBuilderProvider.getObject();
                ReportBuilder r2 = reportBuilderProvider.getObject();
                boolean differ = System.identityHashCode(r1) != System.identityHashCode(r2);
                log.info("prototype instances differ: {}", differ);
            });
        };
    }
}