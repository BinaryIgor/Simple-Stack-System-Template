package com.binaryigor.main.user.auth.app;

import com.binaryigor.main._common.app.Cookies;
import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main._common.core.exception.AppException;
import com.binaryigor.main.user.auth.core.handler.*;
import com.binaryigor.main.user.auth.core.model.SetNewPasswordCommand;
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

    private static final String SIGN_IN_TEMPLATE = userTemplate("sign-in");
    private static final String SIGN_UP_TEMPLATE = userTemplate("sign-up");
    private static final String AFTER_SIGN_UP_TEMPLATE = userTemplate("after-sign-up");
    private static final String RESET_PASSWORD_TEMPLATE = userTemplate("reset-password");
    private static final String AFTER_RESET_PASSWORD_TEMPLATE = userTemplate("after-reset-password");
    private static final String SET_NEW_PASSWORD_TEMPLATE = userTemplate("set-new-password");
    private static final String AFTER_SET_NEW_PASSWORD_TEMPLATE = userTemplate("after-set-new-password");
    private static final String HOME_TEMPLATE = userTemplate("home");
    private final SignUpHandler signUpHandler;
    private final SignInFirstStepHandler signInFirstStepHandler;
    private final ActivateUserHandler activateUserHandler;
    private final ResetUserPasswordHandler resetUserPasswordHandler;
    private final SetNewUserPasswordHandler setNewUserPasswordHandler;
    private final Cookies cookies;

    public UserAuthController(SignUpHandler signUpHandler,
                              SignInFirstStepHandler signInFirstStepHandler,
                              ActivateUserHandler activateUserHandler,
                              ResetUserPasswordHandler resetUserPasswordHandler,
                              SetNewUserPasswordHandler setNewUserPasswordHandler,
                              Cookies cookies) {
        this.signUpHandler = signUpHandler;
        this.signInFirstStepHandler = signInFirstStepHandler;
        this.activateUserHandler = activateUserHandler;
        this.resetUserPasswordHandler = resetUserPasswordHandler;
        this.setNewUserPasswordHandler = setNewUserPasswordHandler;
        this.cookies = cookies;
    }

    private static String userTemplate(String template) {
        return "/user/" + template;
    }

    @GetMapping("/sign-up")
    String signUpPage(Model model) {
        return HTMX.fragmentOrFullPage(model, SIGN_UP_TEMPLATE);
    }

    @PostMapping("/sign-up")
    String signUp(Model model,
                  @ModelAttribute SignUpRequest request,
                  HttpServletResponse response) {
        signUpHandler.handle(request.toCommand());
        HTMX.addClientReplaceUrlHeader(response, "/after-sign-up");
        return afterSignUpPage(model);
    }

    @GetMapping("/after-sign-up")
    String afterSignUpPage(Model model) {
        return HTMX.fragmentOrFullPage(model, AFTER_SIGN_UP_TEMPLATE);
    }

    @GetMapping("/activate-account")
    ModelAndView activateAccount(@RequestParam String token) {
        activateUserHandler.handle(token);
        return new ModelAndView("redirect:" + SIGN_IN_TEMPLATE);
    }

    @GetMapping("/sign-in")
    String signInPage(Model model) {
        return HTMX.fragmentOrFullPage(model, SIGN_IN_TEMPLATE);
    }

    @PostMapping("/sign-in")
    String signIn(Model model,
                  @ModelAttribute SignInFirstStepCommand command,
                  HttpServletResponse response) {
        var result = signInFirstStepHandler.handle(command);
        if (result.secondFactor()) {
            throw new AppException("Not implemented yet!");
        }

        var token = result.user().token();

        response.addCookie(cookies.token(token.value(), token.expiresAt()));

        model.addAttribute("user", result.user().data());

        HTMX.addClientReplaceUrlHeader(response, "/home");
        return HTMX.fragmentOrFullPage(model, HOME_TEMPLATE);
    }

    @PostMapping("/sign-out")
    String signOut(Model model,
                   HttpServletRequest request,
                   HttpServletResponse response) {
        cookies.tokenValue(request.getCookies())
                .ifPresent(t -> {
                    response.addCookie(cookies.token(t, 0));
                });

        HTMX.addClientReplaceUrlHeader(response, "/sign-in");

        return HTMX.fragmentOrFullPage(model, SIGN_IN_TEMPLATE);
    }

    @GetMapping("/reset-password")
    String resetPasswordPage(Model model) {
        return HTMX.fragmentOrFullPage(model, RESET_PASSWORD_TEMPLATE);
    }

    @PostMapping("/reset-password")
    String resetPassword(Model model,
                         @ModelAttribute ResetUserPasswordRequest request,
                         HttpServletResponse response) {
        resetUserPasswordHandler.handle(request.email());
        HTMX.addClientReplaceUrlHeader(response, "/after-reset-password");
        return afterResetPasswordPage(model);
    }

    @GetMapping("/after-reset-password")
    String afterResetPasswordPage(Model model) {
        return HTMX.fragmentOrFullPage(model, AFTER_RESET_PASSWORD_TEMPLATE);
    }

    @GetMapping("/set-new-password")
    String setNewPasswordPage(Model model,
                              @RequestParam String token) {
        model.addAttribute("token", token);
        return HTMX.fragmentOrFullPage(model, SET_NEW_PASSWORD_TEMPLATE);
    }

    @PostMapping("/set-new-password")
    String setNewPassword(Model model,
                          @ModelAttribute SetNewPasswordCommand command) {
        setNewUserPasswordHandler.handle(command);
        return afterSetNewPasswordPage(model);
    }

    @GetMapping("/after-set-new-password")
    String afterSetNewPasswordPage(Model model) {
        return HTMX.fragmentOrFullPage(model, AFTER_SET_NEW_PASSWORD_TEMPLATE);
    }
}
