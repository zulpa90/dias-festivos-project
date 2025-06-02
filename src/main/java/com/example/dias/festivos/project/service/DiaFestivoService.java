package com.example.dias.festivos.project.service;

import com.example.dias.festivos.project.entity.DiaFestivoData;
import com.example.dias.festivos.project.entity.dto.DescripcionDiaFestivoDto;
import com.example.dias.festivos.project.repository.DiaFestivoGatewayRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiaFestivoService {

    private static final String ACCION_RESTAR_DIAS = "Restar días";
    private static final String ACCION_SUMAR_DIAS = "Sumar días";

    private final DiaFestivoGatewayRepository diaFestivoGatewayRepository;

    public DiaFestivoService(DiaFestivoGatewayRepository diaFestivoGatewayRepository) {
        this.diaFestivoGatewayRepository = diaFestivoGatewayRepository;
    }

    public boolean determinarDiaFestivo(int year, int month, int day) {

        var diaFestivo = esLunesFestivoLeyPuenteFestivo(LocalDate.of(year, month, day), 2);

        if (!diaFestivo) {
            DiaFestivoData diaFestivoData = diaFestivoGatewayRepository.consultarDiaFestivo(month, day);
            if (diaFestivoData != null && diaFestivoData.getIdTipo() == 1) {
                diaFestivo = true;
            } else if (esFestivoBasadoEnDomingoPascua(LocalDate.of(year, month, day), true)) {
                diaFestivo = true;
            } else if (esFestivoBasadoEnDomingoPascuaConLeyPuenteFestivo(LocalDate.of(year, month, day))) {
                diaFestivo = true;
            }
        }
        return diaFestivo;
    }

    private boolean esLunesFestivoLeyPuenteFestivo(LocalDate fechaConsultada, int idTipoFestivo) {

        if (fechaConsultada.getDayOfWeek() != DayOfWeek.MONDAY) return false;

        List<DiaFestivoData> diasFestivo = diaFestivoGatewayRepository.consultarFechasPorIdTipoFestivo(idTipoFestivo);

        if (idTipoFestivo == 2) {
            List<String> diaMesFestivo = diasFestivo.stream().map(diaFestivoData -> diaFestivoData.getMes() + "-" + diaFestivoData.getDia())
                    .toList();

            for (String festivoOriginal : diaMesFestivo) {
                String[] parts = festivoOriginal.split("-");
                int mes = Integer.parseInt(parts[0]);
                int dia = Integer.parseInt(parts[1]);

                LocalDate originalDate = LocalDate.of(fechaConsultada.getYear(), mes, dia);

                LocalDate festivoMovido = originalDate;
                if (originalDate.getDayOfWeek() != DayOfWeek.MONDAY) {
                    festivoMovido = originalDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                }

                if (fechaConsultada.equals(festivoMovido)) {
                    return true;
                }
            }
            return false;
        } else if (idTipoFestivo == 4) {
            var domingoRamos = retornarFechaDomingoRamos(fechaConsultada.getYear());
            var domingoPascua = retornarFechaBasadaEnDomingoPascua(domingoRamos, 7, ACCION_SUMAR_DIAS);
            List<LocalDate> diasFestivos = diasFestivo.stream().map(diaFestivoData ->
                    domingoPascua.plusDays(diaFestivoData.getDiasPascua()).with(TemporalAdjusters.next(DayOfWeek.MONDAY))).toList();
            return diasFestivos.contains(fechaConsultada);
        }
        return false;
    }

    private boolean esFestivoBasadoEnDomingoPascua(LocalDate fechaConsultada, boolean soloTipoDomingoPascua) {

        var domingoRamos = retornarFechaDomingoRamos(fechaConsultada.getYear());
        var domingoPascua = retornarFechaBasadaEnDomingoPascua(domingoRamos, 7, ACCION_SUMAR_DIAS);

        if (soloTipoDomingoPascua) {
            var juevesSanto = retornarFechaBasadaEnDomingoPascua(domingoPascua, 3, ACCION_RESTAR_DIAS);
            var viernesSanto = retornarFechaBasadaEnDomingoPascua(domingoPascua, 2, ACCION_RESTAR_DIAS);

            return fechaConsultada.equals(domingoPascua)
                    || fechaConsultada.equals(juevesSanto)
                    || fechaConsultada.equals(viernesSanto);
        } else {
            var ascensionSr = retornarFechaBasadaEnDomingoPascua(domingoPascua, 40, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

            var corpusChristi = retornarFechaBasadaEnDomingoPascua(domingoPascua, 61, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

            var sagradoCorazon = retornarFechaBasadaEnDomingoPascua(domingoPascua, 68, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

            return fechaConsultada.equals(ascensionSr)
                    || fechaConsultada.equals(corpusChristi)
                    || fechaConsultada.equals(sagradoCorazon);
        }
    }

    private boolean esFestivoBasadoEnDomingoPascuaConLeyPuenteFestivo(LocalDate fechaConsultada) {
        if (esLunesFestivoLeyPuenteFestivo(fechaConsultada, 4)) {
            return esFestivoBasadoEnDomingoPascua(fechaConsultada, false);
        }
        return false;
    }

    private LocalDate retornarFechaBasadaEnDomingoPascua(LocalDate fechaInicial, int cantidadDias, String tipoModificacion) {
        if (tipoModificacion.equals(ACCION_SUMAR_DIAS)) {
            return fechaInicial.plusDays(cantidadDias);
        } else {
            return fechaInicial.minusDays(cantidadDias);
        }
    }

    private LocalDate retornarFechaDomingoRamos(int year) {
        var a = year % 19;
        var b = year % 4;
        var c = year % 7;
        var d = (19 * a + 24) % 30;

        var diasParaDomingoRamos = d + ((2 * b) + (4 * c) + (6 * d) + 5) % 7;
        return LocalDate.of(year, 3, 15).plusDays(diasParaDomingoRamos);
    }

    public List<DescripcionDiaFestivoDto> obtenerListaFestivosAnio(int year) {
        var listaFestivos = diaFestivoGatewayRepository.consultarDiasFestivosAnio();
        Map<Integer, List<DiaFestivoData>> listaFestivosTipo = listaFestivos.stream().collect(Collectors.groupingBy(DiaFestivoData::getIdTipo));
        List<DescripcionDiaFestivoDto> descripcionDiasFestivo = new ArrayList<>();

        listaFestivosTipo.forEach((idTipo, listaDiasFestivos) -> {
            if (idTipo == 1) {
                var diasFestivosFijos = listaDiasFestivos.stream().map(diaFestivo -> {
                    String descripcionFestivo = diaFestivo.getNombre();
                    LocalDate fechaLocalDate = formatoFechaInicial(diaFestivo.getDia(), diaFestivo.getMes(), year);
                    return new DescripcionDiaFestivoDto(descripcionFestivo, localDateTimeToStringConvert(fechaLocalDate));
                }).toList();

                descripcionDiasFestivo.addAll(diasFestivosFijos);
            } else if (idTipo == 2) {
                var diasLeyPuenteFestivo = listaDiasFestivos.stream().map(diaFestivo -> {
                    LocalDate fechaLocalDate = formatoFechaInicial(diaFestivo.getDia(), diaFestivo.getMes(), year);
                    String localDateTimeString;

                    if (fechaLocalDate.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
                        localDateTimeString = localDateTimeToStringConvert(fechaLocalDate);
                    } else {
                        fechaLocalDate = fechaLocalDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                        localDateTimeString = localDateTimeToStringConvert(fechaLocalDate);
                    }
                    String descripcionFestivo = diaFestivo.getNombre();
                    return new DescripcionDiaFestivoDto(descripcionFestivo, localDateTimeString);
                }).toList();
                descripcionDiasFestivo.addAll(diasLeyPuenteFestivo);
            } else if (idTipo == 3) {
                descripcionDiasFestivo.addAll(retornarFechasFestivoBasadoEnDomingoPascua(year, true));
            } else if (idTipo == 4) {
                descripcionDiasFestivo.addAll(retornarFechasFestivoBasadoEnDomingoPascua(year, false));
            }
        });
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        descripcionDiasFestivo.sort(Comparator.comparing(dto -> OffsetDateTime.parse(dto.getFecha(), formatter)));
        return descripcionDiasFestivo;
    }

    private LocalDate formatoFechaInicial(int day, int month, int year) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return LocalDate.parse(String.format("%02d/%02d/%d", day, month, year), inputFormatter);

    }

    private List<DescripcionDiaFestivoDto> retornarFechasFestivoBasadoEnDomingoPascua(int year, boolean soloTipoDomingoPascua) {
        List<DescripcionDiaFestivoDto> descripcionDiaFestivo = new ArrayList<>();

        var domingoRamos = retornarFechaDomingoRamos(year);
        var domingoPascua = retornarFechaBasadaEnDomingoPascua(domingoRamos, 7, ACCION_SUMAR_DIAS);

        if (soloTipoDomingoPascua) {
            var juevesSanto = retornarFechaBasadaEnDomingoPascua(domingoPascua, 3, ACCION_RESTAR_DIAS);
            descripcionDiaFestivo.add(new DescripcionDiaFestivoDto("Jueves Santo", localDateTimeToStringConvert(juevesSanto)));

            var viernesSanto = retornarFechaBasadaEnDomingoPascua(domingoPascua, 2, ACCION_RESTAR_DIAS);
            descripcionDiaFestivo.add(new DescripcionDiaFestivoDto("Viernes Santo", localDateTimeToStringConvert(viernesSanto)));
        } else {
            var ascensionSr = retornarFechaBasadaEnDomingoPascua(domingoPascua, 40, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            descripcionDiaFestivo.add(new DescripcionDiaFestivoDto("Ascensión del Señor", localDateTimeToStringConvert(ascensionSr)));

            var corpusChristi = retornarFechaBasadaEnDomingoPascua(domingoPascua, 61, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            descripcionDiaFestivo.add(new DescripcionDiaFestivoDto("Corpus Christi", localDateTimeToStringConvert(corpusChristi)));

            var sagradoCorazon = retornarFechaBasadaEnDomingoPascua(domingoPascua, 68, ACCION_SUMAR_DIAS)
                    .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            descripcionDiaFestivo.add(new DescripcionDiaFestivoDto("Sagrado Corazón de Jesús", localDateTimeToStringConvert(sagradoCorazon)));
        }
        return descripcionDiaFestivo;
    }

    private String localDateTimeToStringConvert(LocalDate fechaLocalDate) {
        LocalTime horaPorDefecto = LocalTime.of(5, 0, 0);
        ZoneOffset offsetPorDefecto = ZoneOffset.ofHours(0);
        LocalDateTime dateTimeConHoraPorDefecto = LocalDateTime.of(fechaLocalDate, horaPorDefecto);
        OffsetDateTime finalOffsetDateTime = OffsetDateTime.of(dateTimeConHoraPorDefecto, offsetPorDefecto);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String finalOffsetDateTimeString = finalOffsetDateTime.format(outputFormatter);

        if (finalOffsetDateTimeString.endsWith("Z")) {
            finalOffsetDateTimeString = finalOffsetDateTimeString.replace("Z", "+00:00");
        }

        return finalOffsetDateTimeString;
    }
}