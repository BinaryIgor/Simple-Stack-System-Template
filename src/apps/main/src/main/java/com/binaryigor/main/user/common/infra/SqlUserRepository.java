package com.binaryigor.main.user.common.infra;

import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main._common.infra.JooqTools;
import com.binaryigor.main._contract.model.UserRole;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.core.repository.UserUpdateRepository;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SqlUserRepository implements UserRepository, UserUpdateRepository {

    private static final Table<?> USER = DSL.table("\"user\".\"user\"");
    private static final Field<UUID> ID = DSL.field("id", UUID.class);
    private static final Field<String> NAME = DSL.field("name", String.class);
    private static final Field<String> EMAIL = DSL.field("email", String.class);
    private static final Field<String> PASSWORD = DSL.field("password", String.class);
    private static final Field<String> LANGUAGE = DSL.field("language", String.class);
    private static final Field<String> STATE = DSL.field("state", String.class);
    private static final Field<String[]> ROLES = DSL.field("roles", String[].class);
    private static final Field<Boolean> SECOND_FACTOR_AUTH = DSL.field("second_factor_auth", Boolean.class);

    private final DSLContext context;

    public SqlUserRepository(DSLContext context) {
        this.context = context;
    }

    @Override
    public UUID create(User user) {
        context.insertInto(USER)
                .columns(ID, NAME, EMAIL, LANGUAGE, STATE, ROLES, PASSWORD, SECOND_FACTOR_AUTH)
                .values(user.id(), user.name(), user.email(),
                        user.language().name(), user.state().name(),
                        JooqTools.collectionToStringArray(user.roles(), Enum::name),
                        user.password(), user.secondFactorAuth())
                .execute();

        return user.id();
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return context.selectFrom(USER)
                .where(EMAIL.eq(email))
                .fetchOptional(this::userFromRecord);
    }

    private User userFromRecord(Record r) {
        return new User(r.get(ID),
                r.get(NAME),
                r.get(EMAIL),
                AppLanguage.valueOf(r.get(LANGUAGE)),
                UserState.valueOf(r.get(STATE)),
                Set.of(r.get(ROLES)).stream()
                        .map(UserRole::valueOf)
                        .collect(Collectors.toSet()),
                r.get(PASSWORD),
                r.get(SECOND_FACTOR_AUTH));
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return context.selectFrom(USER)
                .where(ID.eq(id))
                .fetchOptional(this::userFromRecord);
    }

    @Override
    public void updateName(UUID id, String name) {
        context.update(USER)
                .set(NAME, name)
                .where(ID.eq(id))
                .execute();
    }

    @Override
    public void updateEmail(UUID id, String email) {
        context.update(USER)
                .set(EMAIL, email)
                .where(ID.eq(id))
                .execute();
    }

    @Override
    public void updateState(UUID id, UserState state) {
        context.update(USER)
                .set(STATE, state.name())
                .where(ID.eq(id))
                .execute();
    }

    @Override
    public void updatePassword(UUID id, String password) {
        context.update(USER)
                .set(PASSWORD, password)
                .where(ID.eq(id))
                .execute();
    }
}
