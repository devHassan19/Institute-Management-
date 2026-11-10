package com.ga.auth_service.model.request;

import lombok.*;

@Getter
@Setter
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
