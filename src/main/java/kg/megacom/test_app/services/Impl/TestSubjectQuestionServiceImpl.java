package kg.megacom.test_app.services.Impl;

import kg.megacom.test_app.dao.TestSubjectQuestionDao;
import kg.megacom.test_app.mappers.TestSubjectMapper;
import kg.megacom.test_app.mappers.TestSubjectQuestionMapper;
import kg.megacom.test_app.models.dto.TestSubjectDto;
import kg.megacom.test_app.models.dto.TestSubjectQuestionDto;
import kg.megacom.test_app.models.entities.TestSubjectQuestion;
import kg.megacom.test_app.services.TestSubjectQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestSubjectQuestionServiceImpl implements TestSubjectQuestionService {
    @Autowired
    private TestSubjectQuestionDao testSubjectQuestionDao;
    private TestSubjectQuestionMapper testSubjectQuestionMapper = TestSubjectQuestionMapper.INSTANCE;
    private TestSubjectMapper testSubjectMapper = TestSubjectMapper.INSTANCE;
    @Override
    public TestSubjectQuestionDto save(TestSubjectQuestionDto testSubjectQuestionDto) {
        TestSubjectQuestion testSubjectQuestion = testSubjectQuestionMapper.testSubjectQuestionDtoToTestSubjectQuestion(testSubjectQuestionDto);
        TestSubjectQuestion testSubjectQuestionSaved = testSubjectQuestionDao.save(testSubjectQuestion);
        return testSubjectQuestionMapper.testSubjectQuestionToTestSubjectQuestionDto(testSubjectQuestionSaved);
    }

    @Override
    public TestSubjectQuestionDto findById(Long id) {
        TestSubjectQuestion testSubjectQuestion =  testSubjectQuestionDao.findById(id).orElse(null);
        return testSubjectQuestionMapper.testSubjectQuestionToTestSubjectQuestionDto(testSubjectQuestion);
    }

    @Override
    public TestSubjectQuestionDto update(TestSubjectQuestionDto testSubjectQuestionDto){
        boolean isExists = testSubjectQuestionDao.existsById(testSubjectQuestionDto.getId());
        if (!isExists){
            return null;
        }else{
            TestSubjectQuestion testSubjectQuestion = testSubjectQuestionMapper.testSubjectQuestionDtoToTestSubjectQuestion(testSubjectQuestionDto);
            TestSubjectQuestion  updatedTestSubjectQuestion = testSubjectQuestionDao.save(testSubjectQuestion);
            return testSubjectQuestionMapper.testSubjectQuestionToTestSubjectQuestionDto(updatedTestSubjectQuestion);
        }
    }

    @Override
    public TestSubjectQuestionDto delete(TestSubjectQuestionDto testSubjectQuestionDto) {
        return null;
    }

    @Override
    public List<TestSubjectQuestionDto> findAllByTestSubject(TestSubjectDto testSubjectDto) {
        List<TestSubjectQuestion> testSubQuestions =
                testSubjectQuestionDao.findAllByTestSubject(testSubjectMapper.testSubjectDtoToTestSubject(testSubjectDto));
        return testSubjectQuestionMapper.testSubjectQuestionListToTestSubjectQuestionDtoList(testSubQuestions);
    }
}
