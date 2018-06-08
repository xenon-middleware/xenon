/*
 * Copyright 2013 Netherlands eScience Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.esciencecenter.xenon.adaptors.schedulers.slurm;

import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.LOAD_STANDARD_KNOWN_HOSTS;
import static nl.esciencecenter.xenon.adaptors.schedulers.ssh.SshSchedulerAdaptor.STRICT_HOST_KEY_CHECKING;

import java.util.HashMap;
import java.util.Map;

import org.junit.ClassRule;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;

import nl.esciencecenter.xenon.XenonException;
import nl.esciencecenter.xenon.adaptors.schedulers.SchedulerLocationConfig;
import nl.esciencecenter.xenon.credentials.PasswordCredential;
import nl.esciencecenter.xenon.schedulers.Scheduler;

public class Slurm17SchedulerDockerTest extends SlurmSchedulerTestParent {

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder().file("src/integrationTest/resources/docker-compose/slurm-17.yml")
            .waitingForService("slurm", HealthChecks.toHaveAllPortsOpen()).build();

    @Override
    protected SchedulerLocationConfig setupLocationConfig() {
        return new SlurmLocationConfig(docker.containers().container("slurm").port(22).inFormat("ssh://$HOST:$EXTERNAL_PORT"), "/home/xenon");
    }

    @Override
    public Scheduler setupScheduler(SchedulerLocationConfig config) throws XenonException {
        String location = docker.containers().container("slurm").port(22).inFormat("ssh://$HOST:$EXTERNAL_PORT");
        PasswordCredential cred = new PasswordCredential("xenon", "javagat".toCharArray());
        Map<String, String> props = new HashMap<>();
        props.put(STRICT_HOST_KEY_CHECKING, "false");
        props.put(LOAD_STANDARD_KNOWN_HOSTS, "false");
        return Scheduler.create("slurm", config.getLocation(), cred, props);
    }

}
