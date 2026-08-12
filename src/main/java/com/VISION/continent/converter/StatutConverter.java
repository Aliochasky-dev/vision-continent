package com.VISION.continent.converter;
import com.VISION.continent.entity.User;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StatutConverter implements AttributeConverter<User.Statut, String> {

    @Override
    public String convertToDatabaseColumn(User.Statut statut) {
        return statut != null ? statut.name() : null;
    }

    @Override
    public User.Statut convertToEntityAttribute(String value) {
        if (value == null) return User.Statut.ACTIF;
        return switch (value.toLowerCase()) {
            case "actif", "active", "user"  -> User.Statut.ACTIF;
            case "suspendu", "suspended"    -> User.Statut.SUSPENDU;
            case "banni", "banned"          -> User.Statut.BANNI;
            default -> User.Statut.valueOf(value); // valeur exacte
        };
    }
}