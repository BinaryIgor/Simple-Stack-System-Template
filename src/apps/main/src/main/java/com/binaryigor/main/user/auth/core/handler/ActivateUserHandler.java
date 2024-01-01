package com.binaryigor.main.user.auth.core.handler;

import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.ActivationTokenConsumer;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.main.user.common.core.model.UserStateChangedEvent;
import com.binaryigor.main.user.common.core.repository.UserUpdateRepository;
import com.binaryigor.types.event.AppEventsPublisher;

public class ActivateUserHandler {

    private final ActivationTokenConsumer activationTokenConsumer;
    private final UserUpdateRepository userUpdateRepository;
    private final AppEventsPublisher appEventsPublisher;

    public ActivateUserHandler(ActivationTokenConsumer activationTokenConsumer,
                               UserUpdateRepository userUpdateRepository,
                               AppEventsPublisher appEventsPublisher) {
        this.activationTokenConsumer = activationTokenConsumer;
        this.userUpdateRepository = userUpdateRepository;
        this.appEventsPublisher = appEventsPublisher;
    }

    public void handle(String activationToken) {
        activationTokenConsumer.consume(activationToken, ActivationTokenType.NEW_USER,
                userId -> {
                    userUpdateRepository.updateState(userId, UserState.ACTIVATED);
                    appEventsPublisher.publish(new UserStateChangedEvent(userId, UserState.ACTIVATED));
                });
    }
}
