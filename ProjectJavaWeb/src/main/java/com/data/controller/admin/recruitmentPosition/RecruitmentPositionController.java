package com.data.controller.admin.recruitmentPosition;

import com.data.dto.RecruitmentPositionDto;
import com.data.entity.Account;
import com.data.entity.RecruitmentPosition;
import com.data.entity.Technology;
import com.data.entity.enums.Role;
import com.data.entity.enums.Status;
import com.data.service.account.AccountService;
import com.data.service.recruitmentPosition.RecruitmentPositionService;
import com.data.service.technology.TechnologyService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequestMapping("/admin/recruitment-position")
@RequiredArgsConstructor
public class RecruitmentPositionController {

    private final RecruitmentPositionService recruitmentPositionService;
    private final TechnologyService technologyService;
    private final Cookies cookieUtils;

    @GetMapping
    public String recruitmentPositionManager(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            HttpServletRequest request,
            Model model) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        List<RecruitmentPosition> positions = recruitmentPositionService.findAll(keyword, page, size);
        long totalElements = recruitmentPositionService.countAll(keyword);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("technologies", technologies);

        model.addAttribute("positions", positions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("currentUser", currentUser);

        return "admin/recruitmentPosition/recruitmentPositionManager";
    }

    @GetMapping("/add")
    public String showAddForm(
            Model model,
            HttpServletRequest request) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("recruitmentPositionDto", new RecruitmentPositionDto());
        model.addAttribute("technologies", technologies);
        model.addAttribute("isEdit", false);
        model.addAttribute("currentUser", currentUser); // Thêm currentUser cho view
        return "admin/recruitmentPosition/formRecruitmentPosition";
    }

    @PostMapping("/add")
    public String addRecruitmentPosition(
            @Valid @ModelAttribute RecruitmentPositionDto recruitmentPositionDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) {

        // Thêm validation cho technologies
        if (recruitmentPositionDto.getTechnologies() == null ||
                recruitmentPositionDto.getTechnologies().isEmpty()) {
            bindingResult.rejectValue("technologies", "error.technologies",
                    "Vui lòng chọn ít nhất một công nghệ");
        }

        if (bindingResult.hasErrors()) {
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            model.addAttribute("isEdit", false); // Thêm isEdit flag
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        try {
            recruitmentPositionDto.checkSalaryRange();
            RecruitmentPosition position = convertToEntity(recruitmentPositionDto);

            // Load full technology objects từ database
            List<Technology> fullTechnologies = new ArrayList<>();
            if (recruitmentPositionDto.getTechnologies() != null) {
                for (Technology tech : recruitmentPositionDto.getTechnologies()) {
                    if (tech.getId() > 0) { // Kiểm tra ID hợp lệ
                        Technology fullTech = technologyService.findTechnologyById(tech.getId());
                        if (fullTech != null) {
                            fullTechnologies.add(fullTech);
                        }
                    }
                }
            }
            position.setTechnologies(fullTechnologies);
            position.setStatus(Status.ACTIVE);

            boolean success = recruitmentPositionService.save(position);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Thêm vị trí tuyển dụng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Có lỗi xảy ra khi thêm vị trí tuyển dụng!");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            model.addAttribute("isEdit", false); // Thêm isEdit flag
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        return "redirect:/admin/recruitment-position";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable int id,
            HttpServletRequest request,
            Model model) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        RecruitmentPosition position = recruitmentPositionService.findById(id);
        if (position == null) {
            return "redirect:/admin/recruitment-position";
        }

        List<Technology> technologies = technologyService.findAllTechnologies();
        RecruitmentPositionDto dto = convertToDto(position);
        model.addAttribute("technologies", technologies);

        model.addAttribute("recruitmentPositionDto", dto);
        model.addAttribute("technologies", technologies);
        model.addAttribute("isEdit", true);
        model.addAttribute("currentUser", currentUser); // Thêm currentUser cho view
        return "admin/recruitmentPosition/formRecruitmentPosition";
    }

    @PostMapping("/edit/{id}")
    public String editRecruitmentPosition(
            @PathVariable int id,
            @Valid @ModelAttribute RecruitmentPositionDto recruitmentPositionDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validate technologies
        if (recruitmentPositionDto.getTechnologies() == null ||
                recruitmentPositionDto.getTechnologies().isEmpty()) {
            bindingResult.rejectValue("technologies", "error.technologies",
                    "Vui lòng chọn ít nhất một công nghệ");
        }

        if (bindingResult.hasErrors()) {
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            model.addAttribute("isEdit", true);
            recruitmentPositionDto.setId(id); // Đảm bảo ID được set
            return "admin/recruitmentPosition/formRecruitmentPosition";
        }

        try {
            recruitmentPositionDto.checkSalaryRange();
            recruitmentPositionDto.setId(id);
            RecruitmentPosition position = convertToEntity(recruitmentPositionDto);

            // Load full technology objects từ database
            List<Technology> fullTechnologies = new ArrayList<>();
            if (recruitmentPositionDto.getTechnologies() != null) {
                for (Technology tech : recruitmentPositionDto.getTechnologies()) {
                    if (tech.getId() > 0) { // Kiểm tra ID hợp lệ
                        Technology fullTech = technologyService.findTechnologyById(tech.getId());
                        if (fullTech != null) {
                            fullTechnologies.add(fullTech);
                        }
                    }
                }
            }
            position.setTechnologies(fullTechnologies);

            boolean success = recruitmentPositionService.update(position);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật vị trí tuyển dụng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Có lỗi xảy ra khi cập nhật vị trí tuyển dụng!");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi: " + e.getMessage());
            List<Technology> technologies = technologyService.findAllTechnologies();
            model.addAttribute("technologies", technologies);
            model.addAttribute("isEdit", true); // Thêm isEdit flag
            recruitmentPositionDto.setId(id); // Đảm bảo ID được set
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
        dto.setCreatedDate(position.getCreatedDate());
        dto.setExpiredDate(position.getExpiredDate());
        dto.setTechnologies(position.getTechnologies());
        return dto;
    }

    protected Account getCurrentUser(HttpServletRequest request) {
        // Ưu tiên session trước
        HttpSession session = request.getSession(false);
        if (session != null) {
            Account userFromSession = (Account) session.getAttribute("currentUser");
            if (userFromSession != null) {
                return userFromSession;
            }
        }

        // Fallback sang cookie
        Account userFromCookie = cookieUtils.getUserFromCookie(request);
        if (userFromCookie != null) {
            // Lưu vào session cho lần sau
            request.getSession().setAttribute("currentUser", userFromCookie);
        }

        return userFromCookie;
    }

}
