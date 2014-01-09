/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.addthis.hydra.job;

import java.util.Arrays;

import com.addthis.hydra.job.spawn.JobAlert;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JobAlertTest {

    @Test
    public void basicTriggerTest() throws Exception {
        long now = System.currentTimeMillis();

        Job idleJob = createJobWithState(JobState.IDLE);
        Job errorJob = createJobWithState(JobState.ERROR);
        Job runningJob = createJobWithState(JobState.RUNNING);

        JobAlert errorAlert = new JobAlert("errorAlert", 0, -1, "noone", null);
        assertTrue("Error alert should trigger with at least one error job", errorAlert.checkAlertForJobs(Arrays.asList(idleJob, errorJob)));
        errorAlert.clear();
        assertTrue("Error alert should not trigger with only idle job", !errorAlert.checkAlertForJobs(Arrays.asList(idleJob)));

        JobAlert completeAlert = new JobAlert("completeAlert", 1, -1, "noone", null);
        assertTrue("Complete alert should not trigger with running job", !completeAlert.checkAlertForJobs(Arrays.asList(runningJob)));
        runningJob.setState(JobState.IDLE);
        assertTrue("Complete alert should trigger on job completion", completeAlert.checkAlertForJobs(Arrays.asList(runningJob)));
        runningJob.setState(JobState.RUNNING);

        JobAlert runtimeAlert = new JobAlert("runtimeAlert", 2, 60, "noone", null);
        assertTrue("Runtime alert should not trigger with idle job", !runtimeAlert.checkAlertForJobs(Arrays.asList(idleJob)));
        runningJob.setSubmitTime(now - 1000);
        assertTrue("Runtime alert should not trigger with recently-submitted job", !runtimeAlert.checkAlertForJobs(Arrays.asList(runningJob)));
        runningJob.setSubmitTime(now - 180 * 60 * 1000);
        assertTrue("Runtime alert should trigger with long-running job", runtimeAlert.checkAlertForJobs(Arrays.asList(runningJob)));

        JobAlert rekickAlert = new JobAlert("rekickAlert", 3, 60, "noone", null);
        idleJob.setEndTime(now - 10 * 60 * 1000);
        assertTrue("Rekick alert should not fire after short time period", !rekickAlert.checkAlertForJobs(Arrays.asList(idleJob)));
        idleJob.setEndTime(now - 300 * 60 * 1000);
        assertTrue("Rekick alert should fire after long time period", rekickAlert.checkAlertForJobs(Arrays.asList(idleJob)));

    }

    private Job createJobWithState(JobState jobState) throws Exception {
        Job job = new Job();
        job.setState(jobState);
        return job;
    }
}