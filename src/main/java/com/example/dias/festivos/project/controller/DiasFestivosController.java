package com.example.dias.festivos.project.controller;

import com.example.dias.festivos.project.entity.dto.DescripcionDiaFestivoDto;
import com.example.dias.festivos.project.service.DiaFestivoService;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/festivos")
@CrossOrigin(origins = "http://localhost:4200")
public class DiasFestivosController {

    public DiasFestivosController(DiaFestivoService diaFestivoService) {
        this.diaFestivoService = diaFestivoService;
    }

    private final DiaFestivoService diaFestivoService;

    @GetMapping("/verificar/{year}/{month}/{day}")
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

    @GetMapping("/obtener/{year}")
    public List<DescripcionDiaFestivoDto> obtenerFestivosAnio(@PathVariable int year){
        return diaFestivoService.obtenerListaFestivosAnio(year);
    }
}
