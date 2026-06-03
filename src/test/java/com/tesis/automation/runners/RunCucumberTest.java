package com.tesis.automation.runners;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-report.html, json:target/cucumber.json, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "com.tesis.automation.steps, com.tesis.automation.hooks")
public class RunCucumberTest {}
