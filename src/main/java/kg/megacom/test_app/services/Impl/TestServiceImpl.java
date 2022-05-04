package kg.megacom.test_app.services.Impl;

import kg.megacom.test_app.dao.TestDao;
import kg.megacom.test_app.mappers.TestMapper;
import kg.megacom.test_app.models.dto.*;
import kg.megacom.test_app.models.dto.json.*;
import kg.megacom.test_app.models.entities.Test;
import kg.megacom.test_app.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
    @Autowired
    private AnswerService answerService;

    private final Logger log = LoggerFactory.getLogger(this.getClass());


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
        log.info("Test create input body --- {}", createJson);
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

    @Override
    public PreparedTest getById(Long testId) {
        PreparedTest prepTest = new PreparedTest();
        // Получить объект тест по id
        TestDto testDto = findById(testId);
        // проверка на наличие теста
        if(testDto == null){
            prepTest.setStatus(0);
            prepTest.setMessage("Test not found");
            return prepTest;
        }

        List<PreparedQuestion> questions = new ArrayList<>();

        List<TestSubjectDto> testSubjectDtos = testSubjectService.findAllByTest(testDto);
        testSubjectDtos.stream().forEach(x->{
            List<TestSubjectQuestionDto> questionsBySubject = testSubjectQuestionService.findAllByTestSubject(x);
            questionsBySubject.stream().forEach(y->{
                   PreparedQuestion preparedQuestion = new PreparedQuestion();
                   preparedQuestion.setId(y.getQuestion().getId());
                   preparedQuestion.setQuestion(y.getQuestion().getQuestion());
                   preparedQuestion.setSubjectId(y.getTestSubject().getSubject().getId());

                   List<PreparedAnswer> answers = new ArrayList<>();
                   List<AnswerDto> answerDtoList = answerService.findAllByQuestion(y.getQuestion());
                   answerDtoList.stream().forEach(z->{
                       PreparedAnswer preparedAnswer = new PreparedAnswer();
                       preparedAnswer.setId(z.getId());
                       preparedAnswer.setAnswer(z.getAnswer());
                       answers.add(preparedAnswer);
                   });
                   Collections.shuffle(answers);
                   preparedQuestion.setAnswers(answers);
                   questions.add(preparedQuestion);
            });
        });
        Collections.shuffle(questions);
        prepTest.setQuestions(questions);
        prepTest.setTestName(testDto.getName());
        prepTest.setTestId(testDto.getId());
        prepTest.setStatus(1);
        prepTest.setMessage("Успешно");
        return prepTest;
    }
}
