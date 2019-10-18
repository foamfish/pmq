package com.ppdai.infrastructure.mq.client.core.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.codahale.metrics.MetricFilter;
import com.ppdai.infrastructure.mq.client.MqClient.IMqClientBase;
import com.ppdai.infrastructure.mq.client.core.IMqMeticReporterService;
import com.ppdai.infrastructure.mq.client.metric.MetricSingleton;

public class MqMeticReporterService implements IMqMeticReporterService {
	private AtomicBoolean startFlag = new AtomicBoolean(false);
	private MqMetricReporter reporter;
	private volatile static MqMeticReporterService instance = null;

	/**
	 * 获取单例
	 */
	public static MqMeticReporterService getInstance(IMqClientBase mqClientBase) {
		if (instance == null) {
			synchronized (MqTopicQueueRefreshService.class) {
				if (instance == null) {
					instance = new MqMeticReporterService(mqClientBase);
				}
			}
		}
		return instance;
	}

	private MqMeticReporterService(IMqClientBase mqClientBase) {
		reporter = new MqMetricReporter(MetricSingleton.getMetricRegistry(), "mq-client", MetricFilter.ALL,
				TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS, null, mqClientBase.getContext());
	}

	@Override
	public void start() {
		if (startFlag.compareAndSet(false, true)) {
			// 每30s上报数据
			reporter.start(30, TimeUnit.SECONDS);
		}
	}

	@Override
	public void close() {
		startFlag.set(false);
		instance = null;
		if (reporter != null) {
			try {
				reporter.stop();
				reporter = null;
			} catch (Exception e) {
			}
		}

	}
}
