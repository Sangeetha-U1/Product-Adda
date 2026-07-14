package com.productadda.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * ================================================================
 * SchedulingConfig
 * Enables Spring's @Scheduled support for ScheduledReconciliationTask.
 * Added as its own dedicated config class rather than adding
 * @EnableScheduling directly to ProductaddaApplication.java, since
 * that file's current content was not available in context - merge
 * the annotation there instead if you prefer a single entry point.
 * ================================================================
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

}
