package com.example.dias.festivos.project.repository;

import com.example.dias.festivos.project.entity.DiaFestivoData;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiaFestivoRepositoryAdapter implements DiaFestivoGatewayRepository {

    private final DiaFestivoRepository diaFestivoRepository;

    public DiaFestivoRepositoryAdapter(DiaFestivoRepository diaFestivoRepository) {
        this.diaFestivoRepository = diaFestivoRepository;
    }

    @Override
    public DiaFestivoData consultarDiaFestivo(int mes, int dia) {
        return diaFestivoRepository.findByMesAndDia(mes, dia);
    }

    @Override
    public List<DiaFestivoData> consultarFechasPorIdTipoFestivo(int tipoFecha) {
        return diaFestivoRepository.findAllByIdTipo(tipoFecha);
    }
}
