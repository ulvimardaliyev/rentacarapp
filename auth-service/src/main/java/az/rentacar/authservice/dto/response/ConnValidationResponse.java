package az.rentacar.authservice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ConnValidationResponse implements Serializable {
//    private String accountNumber;
//    private List<GrantedAuthority> authorities;
    public String token;
}
