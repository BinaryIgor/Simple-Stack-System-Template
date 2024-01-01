package com.binaryigor.main.user.auth.app;

import com.binaryigor.main._common.app.Cookies;
import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main._common.core.exception.AppException;
import com.binaryigor.main.user.auth.core.handler.ActivateUserHandler;
import com.binaryigor.main.user.auth.core.handler.SignInFirstStepHandler;
import com.binaryigor.main.user.auth.core.handler.SignUpHandler;
import com.binaryigor.main.user.auth.core.model.SignInFirstStepCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserAuthController {

    private final SignUpHandler signUpHandler;
    private final SignInFirstStepHandler signInFirstStepHandler;
    private final ActivateUserHandler activateUserHandler;
    private final Cookies cookies;

    public UserAuthController(SignUpHandler signUpHandler,
                              SignInFirstStepHandler signInFirstStepHandler,
                              ActivateUserHandler activateUserHandler,
                              Cookies cookies) {
        this.signUpHandler = signUpHandler;
        this.signInFirstStepHandler = signInFirstStepHandler;
        this.activateUserHandler = activateUserHandler;
        this.cookies = cookies;
    }

    @GetMapping("/sign-up")
    public String signUpPage(Model model) {
        return HTMX.fragmentOrFullPage(model, userTemplate("sign-up"));
    }

    @PostMapping("/sign-up")
    public String signUp(Model model,
                         @ModelAttribute SignUpRequest request,
                         HttpServletResponse response) {
        signUpHandler.handle(request.toCommand());
        HTMX.addClientReplaceUrlHeader(response, "/after-sign-up");
        return afterSignUpPage(model);
    }

    @GetMapping("/after-sign-up")
    public String afterSignUpPage(Model model) {
        return HTMX.fragmentOrFullPage(model, userTemplate("after-sign-up"));
    }

    @GetMapping("/activate-account")
    public ModelAndView activateAccount(@RequestParam String token) {
        activateUserHandler.handle(token);
        return new ModelAndView("redirect:"+ userTemplate("sign-in"));
    }

    @GetMapping("/sign-in")
    public String signInPage(Model model) {
        return HTMX.fragmentOrFullPage(model, userTemplate("sign-in"));
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
        return HTMX.fragmentOrFullPage(model, userTemplate("home"));
    }

    @PostMapping("/sign-out")
    public String signOut(Model model,
                          HttpServletRequest request,
                          HttpServletResponse response) {
        cookies.tokenValue(request.getCookies())
                .ifPresent(t -> {
                    response.addCookie(cookies.token(t, 0));
                });

        HTMX.addClientReplaceUrlHeader(response, "/sign-in");

        return signInPage(model);
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(Model model) {
        return HTMX.fragmentOrFullPage(model, userTemplate("reset-password"));
    }

    @GetMapping("/after-reset-password")
    public String afterResetPasswordPage(Model model) {
        return HTMX.fragmentOrFullPage(model, userTemplate("after-reset-password"));
    }

    private String userTemplate(String template) {
        return "/user/" + template;
    }
}
