package com.data.controller.candidate;

import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.service.account.AccountService;
import com.data.service.candidate.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping
    public String listCandidates(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            Model model) {

        List<Candidate> candidates;
        long totalItems;

        if (search != null && !search.trim().isEmpty()) {
            candidates = candidateService.getCandidatesByName(search.trim(), page, size);
            totalItems = candidateService.countCandidatesByName(search.trim());
            model.addAttribute("search", search);
        } else {
            candidates = candidateService.getCandidates(page, size);
            totalItems = candidateService.countAllCandidates();
        }

        int totalPages = (int) Math.ceil((double) totalItems / size);

        model.addAttribute("candidates", candidates);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size); // Thêm pageSize vào model

        return "admin/candidate/candidateManager";
    }

    // Các phương thức khác giữ nguyên...
    @GetMapping("/deactivate/{id}")
    public String deactivateCandidate(@PathVariable int id, RedirectAttributes redirectAttributes) {
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
            boolean success = accountService.resetPassword(account.getEmail(), newPassword);

            if (success) {
                return "success:" + newPassword;
            } else {
                return "error:Không thể reset mật khẩu!";
            }
        } catch (Exception e) {
            return "error:Có lỗi xảy ra khi reset mật khẩu: " + e.getMessage();
        }
    }

    @GetMapping("/detail/{id}")
    public String viewCandidateDetail(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Candidate candidate = candidateService.getCandidateById(id);
            if (candidate == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy ứng viên!");
                return "redirect:/admin/candidate";
            }

            model.addAttribute("candidate", candidate);
            return "admin/candidate/candidateDetail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tải thông tin ứng viên: " + e.getMessage());
            return "redirect:/admin/candidate";
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
}
