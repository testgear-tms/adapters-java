package io.test_gear.clients;

import io.test_gear.client.invoker.ApiException;
import io.test_gear.client.model.*;

import java.util.List;

public interface ApiClient {
    TestRunV2GetModel createTestRun() throws ApiException;
    TestRunV2GetModel getTestRun(String uuid) throws ApiException;
    void completeTestRun(String uuid) throws ApiException;
    void updateAutoTest(AutoTestPutModel model) throws ApiException;
    String createAutoTest(AutoTestPostModel model) throws ApiException;
    AutoTestModel getAutoTestByExternalId(String externalId) throws ApiException;
    void linkAutoTestToWorkItem(String id, String workItemId) throws ApiException;
    void sendTestResults(String testRunUuid, List<AutoTestResultsForTestRunModel> models) throws ApiException;
    String addAttachment(String path) throws ApiException;
    List<String> getTestFromTestRun(String testRunUuid, String configurationId) throws ApiException;
}
