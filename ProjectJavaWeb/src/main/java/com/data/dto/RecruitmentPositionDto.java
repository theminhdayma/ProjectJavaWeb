package com.data.dto;

import com.data.entity.Technology;
import com.data.entity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecruitmentPositionDto {
    private int id;

    @NotBlank(message = "Tên vị trí không được để trống")
    private String name;

    @Size(max = 5000, message = "Mô tả quá dài")
    private String description;

    @NotNull(message = "Lương tối thiểu không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Lương tối thiểu phải lớn hơn 0")
    private Double minSalary;

    @NotNull(message = "Lương tối đa không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Lương tối đa phải lớn hơn 0")
    private Double maxSalary;

    @Min(value = 0, message = "Kinh nghiệm tối thiểu không được âm")
    private int minExperience;

    @NotNull(message = "Trạng thái không được để trống")
    private Status status = Status.ACTIVE;

    @PastOrPresent(message = "Ngày tạo không thể ở tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate = LocalDate.now();

    @NotNull(message = "Ngày hết hạn không được để trống")
    @Future(message = "Ngày hết hạn phải ở tương lai")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiredDate;

    @NotEmpty(message = "Phải chọn ít nhất một công nghệ")
    private List<Technology> technologies;

    public boolean checkSalaryRange() {
        if (minSalary != null && maxSalary != null && minSalary >= maxSalary) {
            throw new IllegalArgumentException("Lương tối thiểu phải nhỏ hơn lương tối đa");
        }
        return true;
    }
}
