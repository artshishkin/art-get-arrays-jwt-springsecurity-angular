package net.shyshkin.study.fullstack.supportportal.backend.mapper;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(imports = {LocalDateTime.class})
public interface UserMapper {

    @Mapping(target = "isNotLocked", source = "nonLocked")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "joinDate", expression = "java( LocalDateTime.now() )")
    @Mapping(target = "role", source = "role", resultType = String.class)
    @Mapping(target = "authorities", source = "role.authorities")
    User toEntity(UserDto userDto);

}
