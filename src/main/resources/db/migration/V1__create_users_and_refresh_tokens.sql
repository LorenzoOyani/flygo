create table users
(
    id         uuid primary key,

    full_name  varchar(150) not null,
    email      varchar(254) not null,

    status     varchar(50)  not null,
    password   varchar(500) not null,
    role       varchar(50)  not null default 'CUSTOMER',

    created_at timestamptz  not null default current_timestamp,
    updated_at timestamptz  not null default current_timestamp,

    constraint uk_users_email unique (email),
    constraint uk_users_full_name unique (full_name)
);

create index idx_users_status on users (status);

create table refresh_tokens
(
    id         uuid primary key,

    user_id    uuid         not null,
    token_hash varchar(255) not null,

    expires_at timestamptz  not null,
    revoked_at timestamptz,
    created_at timestamptz  not null default current_timestamp,

    constraint fk_refresh_tokens_user
        foreign key (user_id) references users (id) on delete cascade,

    constraint uk_refresh_tokens_token_hash unique (token_hash)
);

create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
create index idx_refresh_tokens_expires_at on refresh_tokens (expires_at);