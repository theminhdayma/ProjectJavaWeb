package com.data.controller.candidate.application;

import com.data.dto.ApplicationDto;
import com.data.entity.Account;
import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.service.application.ApplicationService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequestMapping("/candidate/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final Cookies cookieUtils;

    @GetMapping
    public String showApplications(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            Model model,
            HttpServletRequest request) {

        // Lấy thông tin user từ cookie thay vì session
        Account currentUser = getCurrentUserFromCookie(request);
        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        // Lấy danh sách applications với phân trang
        List<Application> applications = applicationService.findAllByCandidateId(
                currentUser.getCandidate().getId(), page, size);
        long totalApplications = applicationService.countByCandidateId(
                currentUser.getCandidate().getId());

        // Convert to DTO
        List<ApplicationDto> applicationDtos = applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // Tính toán thông tin phân trang
        int totalPages = (int) Math.ceil((double) totalApplications / size);

        // Add attributes to model
        model.addAttribute("applications", applicationDtos);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("currentUser", currentUser); // Thêm user info cho view

        // Tính toán các trang hiển thị
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(totalPages - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "candidate/application";
    }

    @PostMapping("/cancel/{id}")
    public String cancelApplication(@PathVariable int id,
                                    @RequestParam String cancelReason,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {

        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);
        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        try {
            Application application = applicationService.findById(id);
            if (application == null ||
                    application.getCandidate().getId() != currentUser.getCandidate().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy đơn ứng tuyển hoặc bạn không có quyền hủy đơn này");
                return "redirect:/candidate/applications";
            }

            // Cập nhật trạng thái và lý do hủy
            application.setProgress(Progress.DESTROYED);
            application.setDestroyReason(cancelReason);
            application.setDestroyAt(LocalDateTime.now());

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã hủy đơn ứng tuyển thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra khi hủy đơn ứng tuyển: " + e.getMessage());
        }

        return "redirect:/candidate/applications";
    }

    @PostMapping("/reschedule/{id}")
    public String rescheduleApplication(@PathVariable int id,
                                        @RequestParam String rescheduleReason,
                                        @RequestParam String requestedDateTime,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {

        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);
        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        try {
            Application application = applicationService.findById(id);
            if (application == null ||
                    application.getCandidate().getId() != currentUser.getCandidate().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy đơn ứng tuyển hoặc bạn không có quyền yêu cầu đổi lịch");
                return "redirect:/candidate/applications";
            }

            // Parse datetime
            LocalDateTime requestedTime = LocalDateTime.parse(requestedDateTime);

            // Cập nhật thông tin yêu cầu đổi lịch
            application.setProgress(Progress.REQUEST_RESCHEDULE);
            application.setConfirmInterviewDateReason(rescheduleReason);
            application.setConfirmInterviewDate(requestedTime);

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi yêu cầu đổi lịch phỏng vấn thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra khi gửi yêu cầu đổi lịch: " + e.getMessage());
        }

        return "redirect:/candidate/applications";
    }

    @PostMapping("/confirm/{id}")
    public String confirmInterview(@PathVariable int id,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {

        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);
        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        try {
            Application application = applicationService.findById(id);
            if (application == null ||
                    application.getCandidate().getId() != currentUser.getCandidate().getId()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Không tìm thấy đơn ứng tuyển");
                return "redirect:/candidate/applications";
            }

            // Đổi trạng thái sang INTERVIEWING
            application.setProgress(Progress.INTERVIEWING);
            application.setConfirmInterviewDate(LocalDateTime.now());
            application.setConfirmInterviewDateReason("Đã xác nhận tham gia phỏng vấn");

            applicationService.update(application);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xác nhận tham gia phỏng vấn thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/applications";
    }

    @GetMapping("/detail")
    public String showApplicationDetail(@RequestParam int id,
                                        Model model,
                                        HttpServletRequest request) {
        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);
        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        Application application = applicationService.findById(id);
        if (application == null ||
                application.getCandidate().getId() != currentUser.getCandidate().getId()) {
            model.addAttribute("error",
                    "Application not found or you do not have permission to view it.");
            return "error";
        }

        ApplicationDto applicationDto = convertToDto(application);
        model.addAttribute("application", applicationDto);
        model.addAttribute("currentUser", currentUser);

        return "candidate/application_detail";
    }

    // Method helper để lấy user từ cookie
    private Account getCurrentUserFromCookie(HttpServletRequest request) {
        try {
            return cookieUtils.getUserFromCookie(request);
        } catch (Exception e) {
            // Log lỗi nếu cần
            System.err.println("Error getting user from cookie: " + e.getMessage());
            return null;
        }
    }

    // Method helper để validate user permissions
    private boolean hasPermissionToAccessApplication(Account user, Application application) {
        return user != null &&
                user.getCandidate() != null &&
                application != null &&
                application.getCandidate().getId() == user.getCandidate().getId();
    }

    private ApplicationDto convertToDto(Application application) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(application.getId());
        dto.setCandidateId(application.getCandidate().getId());
        dto.setRecruitmentPositionId(application.getRecruitmentPosition().getId());
        dto.setCvUrl(application.getCvUrl());
        dto.setProgress(application.getProgress());
        dto.setCreatedAt(application.getCreatedAt());

        // Format các ngày tháng
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

        // Set thông tin bổ sung từ các entity liên quan
        dto.setCandidateName(application.getCandidate().getName());
        dto.setCandidateEmail(application.getCandidate().getEmail());
        dto.setPositionTitle(application.getRecruitmentPosition().getName());
        dto.setDepartmentName(application.getRecruitmentPosition().getDescription());

        return dto;
    }
}
