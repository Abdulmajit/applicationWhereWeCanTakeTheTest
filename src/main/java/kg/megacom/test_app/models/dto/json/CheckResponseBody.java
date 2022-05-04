package kg.megacom.test_app.models.dto.json;

import lombok.Data;
import org.springframework.core.SpringVersion;

@Data
public class CheckResponseBody {

    private Long testId;
    private String testName;
    private int totalQuestions;
    private int correctAnsweredQuest;

}
