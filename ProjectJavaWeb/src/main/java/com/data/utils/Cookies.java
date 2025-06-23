package com.data.utils;

import com.data.entity.Account;
import com.data.entity.enums.Status;
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

    public void createUserCookie(HttpServletResponse response, Account account) {
        try {
            // Thêm timestamp để kiểm tra expiry
            long timestamp = System.currentTimeMillis();
            String userInfo = account.getAccountId() + ":" + account.getEmail() + ":" + account.getRole() + ":" + timestamp;
            String encodedUserInfo = Base64.getEncoder().encodeToString(userInfo.getBytes());

            Cookie userCookie = new Cookie("currentUser", encodedUserInfo);
            userCookie.setMaxAge(10 * 24 * 60 * 60); // 10 ngày
            userCookie.setPath("/");
            userCookie.setHttpOnly(true);
            userCookie.setSecure(false);

            response.addCookie(userCookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Account getUserFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("currentUser".equals(cookie.getName())) {
                    try {
                        String decodedUserInfo = new String(Base64.getDecoder().decode(cookie.getValue()));
                        String[] userParts = decodedUserInfo.split(":");

                        if (userParts.length >= 4) {
                            int userId = Integer.parseInt(userParts[0]);
                            long timestamp = Long.parseLong(userParts[3]);

                            // Kiểm tra cookie có hết hạn chưa (10 ngày)
                            long currentTime = System.currentTimeMillis();
                            long maxAge = 10 * 24 * 60 * 60 * 1000L; // 10 ngày

                            if (currentTime - timestamp > maxAge) {
                                return null; // Cookie hết hạn
                            }

                            Account account = accountService.findById(userId);
                            // Kiểm tra account vẫn active
                            if (account != null && account.getStatus() == Status.ACTIVE) {
                                return account;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public void clearUserCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("currentUser", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
