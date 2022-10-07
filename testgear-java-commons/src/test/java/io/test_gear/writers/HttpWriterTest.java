package io.test_gear.writers;

import io.test_gear.Helper;
import io.test_gear.clients.ApiClient;
import io.test_gear.clients.ClientConfiguration;
import io.test_gear.models.*;
import io.test_gear.services.ResultStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.test_gear.client.invoker.ApiException;
import io.test_gear.client.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

class HttpWriterTest {
    private final static String TEST_RUN_ID = "5819479d-e38b-40d0-9e35-c5b2dab50158";

    private ClientConfiguration config;
    private ApiClient client;
    private ResultStorage storage;


    @BeforeEach
    void init() {
        this.client = mock(ApiClient.class);
        this.config = mock(ClientConfiguration.class);
        this.storage = mock(ResultStorage.class);

        when(config.getUrl()).thenReturn("https://example.test/");
        when(config.getProjectId()).thenReturn("d7defd1e-c1ed-400d-8be8-091ebfdda744");
        when(config.getConfigurationId()).thenReturn("b09d7164-d58c-41a5-9780-89c30e0cc0c7");
        when(config.getPrivateToken()).thenReturn("QwertyT0kentPrivate");
        when(config.getTestRunId()).thenReturn(TEST_RUN_ID);
    }

    @Test
    void startLaunch_WithTestRunId_NoInvokeCreateHandler() throws ApiException {
        // arrange
        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.startLaunch();

        // assert
        verify(client, never()).createTestRun(new TestRunV2PostShortModel());
    }

    @Test
    void startLaunch_WithoutTestRunIdAndWithoutTestRunName_InvokeCreateHandler() throws ApiException {
        // arrange
        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(config.getProjectId()));

        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setId(UUID.fromString(TEST_RUN_ID));

        when(client.createTestRun(model)).thenReturn(response);
        when(config.getTestRunId()).thenReturn("null");
        when(config.getTestRunName()).thenReturn("null");

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.startLaunch();

        // assert
        verify(client, times(1)).createTestRun(model);
        verify(config, times(1)).setTestRunId(TEST_RUN_ID);
    }

    @Test
    void startLaunch_WithoutTestRunIdAndWithTestRunName_InvokeCreateHandler() throws ApiException {
        // arrange
        TestRunV2PostShortModel model = new TestRunV2PostShortModel();
        model.setProjectId(UUID.fromString(config.getProjectId()));
        model.setName("Test run name");

        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setId(UUID.fromString(TEST_RUN_ID));
        response.setName("Test run name");

        when(client.createTestRun(model)).thenReturn(response);
        when(config.getTestRunId()).thenReturn("null");
        when(config.getTestRunName()).thenReturn("Test run name");

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.startLaunch();

        // assert
        verify(client, times(1)).createTestRun(model);
        verify(config, times(1)).setTestRunId(TEST_RUN_ID);
    }

    @Test
    void finishLaunch_WithCompletedTestRun_NoInvokeCompleteHandler() throws ApiException {
        // arrange
        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setStateName(TestRunStateTypeModel.COMPLETED);

        when(client.getTestRun(TEST_RUN_ID)).thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.finishLaunch();

        // assert
        verify(client, never()).completeTestRun(anyString());
    }

    @Test
    void finishLaunch_WithInProgressTestRun_InvokeCompleteHandler() throws ApiException {
        // arrange
        TestRunV2GetModel response = new TestRunV2GetModel();
        response.setStateName(TestRunStateTypeModel.INPROGRESS);

        when(client.getTestRun(TEST_RUN_ID)).thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.finishLaunch();

        // assert
        verify(client, times(1)).completeTestRun(TEST_RUN_ID);
    }

    @Test
    void writeTest_WithExistingAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.setId(null);

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).updateAutoTest(request);
    }

    @Test
    void writeTest_WithCreatingAutoTest_InvokeCreateHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestPostModel request = Helper.generateAutoTestPostModel(config.getProjectId());

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).createAutoTest(request);
    }

    @Test
    void writeTest_WithWorkItemId_InvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        String autotestId = response.getId().toString();
        List<String> workItemGlobalId = testResult.getWorkItemId();

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, times(1)).linkAutoTestToWorkItem(autotestId, workItemGlobalId.get(0));
    }

    @Test
    void writeTest_WithoutWorkItemId_NoInvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult()
                .setWorkItemId(new ArrayList<>());

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, never()).linkAutoTestToWorkItem(anyString(), anyString());
    }

    @Test
    void writeTest_FiledExistingAutoTest_NoInvokeLinkHandler() throws ApiException {
        // arrange
        TestResult testResult = Helper.generateTestResult()
                .setItemStatus(ItemStatus.FAILED);
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client, never()).linkAutoTestToWorkItem(anyString(), anyString());
    }

    @Test
    void writeTest_FiledExistingAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        List<LinkItem> links = new ArrayList<>();
        LinkItem link = new LinkItem();
        link.setTitle("Title").setDescription("Description").setType(io.test_gear.models.LinkType.DEFECT).setUrl("http://test.example/bug123");
        links.add(link);

        TestResult testResult = Helper.generateTestResult()
                .setItemStatus(ItemStatus.FAILED)
                .setLinkItems(links);
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());

        List<LinkPutModel> putLinks = new ArrayList<>();
        LinkPutModel putLink = new LinkPutModel();
        putLink.setTitle("Title");
        putLink.setDescription("Description");
        putLink.setUrl("http://test.example/bug123");
        putLink.setType(io.test_gear.client.model.LinkType.DEFECT);
        putLinks.add(putLink);

        AutoTestPutModel putModel = Helper.generateAutoTestPutModel(config.getProjectId());
        putModel.links(putLinks);

        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTest(testResult);

        // assert
        verify(client).updateAutoTest(putModel);
    }

    @Test
    void writeClass_WithoutAutoTest_NoInvokeUpdateHandler() throws ApiException {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();

        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeClass(container);

        // assert
        verify(client, never()).updateAutoTest(any(AutoTestPutModel.class));
    }

    @Test
    void writeClass_WithAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        ClassContainer container = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.getSetup().add(Helper.generateBeforeEachSetup());
        request.getTeardown().add(Helper.generateAfterEachSetup());

        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeClass(container);

        // assert
        verify(client, times(1)).updateAutoTest(request);
    }

    @Test
    void writeTests_WithoutAutoTest_NoInvokeUpdateHandler() throws ApiException {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        ClassContainer classContainer = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();

        when(storage.getClassContainer(classContainer.getUuid()))
                .thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(null);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTests(container);

        // assert
        verify(client, never()).updateAutoTest(any(AutoTestPutModel.class));
        verify(client, never()).sendTestResults(eq(TEST_RUN_ID), any());
    }

    @Test
    void writeTests_WithAutoTest_InvokeUpdateHandler() throws ApiException {
        // arrange
        MainContainer container = Helper.generateMainContainer();
        ClassContainer classContainer = Helper.generateClassContainer();
        TestResult testResult = Helper.generateTestResult();
        AutoTestModel response = Helper.generateAutoTestModel(config.getProjectId());
        response.getSetup().add(Helper.generateBeforeEachSetup());
        response.getTeardown().add(Helper.generateAfterEachSetup());

        AutoTestPutModel request = Helper.generateAutoTestPutModel(config.getProjectId());
        request.getSetup().add(Helper.generateBeforeAllSetup());
        request.getSetup().add(Helper.generateBeforeEachSetup());
        request.getTeardown().add(Helper.generateAfterEachSetup());
        request.getTeardown().add(Helper.generateAfterAllSetup());

        when(storage.getClassContainer(classContainer.getUuid()))
                .thenReturn(Optional.of(classContainer));
        when(storage.getTestResult(testResult.getUuid()))
                .thenReturn(Optional.of(testResult));
        when(client.getAutoTestByExternalId(config.getProjectId(), testResult.getExternalId()))
                .thenReturn(response);

        Writer writer = new HttpWriter(config, client, storage);

        // act
        writer.writeTests(container);

        // assert
        verify(client, times(1)).updateAutoTest(request);
        verify(client, times(1)).sendTestResults(eq(TEST_RUN_ID), any());
    }

    @Test
    void writeAttachment_withValue_InvokeAddHandler() throws ApiException {
        // arrange
        Writer writer = new HttpWriter(config, client, storage);
        String path = "C:/test.txt";

        // act
        writer.writeAttachment(path);

        // assert
        verify(client).addAttachment(path);
    }
}