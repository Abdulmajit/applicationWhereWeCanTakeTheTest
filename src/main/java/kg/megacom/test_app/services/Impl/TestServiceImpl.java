package kg.megacom.test_app.services.Impl;

import kg.megacom.test_app.dao.TestDao;
import kg.megacom.test_app.mappers.TestMapper;
import kg.megacom.test_app.models.dto.*;
import kg.megacom.test_app.models.dto.json.check.CheckRequestBody;
import kg.megacom.test_app.models.dto.json.check.CheckRequestQuest;
import kg.megacom.test_app.models.dto.json.check.CheckResponseBody;
import kg.megacom.test_app.models.dto.json.check.ResultSubject;
import kg.megacom.test_app.models.dto.json.create.TestCreateJson;
import kg.megacom.test_app.models.dto.json.create.TestResultJson;
import kg.megacom.test_app.models.dto.json.get.PreparedAnswer;
import kg.megacom.test_app.models.dto.json.get.PreparedQuestion;
import kg.megacom.test_app.models.dto.json.get.PreparedTest;
import kg.megacom.test_app.models.entities.Test;
import kg.megacom.test_app.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

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

    @Override
    public CheckResponseBody checkTest(CheckRequestBody checkRequestBody) {
        // Создать объект CheckResponseBody
        //1. Найти Тест(объект Test) по id
        //2. Сделать проверку на случай отсутствия теста
        //3. Сделать проверку на наличие отвеченных вопросов
        //4. Создать пустой список объектов ResultSubject
        //5. Начать перебор списка полученных в параметрах метода вопросов с ответами:
            // - найти тему(объект Subject) по id
               // если она есть, создать объект ResultSubject  заполнить два его поля subjectId и name и correctQuestions = 0
            // - найти вопрос(объект Question) по id
            // - найти правильные ответы по вопросу(лучше получить список, но по идее в нем должен быть только один объект)
            // - найти объект TestSubject по тесту(Test) и теме(Subject)
                 // из полученного объекта TestSubject взять значение поля
                      //questionAmount и положить в поле totalQuestionsBySubject в объекте ResultSubject
            // получить один объект правильного ответа из списка полученных ответов
            // если id этого ответа равен полученному из параметов, увеличить correctQuestions на 1
            // добавить заполненный объект ResultSubject в список этих объектов, созданных в п.4
        //6. Начинаем заполнять объект CheckResponseBody

        CheckResponseBody checkResponseBody = new CheckResponseBody();
        TestDto testDto = findById(checkRequestBody.getTestId());
        if(testDto == null){
            checkResponseBody.setStatus(0);
            checkResponseBody.setMessage("Тест не найден");
            return checkResponseBody;
        }
        if(checkRequestBody.getQuestions().isEmpty()){
            checkResponseBody.setStatus(0);
            checkResponseBody.setMessage("Нет ответов на вопросы");
            return checkResponseBody;
        }

        List<ResultSubject> subjects = new ArrayList<>();

        checkRequestBody.getQuestions().stream().filter(distinctByKey(CheckRequestQuest::getSubjectId)).forEach(x-> {
                    SubjectDto subjectDto = subjectService.findById(x.getSubjectId());
                    if (subjectDto == null) return;

                    ResultSubject resultSubject = new ResultSubject();
                    resultSubject.setSubjectId(x.getSubjectId());
                    resultSubject.setName(subjectDto.getTitle());
                    resultSubject.setCorrectQuestions(0);
                    subjects.add(resultSubject);

        });

        checkRequestBody.getQuestions().stream().forEach(x->{
                subjects.stream().forEach(y->{
                    if(x.getSubjectId().equals(y.getSubjectId())) {

                        QuestionDto questionDto = questionService.findById(x.getQuestionId());
                        if (questionDto == null) return;

                        List<AnswerDto> answerDtos = answerService.findAllByQuestionAndTrue(questionDto);

                        SubjectDto subjectDto = subjectService.findById(y.getSubjectId());

                        TestSubjectDto testSubjectDto = testSubjectService.findByTestAndSubject(testDto, subjectDto);
                        y.setTotalQuestionsBySubject(testSubjectDto.getQuestionAmount());

                        AnswerDto answerDto = answerDtos.get(0);

                        if (answerDto.getId().equals(x.getAnswerId())) {
                            y.setCorrectQuestions(y.getCorrectQuestions() + 1);
                        }
                    }
                });
        });

       checkResponseBody.setStatus(1);
       checkResponseBody.setMessage("Success");
       checkResponseBody.setSubjects(subjects);
       checkResponseBody.setTestName(testDto.getName());
       checkResponseBody.setTotalQuestions(subjects.stream().mapToInt(ResultSubject::getTotalQuestionsBySubject).sum());
       checkResponseBody.setCorrectAnsweredQuestions(subjects.stream().mapToInt(ResultSubject::getCorrectQuestions).sum());
       return checkResponseBody;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
