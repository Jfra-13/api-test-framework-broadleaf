package com.tesis.automation.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import com.tesis.automation.utils.ScenarioContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonHooks {
    
    private static final Logger logger = LoggerFactory.getLogger(CommonHooks.class);
    
    @Before
    public void setup() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("🚀 INICIANDO ESCENARIO CUCUMBER");
        logger.info("═══════════════════════════════════════════════════════════");
        
        // Limpiar contexto de escenarios previos
        ScenarioContext.clear();
    }
    
    @After
    public void teardown() {
        logger.info("═══════════════════════════════════════════════════════════");
        logger.info("✅ ESCENARIO FINALIZADO");
        logger.info("═══════════════════════════════════════════════════════════\n");
        
        // Limpiar contexto
        ScenarioContext.clear();
    }
}
