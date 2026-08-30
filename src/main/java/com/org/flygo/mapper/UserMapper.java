package com.org.flygo.mapper;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUserEntity(UserEntity user);
    UserEntity toUserEntity(User user);
}
