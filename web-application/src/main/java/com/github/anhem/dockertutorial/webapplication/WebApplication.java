package com.github.anhem.dockertutorial.webapplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @RestController
    public class Controller {

        @Value("${DATE_FORMAT:'Today is' EEEE 'the' d 'of' MMMM YYYY}")
        private String dateFormat;

        Logger log = LoggerFactory.getLogger(this.getClass());

        @GetMapping("get-date")
        public String getDate() {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            String date = sdf.format(new Date());
            log.info(date);
            return date;
        }

    }
}
