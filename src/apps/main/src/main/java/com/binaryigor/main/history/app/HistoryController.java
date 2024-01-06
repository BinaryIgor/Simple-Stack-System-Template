package com.binaryigor.main.history.app;

import com.binaryigor.main._common.app.HTMX;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/history")
public class HistoryController {

    @GetMapping
    String historyPage(Model model) {
        return HTMX.fragmentOrFullPage(model, "history/history");
    }
}
