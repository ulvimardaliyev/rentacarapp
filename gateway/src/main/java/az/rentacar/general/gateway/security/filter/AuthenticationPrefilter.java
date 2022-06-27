package az.rentacar.general.gateway.security.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.Serializable;
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
            String header = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
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
                    }).flatMap(chain::filter);

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
}


//private final WebClient.Builder webClientBuilder;
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    public AuthenticationPrefilter(WebClient.Builder webClientBuilder) {
//        super(Config.class);
//        this.webClientBuilder = webClientBuilder;
//    }
//
//    @Override
//    public GatewayFilter apply(Config config) {
//        return (exchange, chain) -> {
//            ServerHttpRequest request = exchange.getRequest();
//            log.info("**************************************************************************");
//            log.info("URL is - " + request.getURI().getPath());
//            String bearerToken = request.getHeaders().getFirst("Authorization");
//            log.info("Bearer Token: " + bearerToken);
//
//            if (isSecured.test(request)) {
//                return webClientBuilder.build().get()
//                        .uri("lb://AUTH-SERVICE/api/auth/validateToken")
//                        .header("Authorization", bearerToken)
//
//                        .retrieve()
//
//                        .bodyToMono(ConnValidationResponse.class)
//                        .
//                        /*.map(connValidationResponse ->
//                                exchange
//                                        .getRequest()
//                                        .mutate()
//                                        .
//
//
//                        )*/
//                        .flatMap(chain::filter).onErrorResume(error -> {
//                            log.info("Error Happened");
//                            HttpStatus errorCode = null;
//                            String errorMsg = "";
//                            if (error instanceof WebClientResponseException) {
//                                WebClientResponseException webCLientException = (WebClientResponseException) error;
//                                errorCode = webCLientException.getStatusCode();
//                                errorMsg = webCLientException.getStatusText();
//
//                            } else {
//                                errorCode = HttpStatus.BAD_GATEWAY;
//                                errorMsg = HttpStatus.BAD_GATEWAY.getReasonPhrase();
//                            }
////                            AuthorizationFilter.AUTH_FAILED_CODE
//                            return onError(exchange, String.valueOf(errorCode.value()), errorMsg, "JWT Authentication Failed", errorCode);
//                        });
//            }
//
//            return chain.filter(exchange);
//        };
//    }
//
//    public Predicate<ServerHttpRequest> isSecured = request -> excludedUrls.stream().noneMatch(uri -> request.getURI().getPath().contains(uri));
//
//
//    private Mono<Void> onError(ServerWebExchange exchange, String errCode, String err, String errDetails, HttpStatus httpStatus) {
//        DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
//        ObjectMapper objMapper = new ObjectMapper();
//        ServerHttpResponse response = exchange.getResponse();
//        response.setStatusCode(httpStatus);
//        try {
//            response.getHeaders().add("Content-Type", "application/json");
//            ExceptionResponseModel data = new ExceptionResponseModel(errCode, err, errDetails, null, new Date());
//            byte[] byteData = objectMapper.writeValueAsBytes(data);
//            return response.writeWith(Mono.just(byteData).map(t -> dataBufferFactory.wrap(t)));
//
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//
//        }
//        return response.setComplete();
//    }
//
//    @NoArgsConstructor
//    static class Config {
//
//
//    }
//
//    @AllArgsConstructor
//    @Data
//    static class ConnValidationResponse {
//        public String token;
//    }
//
//    @AllArgsConstructor
//    @Data
//    static class ExceptionResponseModel {
//        public String errCode;
//        public String err;
//        public String errDetails;
//        public String abc;
//        public Date date;
//    }

 /*{
                            exchange.getRequest().mutate().header("Authorization", connValidationResponse.getToken());
//                            exchange.getRequest().mutate().header("authorities", connValidationResponse.getAuthorities().stream().map(Authorities::getAuthority).reduce("", (a, b) -> a + "," + b));
                            log.info("Inside map");
                            return exchange;

                        }*/