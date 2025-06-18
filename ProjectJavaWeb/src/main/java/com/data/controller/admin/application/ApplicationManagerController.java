package com.data.controller.admin.application;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/application")
public class ApplicationManagerController {
    @GetMapping
    public String application() {
        return "admin/application/applicationManager";
    }
}
