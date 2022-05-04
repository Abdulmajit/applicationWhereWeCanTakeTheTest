package kg.megacom.test_app.models.dto.json;

import lombok.Data;

import java.util.List;

@Data
public class PreparedTest {

    private int status;
    private String message;
    private Long testId;
    private String testName;
    private List<PreparedQuestion> questions;
}
