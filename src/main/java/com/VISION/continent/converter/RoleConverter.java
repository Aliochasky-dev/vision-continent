package com.VISION.continent.converter;


import com.VISION.continent.entity.User;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<User.Role, String> {

    @Override
    public String convertToDatabaseColumn(User.Role role) {
        return role != null ? role.name() : null;
    }

    @Override
    public User.Role convertToEntityAttribute(String value) {
        if (value == null) return User.Role.USER;
        return switch (value.toLowerCase()) {
            case "user"         -> User.Role.USER;
            case "admin"        -> User.Role.ADMIN;
            case "moderateur"   -> User.Role.MODERATEUR;
            default -> User.Role.valueOf(value); // valeur exacte
        };
    }
}
