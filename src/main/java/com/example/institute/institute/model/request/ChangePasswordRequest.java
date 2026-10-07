package com.example.institute.institute.model.request;

import lombok.*;

@Getter
@Setter
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;
}
