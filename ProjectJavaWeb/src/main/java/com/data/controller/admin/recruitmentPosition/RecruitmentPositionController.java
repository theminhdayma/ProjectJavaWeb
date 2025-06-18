package com.data.controller.admin.recruitmentPosition;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/recruitment-position")
public class RecruitmentPositionController {
    @GetMapping
    public String recruitmentPositionManager() {
        return "admin/recruitmentPosition/recruitmentPositionManager";
    }
}
