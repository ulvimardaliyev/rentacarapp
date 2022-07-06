package az.rentacar.general.gateway.security.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class AuthenticationPrefilter extends AbstractGatewayFilterFactory<AuthenticationPrefilter.Config> {

    List<String> excludedUrls = List.of("/api/test/all");
    private final WebClient.Builder webClientBuilder;

    public AuthenticationPrefilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new RuntimeException("Headers is not on incoming request");
            }
            String header = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).stream().findFirst().get();
            return webClientBuilder
                    .build()
                    .get()
                    .uri("lb://AUTH-SERVICE/api/auth/validateToken")
                    .header(HttpHeaders.AUTHORIZATION, header)
                    .retrieve()
                    .bodyToMono(ConnValidationResponse.class)
                    .map(connValidationResponse -> {
                        exchange
                                .getRequest()
                                .mutate()
                                .header("x-user-id", connValidationResponse.getToken());
                        log.info("Header is {}", connValidationResponse.getToken());
                        return exchange;
                    }).flatMap(chain::filter)
                    .onErrorResume(error -> {
                                log.info("Error Happened");
                                HttpStatus errorCode = null;
                                String errorMsg = "";
                                log.info("Exception {}", error);
                                if (error instanceof WebClientResponseException) {
                                    WebClientResponseException webCLientException = (WebClientResponseException) error;
                                    errorCode = webCLientException.getStatusCode();
                                    errorMsg = webCLientException.getStatusText();
                                } else {
                                    errorCode = HttpStatus.BAD_GATEWAY;
                                    errorMsg = HttpStatus.BAD_GATEWAY.getReasonPhrase();
                                }
                                return onError(exchange, errorCode.value(), errorMsg,  errorCode);
                            }
                    );
        };
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class ConnValidationResponse {
        public String token;
    }

    @NoArgsConstructor
    static class Config {
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class ExceptionResponseModel {
        public Integer errCode;
        public String err;
        //public String errDetails;
        public String details;
        public Date date;
    }

    private Mono<Void> onError(ServerWebExchange exchange, Integer errCode, String err,  HttpStatus httpStatus) {
        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
        ObjectMapper objMapper = new ObjectMapper();
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        try {
            response.getHeaders().add("Content-Type", "application/json");
            ExceptionResponseModel data = new ExceptionResponseModel(errCode, err, "Unauthorized user", new Date());
            byte[] byteData = objMapper.writeValueAsBytes(data);
            return response.writeWith(Mono.just(byteData).map(dataBufferFactory::wrap));

        } catch (JsonProcessingException e) {
            e.printStackTrace();

        }
        return response.setComplete();
    }
}
