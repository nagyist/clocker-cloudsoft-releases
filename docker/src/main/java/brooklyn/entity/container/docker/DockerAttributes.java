/*
 * Copyright 2014 by Cloudsoft Corporation Limited
 *
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
package brooklyn.entity.container.docker;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import brooklyn.config.ConfigKey;
import brooklyn.config.render.RendererHints;
import brooklyn.entity.basic.ConfigKeys;
import brooklyn.event.AttributeSensor;
import brooklyn.event.basic.AttributeSensorAndConfigKey;
import brooklyn.event.basic.Sensors;
import brooklyn.util.text.ByteSizeStrings;
import brooklyn.util.time.Duration;
import brooklyn.util.time.Time;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Functions;

public class DockerAttributes {

    /** Do not instantiate. */
    private DockerAttributes() { }

    /*
     * Configuration and constants.
     */

    public static final String DEFAULT_DOCKER_CONTAINER_NAME_FORMAT = "docker-container-brooklyn-%1$s";
    public static final String DEFAULT_DOCKER_HOST_NAME_FORMAT = "docker-host-brooklyn-%1$s";

    public static final String UBUNTU_DOCKERFILE = "classpath://brooklyn/entity/container/docker/ubuntu/Dockerfile";
    public static final String CENTOS_DOCKERFILE = "classpath://brooklyn/entity/container/docker/centos/Dockerfile";

    /** Valid characters for the Dockerfile location. */
    public static final CharMatcher DOCKERFILE_CHARACTERS = CharMatcher.anyOf("-_.")
            .or(CharMatcher.inRange('a', 'z'))
            .or(CharMatcher.inRange('A', 'Z'))
            .or(CharMatcher.inRange('0', '9'));
    public static final CharMatcher DOCKERFILE_INVALID_CHARACTERS = DOCKERFILE_CHARACTERS.negate();

    public static final ConfigKey<String> DOCKERFILE_URL = ConfigKeys.newStringConfigKey("docker.dockerfile.url", "URL of a DockerFile to use");

    public static final AttributeSensorAndConfigKey<String, String> DOCKER_IMAGE_ID = ConfigKeys.newSensorAndConfigKey(String.class, "docker.imageId", "The ID of a Docker image to use for a container");

    public static final AttributeSensorAndConfigKey<String, String> DOCKER_HARDWARE_ID = ConfigKeys.newSensorAndConfigKey(String.class, "docker.hardwareId", "The ID of a Docker gardware type to use for a container", "small");

    /*
     * Sensor attributes for Docker containers.
     */

    public static final AttributeSensor<Duration> UPTIME = Sensors.newSensor(Duration.class, "docker.container.uptime", "Current uptime");

    public static final AttributeSensor<Double> CPU_USAGE = Sensors.newDoubleSensor("docker.container.cpu", "Current CPU usage");
    public static final AttributeSensor<Double> AVERAGE_CPU_USAGE = Sensors.newDoubleSensor("docker.cpu.average", "Average CPU usage across the cluster");

    public static final AttributeSensor<Long> USED_MEMORY = Sensors.newLongSensor("docker.container.memory", "Current memory usage");
    public static final AttributeSensor<Long> USED_MEMORY_DELTA_PER_SECOND_LAST = Sensors.newLongSensor("docker.container.memory.delta", "Change in memory usage per second");
    public static final AttributeSensor<Long> USED_MEMORY_DELTA_PER_SECOND_IN_WINDOW = Sensors.newLongSensor("docker.container.memory.windowed", "Average change in memory usage over 30s");

    /*
     * Counter attributes.
     */

    public static final AttributeSensor<Integer> DOCKER_HOST_COUNT = Sensors.newIntegerSensor("docker.hostCount", "Number of Docker hosts");
    public static final AttributeSensor<Integer> DOCKER_CONTAINER_COUNT = Sensors.newIntegerSensor("docker.containerCount", "Number of Docker containers");
    public static final AttributeSensor<Integer> DOCKER_IDLE_HOST_COUNT = Sensors.newIntegerSensor("docker.hostCount.idle", "Number of idle Docker hosts");
    public static final AttributeSensor<Integer> DOCKER_IDLE_CONTAINER_COUNT = Sensors.newIntegerSensor("docker.containerCount.idle", "Number of idle Docker containers");

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /** Setup renderer hints. */
    @SuppressWarnings("rawtypes")
    public static void init() {
        if (initialized.getAndSet(true)) return;

        Function longValue = new Function<Double, Long>() {
            @Override
            public Long apply(@Nullable Double input) {
                if (input == null) return null;
                return input.longValue();
            }

        };

        RendererHints.register(UPTIME, RendererHints.displayValue(Time.toTimeStringRounded()));

        RendererHints.register(USED_MEMORY, RendererHints.displayValue(ByteSizeStrings.metric()));
        RendererHints.register(USED_MEMORY_DELTA_PER_SECOND_LAST, RendererHints.displayValue(Functions.compose(ByteSizeStrings.metric(), longValue)));
        RendererHints.register(USED_MEMORY_DELTA_PER_SECOND_IN_WINDOW, RendererHints.displayValue(Functions.compose(ByteSizeStrings.metric(), longValue)));
    }

    static {
        init();
    }
}