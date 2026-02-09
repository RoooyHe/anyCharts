package com.roy.anycharts.dashboard.repository;

import com.roy.anycharts.dashboard.entity.DashboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<DashboardEntity, String> {
}
