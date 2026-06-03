package com.hustsimulator.social.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum MessageType {
    TEXT,
    IMAGE,
    FILE;

    /** JPA converter: stores 'text'/'image'/'file' (lowercase) in DB. */
    @Converter(autoApply = false)
    public static class TypeConverter implements AttributeConverter<MessageType, String> {
        @Override
        public String convertToDatabaseColumn(MessageType attribute) {
            return attribute == null ? null : attribute.name().toLowerCase();
        }

        @Override
        public MessageType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : MessageType.valueOf(dbData.toUpperCase());
        }
    }
}
