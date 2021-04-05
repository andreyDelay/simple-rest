package dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public interface Dto<T> {
    T get(T t);

    List<T> getAll(List<T> list);

    default String getJson(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            stringBuilder.append(objectMapper.writeValueAsString(list));
        } catch (JsonProcessingException | NullPointerException e) {
            //TODO
        }
        return stringBuilder.toString();
    }
}
