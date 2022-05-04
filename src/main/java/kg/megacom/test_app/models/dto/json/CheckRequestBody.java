package kg.megacom.test_app.models.dto.json;

import lombok.Data;

import java.util.List;

@Data
public class CheckRequestBody {

    private Long testId;
    private List<CheckRequestQuest> questions;
}
