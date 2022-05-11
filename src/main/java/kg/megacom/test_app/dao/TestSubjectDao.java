package kg.megacom.test_app.dao;

import kg.megacom.test_app.models.entities.Subject;
import kg.megacom.test_app.models.entities.Test;
import kg.megacom.test_app.models.entities.TestSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestSubjectDao extends JpaRepository<TestSubject, Long> {

    List<TestSubject> findAllByTest(Test test);

    @Query("select t from TestSubject t where t.test = ?1 and  t.subject = ?2")
    TestSubject findAllByTestAndSubject(Test test, Subject subject);
}
