package com.binaryigor.main.day.app;

import com.binaryigor.main._common.app.HTMX;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class DayController {

    @GetMapping("/day")
    String dayPage(Model model) {
        model.addAttribute("date", LocalDate.now().toString());
        return HTMX.fragmentOrFullPage(model, "day/day");
    }
}
