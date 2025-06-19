package com.data.utils;

import com.data.entity.Account;
import com.data.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class Cookies {

    private final AccountService accountService;

    // Thay đổi từ private thành public static để có thể gọi từ Controller
    public static void createUserCookie(HttpServletResponse response, Account account) {
        try {
            // Tạo thông tin user để lưu vào cookie (có thể mã hóa)
            String userInfo = account.getAccountId() + ":" + account.getEmail() + ":" + account.getRole();

            // Mã hóa thông tin (tùy chọn)
            String encodedUserInfo = Base64.getEncoder().encodeToString(userInfo.getBytes());

            // Tạo cookie với thời hạn 10 ngày
            Cookie userCookie = new Cookie("currentUser", encodedUserInfo);
            userCookie.setMaxAge(10 * 24 * 60 * 60); // 10 ngày tính bằng giây
            userCookie.setPath("/"); // Cookie có hiệu lực trên toàn bộ ứng dụng
            userCookie.setHttpOnly(true); // Bảo mật: không thể truy cập qua JavaScript
            userCookie.setSecure(false); // Đặt true nếu sử dụng HTTPS

            response.addCookie(userCookie);

        } catch (Exception e) {
            // Log lỗi nếu cần
            e.printStackTrace();
        }
    }

    public Account getUserFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("currentUser".equals(cookie.getName())) {
                    try {
                        // Giải mã thông tin từ cookie
                        String decodedUserInfo = new String(Base64.getDecoder().decode(cookie.getValue()));
                        String[] userParts = decodedUserInfo.split(":");

                        if (userParts.length >= 3) {
                            int userId = Integer.parseInt(userParts[0]);
                            return accountService.findById(userId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    // Thêm method để xóa cookie khi logout
    public static void clearUserCookie(HttpServletResponse response) {
        Cookie userCookie = new Cookie("currentUser", null);
        userCookie.setMaxAge(0); // Xóa cookie
        userCookie.setPath("/");
        response.addCookie(userCookie);
    }

    // Method kiểm tra cookie có tồn tại không
    public static boolean hasRememberMeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("currentUser".equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
