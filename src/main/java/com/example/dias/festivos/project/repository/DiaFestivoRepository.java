package com.example.dias.festivos.project.repository;

import com.example.dias.festivos.project.entity.DiaFestivoData;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface DiaFestivoRepository extends Repository<DiaFestivoData, Integer> {
    DiaFestivoData findByMesAndDia(int mes, int dia);

    List<DiaFestivoData> findAllByIdTipo(int idTipo);
}
