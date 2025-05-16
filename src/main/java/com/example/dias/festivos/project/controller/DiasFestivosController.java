package com.example.dias.festivos.project.controller;

import com.example.dias.festivos.project.service.DiaFestivoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.LocalDate;

@RestController
@RequestMapping("/festivos/verificar/{year}/{month}/{day}")
public class DiasFestivosController {

    public DiasFestivosController(DiaFestivoService diaFestivoService) {
        this.diaFestivoService = diaFestivoService;
    }

    private final DiaFestivoService diaFestivoService;

    @GetMapping
    public String determinarDiaFestivo(@PathVariable int year, @PathVariable int month, @PathVariable int day) {

        try {
            LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return "Fecha no válida";
        }
        if(diaFestivoService.determinarDiaFestivo(year, month, day)){
            return "Es festivo";
        } else {
            return "No es festivo";
        }
    }
}
