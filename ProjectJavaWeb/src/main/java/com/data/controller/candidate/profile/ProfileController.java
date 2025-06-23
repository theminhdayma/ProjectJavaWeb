package com.data.controller.candidate.profile;

import com.data.dto.CandidateDto;
import com.data.dto.ChangePasswordDto;
import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.entity.Technology;
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
import java.util.ArrayList;
import java.util.List;

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
        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        // Lấy thông tin candidate mới nhất từ database với technologies
        Account account = accountService.findByEmail(currentUser.getEmail());
        if (account == null || account.getCandidate() == null) {
            return "redirect:/login";
        }

        Candidate candidate = account.getCandidate();
        CandidateDto candidateDto = convertToDto(candidate);

        // Đảm bảo technologies được set vào DTO
        candidateDto.setTechnologies(candidate.getTechnologies());

        // Tạo danh sách ID của các công nghệ đã chọn để sử dụng trong form
        List<Integer> selectedTechIds = new ArrayList<>();
        if (candidate.getTechnologies() != null) {
            for (Technology tech : candidate.getTechnologies()) {
                selectedTechIds.add(tech.getId());
            }
        }
        candidateDto.setTechnologyIds(selectedTechIds);

        model.addAttribute("candidateDto", candidateDto);
        model.addAttribute("technologies", technologyService.findAllTechnologies());
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("currentUser", account);

        return "candidate/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@Valid @ModelAttribute CandidateDto candidateDto,
                                BindingResult result,
                                @RequestParam(required = false) List<Integer> technologyIds,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        Account currentUser = getCurrentUserFromCookie(request);

        if (currentUser == null || currentUser.getCandidate() == null) {
            return "redirect:/login";
        }

        // Kiểm tra validation errors
        if (result.hasErrors()) {
            Account account = accountService.findByEmail(currentUser.getEmail());
            candidateDto.setTechnologies(account.getCandidate().getTechnologies());
            model.addAttribute("candidateDto", candidateDto);
            model.addAttribute("technologies", technologyService.findAllTechnologies());
            model.addAttribute("changePasswordDto", new ChangePasswordDto());
            model.addAttribute("currentUser", account);
            model.addAttribute("showEditModal", true);
            return "candidate/profile";
        }

        try {
            // Lấy thông tin candidate mới nhất từ database
            Account account = accountService.findByEmail(currentUser.getEmail());
            Candidate candidate = account.getCandidate();

            // Cập nhật thông tin cá nhân
            candidate.setName(candidateDto.getName());
            candidate.setPhone(candidateDto.getPhone());
            candidate.setExperience(candidateDto.getExperience());
            candidate.setGender(candidateDto.getGender());
            candidate.setDescription(candidateDto.getDescription());
            candidate.setDob(candidateDto.getDob());

            // Cập nhật công nghệ
            List<Technology> selectedTechnologies = new ArrayList<>();
            if (technologyIds != null && !technologyIds.isEmpty()) {
                for (Integer techId : technologyIds) {
                    Technology tech = technologyService.findTechnologyById(techId);
                    if (tech != null) {
                        selectedTechnologies.add(tech);
                    }
                }
            }
            candidate.setTechnologies(selectedTechnologies);

            boolean success = candidateService.updateCandidate(candidate);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật thông tin và công nghệ thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Có lỗi xảy ra khi cập nhật!");
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

        // Set technologies
        dto.setTechnologies(candidate.getTechnologies());

        // Set technology IDs cho form checkbox
        List<Integer> techIds = new ArrayList<>();
        if (candidate.getTechnologies() != null) {
            for (Technology tech : candidate.getTechnologies()) {
                techIds.add(tech.getId());
            }
        }
        dto.setTechnologyIds(techIds);

        return dto;
    }

}
