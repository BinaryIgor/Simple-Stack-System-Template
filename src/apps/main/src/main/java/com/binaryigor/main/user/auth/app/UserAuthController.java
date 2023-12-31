package com.binaryigor.main.user.auth.app;

import com.binaryigor.main._commons.app.WebViews;
import com.binaryigor.main._commons.core.exception.AppException;
import com.binaryigor.main.user.auth.core.model.SignInFirstStepCommand;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user-auth")
public class UserAuthController {

    @GetMapping("/sign-in")
    public String signInPage(Model model) {
        return WebViews.fragmentOrFullPage(model, "user/sign-in");
    }

    @PostMapping("/sign-in")
    public String signIn(Model model,
                         @ModelAttribute SignInFirstStepCommand command) {
        System.out.println("Signing-in: " + command);
        throw new AppException("Not implemented yet!");
    }
}
