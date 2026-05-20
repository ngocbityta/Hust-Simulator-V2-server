package com.hustsimulator.social.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MessageTypeConverter implements AttributeConverter<MessageType, String> {

    @Override
    public String convertToDatabaseColumn(MessageType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public MessageType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return MessageType.valueOf(dbData.toUpperCase());
    }
}
