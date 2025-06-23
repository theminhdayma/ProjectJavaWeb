package com.data.controller.admin.application;

import com.data.dto.ApplicationDto;
import com.data.entity.Account;
import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;
import com.data.entity.enums.Role;
import com.data.service.application.ApplicationService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequestMapping("/admin/applications")
@RequiredArgsConstructor
public class ApplicationManagerController {

    private final ApplicationService applicationService;
    private final Cookies cookieUtils;

    @GetMapping
    public String listApplications(
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "progress", required = false) Progress progress,
            @RequestParam(value = "resultInterview", required = false) ResultInterview resultInterview,
            @RequestParam(value = "page", defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(value = "size", defaultValue = "" + DEFAULT_SIZE) int size,
            HttpServletRequest request,
            Model model) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        List<Application> applications = applicationService.findAll(keyword, progress, resultInterview, page, size);
        long totalElements = applicationService.countAll(keyword, progress, resultInterview);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<ApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("applicationManager", applicationDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedProgress", progress);
        model.addAttribute("selectedResultInterview", resultInterview);
        model.addAttribute("progressValues", Progress.values());
        model.addAttribute("resultInterviewValues", ResultInterview.values());
        model.addAttribute("currentUser", currentUser); // Thêm thông tin người dùng hiện tại

        return "admin/application/applicationManager";
    }

    @GetMapping("/{id}")
    public String viewApplication(
            @PathVariable int id,
            HttpServletRequest request,
            Model model) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        Application application = applicationService.findById(id);
        if (application == null) {
            return "redirect:/admin/applications?error=notfound";
        }

        if("PENDING".equals(application.getProgress().name())) {
            application.setProgress(Progress.HANDING);
        }
        ApplicationDto applicationDto = convertToDto(application);
        applicationService.update(application);
        model.addAttribute("applicationDetail", applicationDto);
        model.addAttribute("currentUser", currentUser); // Thêm thông tin người dùng hiện tại
        return "admin/application/application_detail";
    }

    // Hẹn lịch phỏng vấn
    @PostMapping("/{id}/schedule-interview")
    public String scheduleInterview(
            @PathVariable int id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime interviewDate,
            @RequestParam(required = false) String interviewLink,
            RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.findById(id);
            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/admin/applications";
            }

            application.setInterviewDate(interviewDate);
            application.setProgress(Progress.WAITING_FOR_INTERVIEW_CONFIRM);
            application.setInterviewLink(interviewLink);

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hẹn lịch phỏng vấn thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/applications/" + id;
    }

    // Từ chối đơn ứng tuyển
    @PostMapping("/{id}/reject")
    public String rejectApplication(@PathVariable int id,
                                    @RequestParam String rejectedReason,
                                    RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.findById(id);
            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/admin/applications";
            }

            application.setProgress(Progress.REJECTED);
            application.setRejectedReason(rejectedReason);
            application.setRejectedAt(LocalDateTime.now());

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn ứng tuyển thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/applications/" + id;
    }

    // Cập nhật kết quả phỏng vấn
    @PostMapping("/{id}/update-interview")
    public String updateInterviewResult(
            @PathVariable int id,
            @RequestParam ResultInterview resultInterview,
            RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.findById(id);
            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/admin/applications";
            }

            application.setResultInterview(resultInterview);
            application.setProgress(Progress.DONE);

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật kết quả phỏng vấn thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/applications/" + id;
    }

    // Xác nhận lịch phỏng vấn mới (khi ứng viên yêu cầu đổi lịch)
    @PostMapping("/{id}/confirm-reschedule")
    public String confirmReschedule(@PathVariable int id,
                                    RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.findById(id);
            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/admin/applications";
            }

            application.setInterviewDate(application.getConfirmInterviewDate());
            application.setProgress(Progress.INTERVIEWING);

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận lịch phỏng vấn mới thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/applications/" + id;
    }

    // Xác nhận chuyển sang giai đoạn phỏng vấn
    @PostMapping("/{id}/confirm")
    public String confirmInterviewing(@PathVariable int id,
                                      RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationService.findById(id);
            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn ứng tuyển");
                return "redirect:/admin/applications";
            }

            application.setProgress(Progress.INTERVIEWING);

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận chuyển sang giai đoạn phỏng vấn");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/applications/" + id;
    }

    private ApplicationDto convertToDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setRecruitmentPositionId(application.getRecruitmentPosition().getId());
        dto.setCvUrl(application.getCvUrl());
        dto.setProgress(application.getProgress());
        dto.setCreatedAt(application.getCreatedAt());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        if (application.getCreatedAt() != null) {
            dto.setCreatedAtFormatted(application.getCreatedAt().format(formatter));
        } else {
            dto.setCreatedAtFormatted("N/A");
        }

        if (application.getInterviewDate() != null) {
            dto.setInterviewDateFormatted(application.getInterviewDate().format(formatter));
        }

        if (application.getConfirmInterviewDate() != null) {
            dto.setConfirmInterviewDateFormatted(application.getConfirmInterviewDate().format(formatter));
        }

        if (application.getRejectedAt() != null) {
            dto.setRejectedAtFormatted(application.getRejectedAt().format(formatter));
        }

        if (application.getDestroyAt() != null) {
            dto.setDestroyAtFormatted(application.getDestroyAt().format(formatter));
        }

        dto.setDestroyAt(application.getDestroyAt());
        dto.setDestroyReason(application.getDestroyReason());
        dto.setInterviewDate(application.getInterviewDate());
        dto.setResultInterview(application.getResultInterview());
        dto.setConfirmInterviewDate(application.getConfirmInterviewDate());
        dto.setConfirmInterviewDateReason(application.getConfirmInterviewDateReason());
        dto.setRejectedAt(application.getRejectedAt());
        dto.setRejectedReason(application.getRejectedReason());

        dto.setCandidateName(application.getCandidate().getName());
        dto.setCandidateEmail(application.getCandidate().getEmail());
        dto.setPositionTitle(application.getRecruitmentPosition().getName());
        dto.setDepartmentName(application.getRecruitmentPosition().getDescription());

        return dto;
    }

    // Base method cho tất cả admin controllers
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
