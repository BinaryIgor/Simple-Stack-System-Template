package com.binaryigor.main.user.auth.app;

import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main.user.auth.core.model.SignUpCommand;

public record SignUpRequest(String name,
                            String email,
                            AppLanguage language,
                            String password) {


    public SignUpCommand toCommand() {
        return new SignUpCommand(name, email, language, password);
    }
}
