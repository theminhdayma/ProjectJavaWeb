package com.data.controller.admin;

import com.data.entity.Account;
import com.data.entity.enums.Role;
import com.data.entity.enums.Status;
import com.data.utils.Cookies;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final Cookies cookieUtils;

    @GetMapping
    public String dashboard(HttpServletRequest request, Model model) {
        Account currentUser = cookieUtils.getUserFromCookie(request);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra role ADMIN
        if (!Role.ADMIN.equals(currentUser.getRole())) {
            return "redirect:/login?error=access_denied";
        }

        // Kiểm tra trạng thái tài khoản
        if (currentUser.getStatus() != Status.ACTIVE) {
            return "redirect:/login?error=account_locked";
        }

        // Truyền currentUser cho view
        model.addAttribute("currentUser", currentUser);

        // SỬA: Return view name thay vì redirect
        return "admin/dashboard/dashboard";
    }
}
