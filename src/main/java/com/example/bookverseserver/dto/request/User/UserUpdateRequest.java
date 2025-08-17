package com.example.bookverseserver.dto.request.User;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {
    String password;

    public Boolean setEnabled() {
        return true;
    }
    List<String> roles;
}
