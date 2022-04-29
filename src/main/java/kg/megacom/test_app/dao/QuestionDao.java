package kg.megacom.test_app.dao;

import kg.megacom.test_app.models.dto.QuestionDto;
import kg.megacom.test_app.models.dto.SubjectDto;
import kg.megacom.test_app.models.entities.Question;
import kg.megacom.test_app.models.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionDao extends JpaRepository<Question, Long> {
//    select * from Cars c
//    inner join Colours cl  on c.colourID = cl.ID
    //@Query("select q from Question q inner join Subject s on q.subject.id = s.id")
    //@Query("select q from Question q where q.subject = ?1 and q.isActive = true")
    @Query(value = "select * from tb_question tbq where tbq.subject_id = ?1 and tbq.is_active = true", nativeQuery = true)
    List<Question> findAllBySubjectAndIsActiveTrue(Long subjectId);

    @Query(value = "select * from tb_question q where q.is_active = true and q.subject_id = ?1 order by RANDOM() LIMIT ?2", nativeQuery = true)
    List<Question> findAllBySubjectByRandom(Long subjectId, int amount);

}
