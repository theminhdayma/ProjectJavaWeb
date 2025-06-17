package com.data.controller.candidate;

import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.service.account.AccountService;
import com.data.service.candidate.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static com.data.utils.PaginationUtil.DEFAULT_PAGE;
import static com.data.utils.PaginationUtil.DEFAULT_SIZE;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/candidate")
public class CandidateController {

    private final CandidateService candidateService;
    private final AccountService accountService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping
    public String listCandidates(
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Integer experience,
            Model model) {

        // Sử dụng phương thức lọc tổng hợp duy nhất cho mọi trường hợp (không có technology)
        List<Candidate> candidates = candidateService.getFilteredCandidates(
                search, gender, age, experience, page, size);
        long totalItems = candidateService.countFilteredCandidates(
                search, gender, age, experience);

        int totalPages = (int) Math.ceil((double) totalItems / size);

        // Thêm tất cả các thuộc tính vào model
        model.addAttribute("candidates", candidates);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);

        // Giữ trạng thái form lọc (không có technology)
        model.addAttribute("search", search);
        model.addAttribute("gender", gender);
        model.addAttribute("age", age);
        model.addAttribute("experience", experience);

        return "admin/candidate/candidateManager";
    }

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

            // Tạo mật khẩu mới ngẫu nhiên
            String newPassword = "password123";
            String encodedPassword = bCryptPasswordEncoder.encode(newPassword);
            boolean success = accountService.resetPassword(account.getEmail(), encodedPassword);

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
}
