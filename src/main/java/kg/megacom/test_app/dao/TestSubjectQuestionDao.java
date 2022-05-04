package kg.megacom.test_app.dao;

import kg.megacom.test_app.models.entities.TestSubject;
import kg.megacom.test_app.models.entities.TestSubjectQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSubjectQuestionDao extends JpaRepository<TestSubjectQuestion, Long> {

    List<TestSubjectQuestion> findAllByTestSubject(TestSubject testSubject);
}
