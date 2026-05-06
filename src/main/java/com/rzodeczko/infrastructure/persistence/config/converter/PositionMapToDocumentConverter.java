package com.rzodeczko.infrastructure.persistence.config.converter;

import com.rzodeczko.domain.vo.Position;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.Map;

@WritingConverter
public class PositionMapToDocumentConverter implements Converter<Map<Position, Boolean>, Document> {


    @Override
    public Document convert(Map<Position, Boolean> map) {
        Document document = new Document();
        map.forEach((key, val) -> document.put(key.toString(), val));
        return document;
    }
}
