package com.data.dto;

import com.data.entity.Technology;
import com.data.entity.enums.Gender;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidateDto {

    private int id;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @Min(value = 0, message = "Kinh nghiệm phải lớn hơn hoặc bằng 0")
    private int experience;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    private String description;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    private List<Integer> technologyIds;
    private List<Technology> technologies;
}
