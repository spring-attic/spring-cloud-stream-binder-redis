/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.binder.redis.config;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.RedisHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cloud.CloudAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Bind to services, either locally or in a cloud environment.
 *
 * @author Mark Fisher
 * @author Dave Syer
 * @author David Turanski
 * @author Eric Bottard
 */
@Configuration
@ConditionalOnMissingBean(Binder.class)
@Import(RedisMessageChannelBinderConfiguration.class)
@PropertySource("classpath:/META-INF/spring-cloud-stream/redis-binder.properties")
@AutoConfigureBefore({CloudAutoConfiguration.class, RedisAutoConfiguration.class})
public class RedisServiceAutoConfiguration {

	@Configuration
	@ConditionalOnClass(Cloud.class)
	@Profile("cloud")
	protected static class CloudConfig {

		@Bean
		public Cloud cloud() {
			return new CloudFactory().getCloud();
		}

		@Bean
		@ConditionalOnMissingBean(RedisConnectionFactory.class)
		RedisConnectionFactory redisConnectionFactory(Cloud cloud) {
			return cloud.getSingletonServiceConnector(RedisConnectionFactory.class, null);
		}
	}

	@Profile("!cloud")
	@Import(RedisAutoConfiguration.class)
	protected static class NoCloudConfig {
	}

	@Bean
	public HealthIndicator binderHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
		return new RedisHealthIndicator(redisConnectionFactory);
	}
}
