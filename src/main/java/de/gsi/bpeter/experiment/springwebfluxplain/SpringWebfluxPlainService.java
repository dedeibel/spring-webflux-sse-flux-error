package de.gsi.bpeter.experiment.springwebfluxplain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
public class SpringWebfluxPlainService {

    @SuppressWarnings("resource")
    public static void main(final String[] args) {
        SpringApplication.run(SpringWebfluxPlainService.class, args);
    }

}
