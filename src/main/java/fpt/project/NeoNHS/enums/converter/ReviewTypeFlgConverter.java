package fpt.project.NeoNHS.enums.converter;

import fpt.project.NeoNHS.enums.ReviewTypeFlg;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReviewTypeFlgConverter implements AttributeConverter<ReviewTypeFlg, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ReviewTypeFlg attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public ReviewTypeFlg convertToEntityAttribute(Integer dbData) {
        return ReviewTypeFlg.fromValue(dbData);
    }
}
