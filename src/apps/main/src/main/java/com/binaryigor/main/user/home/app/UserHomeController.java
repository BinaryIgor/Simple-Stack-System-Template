package com.binaryigor.main.user.home.app;

import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main._contract.AuthUserClient;
import com.binaryigor.main.user.home.core.GetCurrentUserDataHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserHomeController {

    private final GetCurrentUserDataHandler getCurrentUserDataHandler;
    private final AuthUserClient authUserClient;

    public UserHomeController(GetCurrentUserDataHandler getCurrentUserDataHandler,
                              AuthUserClient authUserClient) {
        this.getCurrentUserDataHandler = getCurrentUserDataHandler;
        this.authUserClient = authUserClient;
    }

    @GetMapping("/home")
    String home(Model model) {
        return homePage(model);
    }

    private String homePage(Model model) {
        var user = getCurrentUserDataHandler.handle(authUserClient.currentId());
        model.addAttribute("user", user);
        return HTMX.fragmentOrFullPage(model, "user/home");
    }

    @GetMapping("/")
    String rootHome(Model model) {
        return homePage(model);
    }
}
