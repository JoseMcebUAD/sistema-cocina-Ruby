package com.cocinarubi.dao;

import com.cocinarubi.entity.HorarioAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Integer> {

    @Override
    @Query("SELECT h FROM HorarioAtencion h ORDER BY h.tipoHorario ASC, h.diaSemana ASC")
    List<HorarioAtencion> findAll();
}
