package kg.megacom.test_app.models.dto.json;

import lombok.Data;

import java.util.List;

@Data
public class PreparedQuestion {

    private Long id;
    private String question;
    private Long subjectId;
    private List<PreparedAnswer> answers;
}
