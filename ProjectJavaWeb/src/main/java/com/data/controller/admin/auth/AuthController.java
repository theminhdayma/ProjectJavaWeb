package com.data.controller.admin.auth;

import com.data.dto.AuthDto;
import com.data.dto.LoginDto;
import com.data.entity.Account;
import com.data.entity.Candidate;
import com.data.entity.Technology;
import com.data.entity.enums.Role;
import com.data.entity.enums.Status;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;
    private final CandidateService candidateService;
    private final TechnologyService technologyService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Cookies cookieUtils;

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpServletRequest request, HttpSession session) {

        // Kiểm tra cookie nếu không có session
        Account userFromCookie = cookieUtils.getUserFromCookie(request);
        if (userFromCookie != null) {
            // Lưu vào session để tránh check cookie liên tục
            session.setAttribute("currentUser", userFromCookie);

            if (Role.ADMIN.equals(userFromCookie.getRole())) {
                return "redirect:/admin/dashboard";
            } else if (Role.CANDIDATE.equals(userFromCookie.getRole())) {
                return "redirect:/candidate/home";
            }
        }

        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginDto());
        }
        return "login";
    }


    @PostMapping("/login")
    public String processLogin(@ModelAttribute("loginDto") @Valid LoginDto loginDto,
                               BindingResult bindingResult,
                               Model model,
                               HttpSession session,
                               HttpServletResponse response,
                               HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng điền đầy đủ thông tin");
            return "login";
        }

        try {
            Account account = accountService.findAccountByEmail(loginDto.getEmail());

            if (account == null) {
                model.addAttribute("error", "Email không tồn tại trong hệ thống");
                return "login";
            }

            if (!passwordEncoder.matches(loginDto.getPassword(), account.getPassword())) {
                model.addAttribute("error", "Mật khẩu không đúng");
                return "login";
            }

            if (account.getStatus() != Status.ACTIVE) {
                model.addAttribute("error", "Tài khoản của bạn đã bị khóa");
                return "login";
            }

            // Lưu thông tin vào session
            session.setAttribute("currentUser", account);

            cookieUtils.createUserCookie(response, account);

            if (Role.ADMIN.equals(account.getRole())) {
                return "redirect:/admin/candidate";
            } else if (Role.CANDIDATE.equals(account.getRole())) {
                return "redirect:/candidate/home";
            } else {
                model.addAttribute("error", "Tài khoản không có quyền truy cập");
                return "login";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng nhập");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // Xóa session
        session.invalidate();

        // Xóa cookie remember-me
        cookieUtils.clearUserCookie(response);

        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("authDto")) {
            model.addAttribute("authDto", new AuthDto());
        }

        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("technologies", technologies);

        return "candidate/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute("authDto") @Valid AuthDto authDto,
                                  BindingResult bindingResult,
                                  Model model) {

        List<Technology> technologies = technologyService.findAllTechnologies();
        model.addAttribute("technologies", technologies);

        // Kiểm tra email theo thứ tự ưu tiên
        if (authDto.getEmail() == null || authDto.getEmail().trim().isEmpty()) {
            bindingResult.rejectValue("email", "email.required", "Email không được để trống");
            return "candidate/register";
        } else if (!isValidEmail(authDto.getEmail())) {
            bindingResult.rejectValue("email", "email.invalid", "Email không đúng định dạng");
            return "candidate/register";
        } else if (accountService.checkEmail(authDto.getEmail())) {
            bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại trong hệ thống");
            return "candidate/register";
        }

        // Kiểm tra password theo thứ tự ưu tiên
        if (authDto.getPassword() == null || authDto.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "password.required", "Mật khẩu không được để trống");
            return "candidate/register";
        } else if (authDto.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "password.tooShort", "Mật khẩu phải có ít nhất 6 ký tự");
            return "candidate/register";
        }

        // Kiểm tra confirm password
        if (authDto.getConfirmPassword() == null || authDto.getConfirmPassword().trim().isEmpty()) {
            bindingResult.rejectValue("confirmPassword", "confirmPassword.required", "Xác nhận mật khẩu không được để trống");
            return "candidate/register";
        } else if (authDto.getPassword() != null && !authDto.getPassword().equals(authDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu xác nhận không khớp");
            return "candidate/register";
        }

        // Kiểm tra name
        if (authDto.getName() == null || authDto.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "name.required", "Tên không được để trống");
            return "candidate/register";
        } else if (authDto.getName().trim().length() < 2) {
            bindingResult.rejectValue("name", "name.tooShort", "Tên phải có ít nhất 2 ký tự");
            return "candidate/register";
        }

        // Kiểm tra phone
        if (authDto.getPhone() == null || authDto.getPhone().trim().isEmpty()) {
            bindingResult.rejectValue("phone", "phone.required", "Số điện thoại không được để trống");
            return "candidate/register";
        } else if (!isValidPhoneNumber(authDto.getPhone())) {
            bindingResult.rejectValue("phone", "phone.invalid", "Số điện thoại không hợp lệ");
            return "candidate/register";
        } else if (candidateService.checkPhoneNumber(authDto.getPhone())) {
            bindingResult.rejectValue("phone", "phone.exists", "Số điện thoại đã được sử dụng");
            return "candidate/register";
        }

        // Kiểm tra gender
        if (authDto.getGender() == null || authDto.getGender().toString().trim().isEmpty()) {
            bindingResult.rejectValue("gender", "gender.required", "Vui lòng chọn giới tính");
            return "candidate/register";
        }

        try {
            Candidate candidate = new Candidate();
            candidate.setName(authDto.getName());
            candidate.setPhone(authDto.getPhone());
            candidate.setExperience(authDto.getExperience());
            candidate.setGender(authDto.getGender());
            candidate.setDescription(authDto.getDescription());
            candidate.setDob(authDto.getDob());

            if (authDto.getTechnologyIds() != null && !authDto.getTechnologyIds().isEmpty()) {
                List<Technology> selectedTechnologies = new ArrayList<>();
                for (Integer techId : authDto.getTechnologyIds()) {
                    Technology tech = technologyService.findTechnologyById(techId);
                    if (tech != null) {
                        selectedTechnologies.add(tech);
                    }
                }
                candidate.setTechnologies(selectedTechnologies);
            }

            Candidate savedCandidate = candidateService.register(candidate);

            Account account = new Account();
            account.setEmail(authDto.getEmail());
            account.setPassword(passwordEncoder.encode(authDto.getPassword()));
            account.setRole(Role.CANDIDATE);
            account.setStatus(Status.ACTIVE);
            account.setCandidate(savedCandidate);

            accountService.register(account);

            return "redirect:/login?success=true";

        } catch (Exception e) {
            bindingResult.rejectValue("email", "registration.error",
                    "Có lỗi xảy ra trong quá trình đăng ký: " + e.getMessage());
            return "candidate/register";
        }
    }

    // Thêm các method helper
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private boolean isValidPhoneNumber(String phone) {
        String phoneRegex = "^(\\+84|0)\\d{9,10}$";
        return phone.matches(phoneRegex);
    }
}
