package com.example.dias.festivos.project.config;

import com.example.dias.festivos.project.repository.DiaFestivoGatewayRepository;
import com.example.dias.festivos.project.service.DiaFestivoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public DiaFestivoService diaFestivoService(DiaFestivoGatewayRepository diaFestivoGatewayRepository) {
        return new DiaFestivoService(diaFestivoGatewayRepository);
    }

}
