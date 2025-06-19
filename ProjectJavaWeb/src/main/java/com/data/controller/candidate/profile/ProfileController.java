package com.data.controller.candidate.profile;

import com.data.dto.CandidateDto;
import com.data.dto.ChangePasswordDto;
import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.service.account.AccountService;
import com.data.service.candidate.CandidateService;
import com.data.service.technology.TechnologyService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/candidate/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final CandidateService candidateService;
    private final TechnologyService technologyService;
    private final AccountService accountService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Cookies cookieUtils;

    @GetMapping
    public String showProfile(Model model, HttpServletRequest request) {
        // Lấy thông tin user từ cookie thay vì session
        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        // Lấy thông tin candidate mới nhất từ database
        Account account = accountService.findByEmail(currentUser.getEmail());
        if (account == null || account.getCandidate() == null) {
            return "redirect:/login";
        }

        CandidateDto candidateDto = convertToDto(account.getCandidate());
        model.addAttribute("candidateDto", candidateDto);
        model.addAttribute("technologies", technologyService.findAllTechnologies());
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("currentUser", account); // Thêm currentUser cho view

        return "candidate/profile";
    }

    @PostMapping("/update-info")
    public String updateInfo(@Valid @ModelAttribute CandidateDto candidateDto,
                             BindingResult result,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("technologies", technologyService.findAllTechnologies());
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
            model.addAttribute("currentUser", currentUser);
            return "candidate/profile";
        }

        // Lấy thông tin candidate mới nhất từ database
        Account account = accountService.findByEmail(currentUser.getEmail());
        if (account == null || account.getCandidate() == null) {
            return "redirect:/login";
        }

        try {
            // Cập nhật thông tin candidate
            Candidate candidate = account.getCandidate();
            candidate.setName(candidateDto.getName());
            candidate.setPhone(candidateDto.getPhone());
            candidate.setExperience(candidateDto.getExperience());
            candidate.setGender(candidateDto.getGender());
            candidate.setDescription(candidateDto.getDescription());
            candidate.setDob(candidateDto.getDob());

            boolean success = candidateService.updateCandidate(candidate);

            if (success) {
                // Cập nhật lại cookie với thông tin mới
                account.setCandidate(candidate);
                cookieUtils.createUserCookie(response, account);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật thông tin thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Có lỗi xảy ra khi cập nhật thông tin!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute ChangePasswordDto changePasswordDto,
                                 BindingResult result,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        // Lấy thông tin user từ cookie
        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy thông tin account mới nhất từ database
        Account account = accountService.findByEmail(currentUser.getEmail());

        if (account == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản!");
            return "redirect:/candidate/profile";
        }

        // Kiểm tra validation errors
        if (result.hasErrors()) {
            CandidateDto candidateDto = convertToDto(account.getCandidate());
            model.addAttribute("candidateDto", candidateDto);
            model.addAttribute("technologies", technologyService.findAllTechnologies());
            model.addAttribute("changePasswordDto", changePasswordDto);
            model.addAttribute("showPasswordModal", true);
            model.addAttribute("currentUser", account);
            return "candidate/profile";
        }

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), account.getPassword())) {
            CandidateDto candidateDto = convertToDto(account.getCandidate());
            model.addAttribute("candidateDto", candidateDto);
            model.addAttribute("technologies", technologyService.findAllTechnologies());
            model.addAttribute("changePasswordDto", changePasswordDto);
            model.addAttribute("showPasswordModal", true);
            model.addAttribute("passwordError", "Mật khẩu cũ không đúng!");
            model.addAttribute("currentUser", account);
            return "candidate/profile";
        }

        // Kiểm tra mật khẩu mới và xác nhận
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            CandidateDto candidateDto = convertToDto(account.getCandidate());
            model.addAttribute("candidateDto", candidateDto);
            model.addAttribute("technologies", technologyService.findAllTechnologies());
            model.addAttribute("changePasswordDto", changePasswordDto);
            model.addAttribute("showPasswordModal", true);
            model.addAttribute("passwordError", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
            model.addAttribute("currentUser", account);
            return "candidate/profile";
        }

        try {
            // Đổi mật khẩu
            boolean success = accountService.changePassword(account, changePasswordDto.getNewPassword());

            if (success) {
                // Cập nhật lại cookie với thông tin mới (mật khẩu đã thay đổi)
                account.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
                cookieUtils.createUserCookie(response, account);
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi đổi mật khẩu!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/candidate/profile";
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

    // Method helper để validate permissions
    private boolean hasPermissionToAccessProfile(Account user) {
        return user != null && user.getCandidate() != null;
    }

    private CandidateDto convertToDto(Candidate candidate) {
        CandidateDto dto = new CandidateDto();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setPhone(candidate.getPhone());
        dto.setExperience(candidate.getExperience());
        dto.setGender(candidate.getGender());
        dto.setDescription(candidate.getDescription());
        dto.setDob(candidate.getDob());
        return dto;
    }
}
