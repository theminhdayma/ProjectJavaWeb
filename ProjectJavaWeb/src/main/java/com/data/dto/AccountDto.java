package com.data.dto;

import com.data.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    private int accountId;

    @Valid
    private CandidateDto candidateDto;

    @NotBlank(message = "Email không được để trống")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    private Status status = Status.ACTIVE;
}
