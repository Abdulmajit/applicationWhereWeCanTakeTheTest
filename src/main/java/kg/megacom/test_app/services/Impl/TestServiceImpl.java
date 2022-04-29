package kg.megacom.test_app.services.Impl;

import kg.megacom.test_app.dao.TestDao;
import kg.megacom.test_app.mappers.TestMapper;
import kg.megacom.test_app.models.dto.*;
import kg.megacom.test_app.models.dto.json.TestCreateJson;
import kg.megacom.test_app.models.dto.json.TestResultJson;
import kg.megacom.test_app.models.entities.Test;
import kg.megacom.test_app.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private TestDao testDao;

    private TestMapper testMapper = TestMapper.INSTANCE;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private TestSubjectService testSubjectService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private TestSubjectQuestionService testSubjectQuestionService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public TestDto save(TestDto testDto) {
        Test test = testMapper.testDtoToTest(testDto);
        test.setActive(true);
        Test testSaved = testDao.save(test);
        return testMapper.testToTestDto(testSaved);
    }

    @Override
    public TestDto findById(Long id) {
        Test test = testDao.findById(id).orElse(null);
        return testMapper.testToTestDto(test);
    }

    @Override
    public TestDto update(TestDto testDto) {
        boolean isExists = testDao.existsById(testDto.getId());
        if (!isExists){
            return null;
        }else{
            Test test = testMapper.testDtoToTest(testDto);
            Test updatedTest = testDao.save(test);
            return testMapper.testToTestDto(updatedTest);
        }
    }

    @Override
    public TestDto delete(TestDto testDto) {
        Test test = testMapper.testDtoToTest(testDto);
        test.setActive(false);
        TestDto deletedTest = update(testMapper.testToTestDto(test));
        return deletedTest;
    }

    @Override
    public List<TestDto> findAllByActive() {
        List<Test> tests = testDao.findAllByActive();
        return testMapper.testListToTestDtoList(tests);
    }

    @Override
    public TestResultJson createNewTest(TestCreateJson createJson) {
        LanguageDto languageDto = languageService.findById(createJson.getLanguageId());
        if(languageDto == null){
            TestResultJson result = new TestResultJson();
            result.setStatus(0);
            result.setMessage("Lang not found");
            return result;
        }

        TestDto testDto = new TestDto();
        testDto.setName(createJson.getName());
        testDto.setNavi_user(createJson.getAuthor());
        TestDto savedTest = save(testDto);

        createJson.getSubjectJsons().stream().forEach(x->{
            SubjectDto subjectDto = subjectService.findById(x.getSubjectId());

            TestSubjectDto testSubjectDto = new TestSubjectDto();
            testSubjectDto.setQuestionAmount(x.getQuestionsAmount());
            testSubjectDto.setSubject(subjectDto);
            testSubjectDto.setTest(savedTest);
            TestSubjectDto testSubjectDtoSaved = testSubjectService.save(testSubjectDto);

            List<QuestionDto> questions = questionService.findQuestionsRandomly(subjectDto, x.getQuestionsAmount());
            questions.stream().forEach(y->{
                  TestSubjectQuestionDto testSubjectQuestionDto = new TestSubjectQuestionDto();
                  testSubjectQuestionDto.setQuestion(y);
                  testSubjectQuestionDto.setTestSubject(testSubjectDtoSaved);
                  TestSubjectQuestionDto savedTestSubQuest = testSubjectQuestionService.save(testSubjectQuestionDto);
            });
        });

        TestResultJson result = new TestResultJson();
        result.setStatus(1);
        result.setMessage("Success");
        result.setTestName(savedTest.getName());
        result.setTestId(savedTest.getId());
        return result;
    }
}
