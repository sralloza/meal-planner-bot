package base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.net.URL;

public class BaseTest {
    protected  <T> T readJson(Class<T> clazz, String filename) {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();
            return mapper.readValue(new File(resource.toURI()), clazz);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
