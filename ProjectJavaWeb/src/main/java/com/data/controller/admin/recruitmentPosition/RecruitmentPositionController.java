package com.data.controller.admin.recruitmentPosition;

import com.data.dto.RecruitmentPositionDto;
import com.data.entity.RecruitmentPosition;
import com.data.entity.Technology;
import com.data.service.recruitmentPosition.RecruitmentPositionService;
import com.data.service.technology.TechnologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequestMapping("/admin/recruitment-position")
@RequiredArgsConstructor
public class RecruitmentPositionController {

    private final RecruitmentPositionService recruitmentPositionService;
    private final TechnologyService technologyService;

    @GetMapping
    public String recruitmentPositionManager(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            Model model) {

        List<RecruitmentPosition> positions = recruitmentPositionService.findAll(keyword, page, size);
        long totalElements = recruitmentPositionService.countAll();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("technologies", technologies);

        model.addAttribute("positions", positions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);

        return "admin/recruitmentPosition/recruitmentPositionManager";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("recruitmentPositionDto", new RecruitmentPositionDto());
        model.addAttribute("technologies", technologies);
        return "admin/recruitmentPosition/formRecruitmentPosition";
    }

    @PostMapping("/add")
    public String addRecruitmentPosition(
            @Valid @ModelAttribute RecruitmentPositionDto recruitmentPositionDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        try {
            recruitmentPositionDto.checkSalaryRange();
            RecruitmentPosition position = convertToEntity(recruitmentPositionDto);
            boolean success = recruitmentPositionService.save(position);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Thêm vị trí tuyển dụng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi thêm vị trí tuyển dụng!");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        return "redirect:/admin/recruitment-position";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        RecruitmentPosition position = recruitmentPositionService.findById(id);
        if (position == null) {
            return "redirect:/admin/recruitment-position";
        }

        List<Technology> technologies = technologyService.findAllTechnologies();
        RecruitmentPositionDto dto = convertToDto(position);

        model.addAttribute("recruitmentPositionDto", dto);
        model.addAttribute("technologies", technologies);
        return "admin/recruitmentPosition/formRecruitmentPosition";
    }

    @PostMapping("/edit/{id}")
    public String editRecruitmentPosition(
            @PathVariable int id,
            @Valid @ModelAttribute RecruitmentPositionDto recruitmentPositionDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        try {
            recruitmentPositionDto.checkSalaryRange();
            recruitmentPositionDto.setId(id);
            RecruitmentPosition position = convertToEntity(recruitmentPositionDto);
            boolean success = recruitmentPositionService.update(position);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật vị trí tuyển dụng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật vị trí tuyển dụng!");
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        return "redirect:/admin/recruitment-position";
    }

    @PostMapping("/delete/{id}")
    public String deleteRecruitmentPosition(@PathVariable int id, RedirectAttributes redirectAttributes) {
        boolean success = recruitmentPositionService.delete(id);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Xóa vị trí tuyển dụng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa vị trí tuyển dụng!");
        }

        return "redirect:/admin/recruitment-position";
    }

    private RecruitmentPosition convertToEntity(RecruitmentPositionDto dto) {
        RecruitmentPosition position = new RecruitmentPosition();
        position.setId(dto.getId());
        position.setName(dto.getName());
        position.setDescription(dto.getDescription());
        position.setMinSalary(dto.getMinSalary());
        position.setMaxSalary(dto.getMaxSalary());
        position.setMinExperience(dto.getMinExperience());
        position.setStatus(dto.getStatus());
        position.setCreatedDate(dto.getCreatedDate());
        position.setExpiredDate(dto.getExpiredDate());
        position.setTechnologies(dto.getTechnologies());
        return position;
    }

    private RecruitmentPositionDto convertToDto(RecruitmentPosition position) {
        RecruitmentPositionDto dto = new RecruitmentPositionDto();
        dto.setId(position.getId());
        dto.setName(position.getName());
        dto.setDescription(position.getDescription());
        dto.setMinSalary(position.getMinSalary());
        dto.setMaxSalary(position.getMaxSalary());
        dto.setMinExperience(position.getMinExperience());
        dto.setStatus(position.getStatus());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Xử lý ngày tạo
        if (position.getCreatedDate() != null) {
            dto.setCreatedDateFormatted(position.getCreatedDate().format(formatter));
            dto.setCreatedDate(position.getCreatedDate());
        } else {
            dto.setCreatedDateFormatted("Chưa có");
        }

        // Xử lý ngày hết hạn
        if (position.getExpiredDate() != null) {
            dto.setExpiredDateFormatted(position.getExpiredDate().format(formatter));
            dto.setExpiredDate(position.getExpiredDate());
        } else {
            dto.setExpiredDateFormatted("Chưa có");
        }

        dto.setTechnologies(position.getTechnologies());
        return dto;
    }


}
