package com.binaryigor.email.factory;

import com.binaryigor.email.model.Email;
import com.binaryigor.email.model.NewEmailTemplate;

public interface EmailFactory {
    Email newEmail(NewEmailTemplate template);
}
