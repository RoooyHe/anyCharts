package com.roy.anycharts.chart.repository;

import com.roy.anycharts.chart.entity.ChartConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChartConfigRepository extends JpaRepository<ChartConfigEntity, String> {}
