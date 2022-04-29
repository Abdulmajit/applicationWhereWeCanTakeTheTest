package kg.megacom.test_app.models.dto.json;

import lombok.Data;

@Data
public class TestResultJson {

    private int status;
    private String message;
    private Long testId;
    private String testName;
}
