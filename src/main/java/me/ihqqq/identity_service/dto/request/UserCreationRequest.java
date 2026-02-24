package me.ihqqq.identity_service.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.identity_service.validator.DobConstraint;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;
    @Size(min = 8, message = "INVALID_PASSWORD")
    String password;
    String firstName;
    String lastName;

    @DobConstraint(min = 7, message = "INVALID_DOB")
    LocalDate dob;

}
