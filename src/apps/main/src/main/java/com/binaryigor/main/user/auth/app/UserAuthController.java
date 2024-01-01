package com.binaryigor.main.user.auth.app;

import com.binaryigor.main._common.app.Cookies;
import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main._common.core.exception.AppException;
import com.binaryigor.main.user.auth.core.handler.SignInFirstStepHandler;
import com.binaryigor.main.user.auth.core.model.SignInFirstStepCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user-auth")
public class UserAuthController {

    private final SignInFirstStepHandler signInFirstStepHandler;
    private final Cookies cookies;

    public UserAuthController(SignInFirstStepHandler signInFirstStepHandler,
                              Cookies cookies) {
        this.signInFirstStepHandler = signInFirstStepHandler;
        this.cookies = cookies;
    }

    @GetMapping("/sign-in")
    public String signInPage(Model model) {
        return HTMX.fragmentOrFullPage(model, "user/sign-in");
    }

    @PostMapping("/sign-in")
    public String signIn(Model model,
                         @ModelAttribute SignInFirstStepCommand command,
                         HttpServletResponse response) {
        System.out.println("Signing-in: " + command);

        var result = signInFirstStepHandler.handle(command);
        if (result.secondFactor()) {
            throw new AppException("Not implemented yet!");
        }

        var token = result.user().token();

        response.addCookie(cookies.token(token.value(), token.expiresAt()));

        model.addAttribute("user", result.user().data());

        HTMX.addClientReplaceUrlHeader(response, "/home");

        return HTMX.fragmentOrFullPage(model, "user/home");
    }

    @PostMapping("/sign-out")
    public String signOut(Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        cookies.tokenValue(request.getCookies())
                .ifPresent(t -> {
                    response.addCookie(cookies.token(t, 0));
                });

        HTMX.addClientReplaceUrlHeader(response, "/user/sign-in");

        return signInPage(model);
    }
}
