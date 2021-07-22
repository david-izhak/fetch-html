package com.example.fetchhtml;

import com.example.fetchhtml.service.HtmlHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FetchHtmlApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(FetchHtmlApplication.class, args);
        ctx.close();
//        System.exit(0);
    }

    @Bean(initMethod= "startHandling")
    public HtmlHandler getBean() {
        return new HtmlHandler();
    }

}
