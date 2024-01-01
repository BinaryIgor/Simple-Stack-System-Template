package com.binaryigor.main.user.common.core.model;

import com.binaryigor.main._contract.model.UserState;

import java.util.UUID;

public record UserStateChangedEvent(UUID id, UserState newState) {
}
