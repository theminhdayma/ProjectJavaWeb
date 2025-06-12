package com.data.controller.statistical;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatisticalController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard/dashboard";
    }
}
