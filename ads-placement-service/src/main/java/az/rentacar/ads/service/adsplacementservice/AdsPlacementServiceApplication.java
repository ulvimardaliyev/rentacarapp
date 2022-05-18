package az.rentacar.ads.service.adsplacementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class AdsPlacementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdsPlacementServiceApplication.class, args);
    }

}
