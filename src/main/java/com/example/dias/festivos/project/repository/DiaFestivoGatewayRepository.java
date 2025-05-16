package com.example.dias.festivos.project.repository;

import com.example.dias.festivos.project.entity.DiaFestivoData;

import java.util.List;

public interface DiaFestivoGatewayRepository {

    DiaFestivoData consultarDiaFestivo (int mes, int dia);
    List<DiaFestivoData> consultarFechasPorIdTipoFestivo(int tipoFecha);
}
