package com.vipassistant.web.backend.mapper;


import com.vipassistant.web.backend.dto.UserDTO;
import com.vipassistant.web.backend.entity.User;
import com.vipassistant.web.backend.enums.UserRole;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface UserMapper {
    List<UserDTO> toUserDTOList(List<User> userList);

    UserDTO toUserDTO(User user);

    User toUser(UserDTO userDTO);

    @AfterMapping
    default void afterUserMapping(UserDTO userDTO,
                                    @MappingTarget User user) {
        user.setRole(UserRole.USER);
        user.setSocialDistanceSet(new HashSet<>());
        user.setTestedPositive(false);
    }
}
