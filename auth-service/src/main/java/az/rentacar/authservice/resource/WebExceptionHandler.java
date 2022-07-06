package az.rentacar.authservice.resource;

import az.rentacar.authservice.dto.response.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
//
//@Slf4j
//@ControllerAdvice
//public class WebExceptionHandler {
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<MessageResponse> send(RuntimeException runtimeException) {
//        MessageResponse messageResponse = new MessageResponse();
//        messageResponse.setMessage(runtimeException.getMessage());
//        log.info("ismayil {}", messageResponse);
//        return ResponseEntity.badRequest().body(messageResponse);
//    }
//}
