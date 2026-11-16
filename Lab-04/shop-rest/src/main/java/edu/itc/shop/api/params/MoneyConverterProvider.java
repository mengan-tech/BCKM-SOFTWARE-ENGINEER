package edu.itc.shop.api.params;

import edu.itc.shop.domain.Money;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class MoneyConverterProvider implements ParamConverterProvider {
    private static final ParamConverter<Money> CONVERTER = new ParamConverter<>() {
        @Override
        public Money fromString(String value) {
            try {
                return Money.parse(value);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Cannot parse Money from '" + value + "'", ex);
            }
        }

        @Override
        public String toString(Money value) {
            return value.toString();
        }
    };

    @Override
    public <T> ParamConverter<T> getConverter(
            Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType != Money.class) {
            return null;
        }
        return new ParamConverter<>() {
            @Override
            public T fromString(String value) {
                return rawType.cast(CONVERTER.fromString(value));
            }

            @Override
            public String toString(T value) {
                return CONVERTER.toString(Money.class.cast(value));
            }
        };
    }
}
