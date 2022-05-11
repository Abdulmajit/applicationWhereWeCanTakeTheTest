package kg.megacom.test_app.models.dto.json.check;

import lombok.Data;

@Data
public class CheckRequestQuest {

    private Long questionId;
    private Long subjectId;
    private Long answerId;
}
