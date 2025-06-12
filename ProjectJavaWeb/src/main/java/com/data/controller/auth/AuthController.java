package com.data.controller.auth;

import com.data.dto.AccountDto;
import com.data.dto.CandidateDto;
import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.service.account.AccountService;
import com.data.service.candidate.CandidateService;
import com.data.utils.EncryptUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final CandidateService candidateService;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("accountDto", new AccountDto());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("accountDto") @Valid AccountDto accountDto,
                               BindingResult bindingResult,
                               Model model,
                               HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        boolean success = accountService.login(accountDto.getEmail(), accountDto.getPassword());
        System.out.println("Đăng nhập thành công: " + success);

        if (success) {
            Account account = accountService.findAccountByEmail(accountDto.getEmail());

            if (account == null) {
                model.addAttribute("error", "Không tìm thấy tài khoản");
                return "login";
            }
            String encryptedId = EncryptUtil.encrypt(String.valueOf(account.getAccountId()));

            Cookie idCookie = new Cookie("aid", encryptedId);
            idCookie.setMaxAge(7 * 24 * 60 * 60);
            idCookie.setPath("/");
            idCookie.setHttpOnly(true);
            response.addCookie(idCookie);

            String role = String.valueOf(account.getRole());
            if ("ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else if ("CANDIDATE".equals(role)) {
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Vai trò không hợp lệ");
                return "login";
            }
        } else {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie idCookie = new Cookie("aid", null);
        idCookie.setMaxAge(0);
        idCookie.setPath("/");
        response.addCookie(idCookie);

        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("accountDto", new AccountDto());
        return "candidate/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("accountDto") @Valid AccountDto accountDto,
                                  BindingResult bindingResult,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            return "candidate/register";
        } else if (accountService.checkEmail(accountDto.getEmail())) {
            model.addAttribute("error", "Email đã tồn tại");
            return "candidate/register";
        } else if (candidateService.checkPhoneNumber(accountDto.getCandidateDto().getPhone())) {
            model.addAttribute("error", "Số điện thoại đã tồn tại");
            return "candidate/register";
        }

        try {
            CandidateDto candidateDto = accountDto.getCandidateDto();

            Candidate candidate = new Candidate();
            candidate.setId(candidateDto.getId());
            candidate.setDescription(candidateDto.getDescription());
            candidate.setDob(candidateDto.getDob());
            candidate.setGender(candidateDto.getGender());
            candidate.setExperience(candidateDto.getExperience());
            candidate.setName(candidateDto.getName());
            candidate.setPhone(candidateDto.getPhone());

            boolean candidateSaved = candidateService.register(candidate);
            if (!candidateSaved) {
                model.addAttribute("error", "Đăng ký thất bại ở phần ứng viên");
                return "candidate/register";
            }

            Account account = new Account();
            account.setAccountId(accountDto.getAccountId());
            account.setCandidate(candidate);
            account.setEmail(accountDto.getEmail());
            account.setPassword(accountDto.getPassword());
            account.setStatus(accountDto.getStatus());

            boolean accountSaved = accountService.register(account);

            if (!accountSaved) {
                model.addAttribute("error", "Đăng ký thất bại ở phần tài khoản");
                return "candidate/register";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "candidate/register";
        }

        return "redirect:/login";
    }
}
