package com.cocinarubi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Pool dedicado para las tareas @Async de AuditoriaService.
 *
 * <p>Sin este bean, @EnableAsync usa SimpleAsyncTaskExecutor (crea un thread por tarea
 * sin reutilizar), que bajo carga alta agota threads del SO. Con core=2, max=4 y
 * queue=500 el pool sostiene ~40 escrituras/s. DiscardOldestPolicy asegura que bajo
 * saturación se descartan las auditorías más antiguas en cola, no las nuevas ni el
 * request HTTP.</p>
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "auditExecutor")
    public TaskExecutor auditExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("audit-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(10);
        ex.initialize();
        return ex;
    }
}
