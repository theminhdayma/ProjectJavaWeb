package com.data.controller.admin.candidate;

import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.entity.enums.Role;
import com.data.service.account.AccountService;
import com.data.service.candidate.CandidateService;
import com.data.service.technology.TechnologyService;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.List;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/candidate")
public class CandidateController {

    private final CandidateService candidateService;
    private final AccountService accountService;
    private final TechnologyService technologyService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Cookies cookieUtils;

    @GetMapping
    public String listCandidates(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) Integer technologyId,
            HttpServletRequest request,
            Model model) {

        Account currentUser = getCurrentUser(request);
        if (currentUser == null || !Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // Sử dụng phương thức gộp
        List<Candidate> candidates = candidateService.getFilteredCandidates(
                search, gender, age, experience, technologyId, page, size);
        long totalItems = candidateService.countFilteredCandidates(
                search, gender, age, experience, technologyId);

        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Thêm tất cả các thuộc tính vào model
        model.addAttribute("candidates", candidates);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);

        // Giữ trạng thái form lọc
        model.addAttribute("search", search);
        model.addAttribute("gender", gender);
        model.addAttribute("age", age);
        model.addAttribute("experience", experience);
        model.addAttribute("technologyId", technologyId);

        // Lấy danh sách technologies cho dropdown
        model.addAttribute("allTechnologies", technologyService.findAllTechnologies());
        model.addAttribute("currentUser", currentUser); // Thêm currentUser cho view

        return "admin/candidate/candidateManager";
    }


    @GetMapping("/deactivate/{id}")
    public String deactivateCandidate(@PathVariable int id,
                                      RedirectAttributes redirectAttributes) {
        try {
            boolean success = accountService.lockCandidate(id);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã khóa tài khoản ứng viên thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể khóa tài khoản ứng viên!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/candidate";
    }

    @GetMapping("/activate/{id}")
    public String activateCandidate(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            boolean success = accountService.unlockCandidate(id);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đã mở khóa tài khoản ứng viên thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể mở khóa tài khoản ứng viên!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi mở khóa tài khoản: " + e.getMessage());
        }
        return "redirect:/admin/candidate";
    }

    @PostMapping("/reset-password/{id}")
    @ResponseBody
    public String resetPassword(@PathVariable int id) {
        try {
            Account account = accountService.findAccountByCandidateId(id);
            if (account == null) {
                return "error:Không tìm thấy tài khoản ứng viên!";
            }

            String newPassword = generateSecureRandomPassword();
            boolean success = accountService.resetPassword(account.getEmail(), bCryptPasswordEncoder.encode(newPassword));

            if (success) {
                return "success:" + newPassword;
            } else {
                return "error:Không thể reset mật khẩu!";
            }
        } catch (Exception e) {
            return "error:Có lỗi xảy ra khi reset mật khẩu: " + e.getMessage();
        }
    }

    private String generateSecureRandomPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // Đảm bảo có ít nhất 1 ký tự từ mỗi loại
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // Thêm 4 ký tự ngẫu nhiên nữa
        for (int i = 4; i < 8; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // Trộn lại thứ tự
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);

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
