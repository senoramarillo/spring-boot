/*
 * Copyright 2012-2023 the original author or authors.
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

package org.springframework.boot.actuate.autoconfigure.jms;

import java.time.Duration;
import java.util.Map;

import jakarta.jms.ConnectionFactory;

import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.jms.JmsHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link JmsHealthIndicator}.
 *
 * @author Stephane Nicoll
 * @author Moritz Halbritter
 * @since 2.0.0
 */
@AutoConfiguration(after = { ActiveMQAutoConfiguration.class, ArtemisAutoConfiguration.class })
@ConditionalOnClass(ConnectionFactory.class)
@ConditionalOnBean(ConnectionFactory.class)
@ConditionalOnEnabledHealthIndicator("jms")
public class JmsHealthContributorAutoConfiguration
		extends CompositeHealthContributorConfiguration<JmsHealthIndicator, ConnectionFactory> {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	public JmsHealthContributorAutoConfiguration(Environment environment) {
		super((connectionFactory) -> new JmsHealthIndicator(connectionFactory, getTaskExecutor(environment), TIMEOUT));
	}

	@Bean
	@ConditionalOnMissingBean(name = { "jmsHealthIndicator", "jmsHealthContributor" })
	public HealthContributor jmsHealthContributor(Map<String, ConnectionFactory> connectionFactories) {
		return createContributor(connectionFactories);
	}

	private static SimpleAsyncTaskExecutor getTaskExecutor(Environment environment) {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("jms-health-indicator");
		if (Threading.VIRTUAL.isActive(environment)) {
			taskExecutor.setVirtualThreads(true);
		}
		return taskExecutor;
	}

}
