package com.data.controller.candidate.home;

import com.data.entity.Account;
import com.data.entity.Application;
import com.data.entity.RecruitmentPosition;
import com.data.entity.enums.Progress;
import com.data.service.CloudinaryService;
import com.data.service.account.AccountService;
import com.data.service.application.ApplicationService;
import com.data.service.recruitmentPosition.RecruitmentPositionService;
import com.data.service.technology.TechnologyService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/candidate/home")
public class HomeController {

    private final ApplicationService applicationService;
    private final AccountService accountService;
    private final TechnologyService technologyService;
    private final RecruitmentPositionService recruitmentPositionService;
    private final CloudinaryService cloudinaryService;
    private final Cookies cookieUtils;

    @GetMapping
    public String home(
            HttpServletRequest request,
            Model model,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer technologyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        Account account = accountService.findByEmail(currentUser.getEmail());
        if (account == null || account.getCandidate() == null) {
            return "redirect:/login";
        }

        // Debug logging
        System.out.println("Search parameters:");
        System.out.println("Keyword: " + keyword);
        System.out.println("Technology ID: " + technologyId);

        List<RecruitmentPosition> positions;
        long totalPositions;

        if (technologyId != null && technologyId > 0) {
            positions = recruitmentPositionService.findAllByTechnology(keyword, technologyId, page, size);
            totalPositions = recruitmentPositionService.countAllByTechnology(keyword, technologyId);
        } else {
            positions = recruitmentPositionService.findAll(keyword, page, size);
            totalPositions = recruitmentPositionService.countAll(keyword);
        }

        int totalPages = (int) Math.ceil((double) totalPositions / size);

        model.addAttribute("currentUser", account);
        model.addAttribute("allTechnologies", technologyService.findAllTechnologies());
        model.addAttribute("positions", positions);
        model.addAttribute("keyword", keyword);
        model.addAttribute("technologyId", technologyId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalPositions", totalPositions);
        model.addAttribute("size", size);

        return "candidate/home";
    }

    @GetMapping("/recruitmentDetail/{id}")
    public String recruitmentDetail(
            @PathVariable("id") int positionId,
            HttpServletRequest request,
            Model model
    ) {
        try {
            Account currentUser = getCurrentUserFromCookie(request);

            if (currentUser == null || currentUser.getCandidate() == null) {
                return "redirect:/login";
            }

            // Lấy thông tin chi tiết vị trí tuyển dụng
            RecruitmentPosition position = recruitmentPositionService.findById(positionId);

            if (position == null) {
                return "redirect:/candidate/home";
            }

            // Lấy thông tin candidate mới nhất từ database
            Account account = accountService.findByEmail(currentUser.getEmail());

            // Lấy 3 vị trí ngẫu nhiên khác
            List<RecruitmentPosition> randomPositions = recruitmentPositionService
                    .findRandomPositionsExcluding(positionId, 2);

            model.addAttribute("position", position);
            model.addAttribute("currentUser", account);
            model.addAttribute("randomPositions", randomPositions);

            return "candidate/recruitmentDetail";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/candidate/home?error=true";
        }
    }

    @PostMapping("/apply")
    public String applyForPosition(
            @RequestParam("positionId") int positionId,
            @RequestParam("cvFile") MultipartFile cvFile,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Account currentUser = getCurrentUserFromCookie(request);

            if (currentUser == null || currentUser.getCandidate() == null) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để ứng tuyển");
                return "redirect:/login";
            }

            // Validate file
            if (cvFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file CV");
                return "redirect:/candidate/home/recruitmentDetail/" + positionId;
            }

            // Check file type
            String contentType = cvFile.getContentType();
            if (!isValidFileType(contentType)) {
                redirectAttributes.addFlashAttribute("error", "Chỉ chấp nhận file PDF, DOC, DOCX");
                return "redirect:/candidate/home/recruitmentDetail/" + positionId;
            }

            // Check file size (5MB)
            if (cvFile.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "File không được vượt quá 5MB");
                return "redirect:/candidate/home/recruitmentDetail/" + positionId;
            }

            // Get position
            RecruitmentPosition position = recruitmentPositionService.findById(positionId);
            if (position == null) {
                redirectAttributes.addFlashAttribute("error", "Vị trí tuyển dụng không tồn tại");
                return "redirect:/candidate/home";
            }

            // Upload CV to Cloudinary
            String cvUrl = cloudinaryService.uploadFile(cvFile, "cv_files");

            // Create application
            Application application = new Application();
            application.setCandidate(currentUser.getCandidate());
            application.setRecruitmentPosition(position);
            application.setCvUrl(cvUrl);
            application.setProgress(Progress.PENDING);
            application.setCreatedAt(LocalDateTime.now());

            // Save application
            Application savedApplication = applicationService.save(application);

            if (savedApplication != null) {
                // Success message
                redirectAttributes.addFlashAttribute("success", "Ứng tuyển thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất.");
                redirectAttributes.addFlashAttribute("applicationId", savedApplication.getId());

                // Redirect to application list or position detail
                return "redirect:/candidate/home/recruitmentDetail/" + positionId + "?applied=true";
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi lưu đơn ứng tuyển");
                return "redirect:/candidate/home/recruitmentDetail/" + positionId;
            }

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải file lên: " + e.getMessage());
            return "redirect:/candidate/home/recruitmentDetail/" + positionId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/candidate/home/recruitmentDetail/" + positionId;
        }
    }

    private boolean isValidFileType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }



    private Account getCurrentUserFromCookie(HttpServletRequest request) {
        try {
            return cookieUtils.getUserFromCookie(request);
        } catch (Exception e) {
            System.err.println("Error getting user from cookie: " + e.getMessage());
            return null;
        }
    }
}
