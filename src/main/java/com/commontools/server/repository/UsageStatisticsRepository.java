package com.commontools.server.repository;

import com.commontools.server.model.UsageStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageStatisticsRepository extends JpaRepository<UsageStatistics, Long> {
    Optional<UsageStatistics> findByUserIdAndModuleAndActionAndStatDateAndEndpointAndIpAddress(
            String userId, String module, String action, LocalDateTime statDate, String endpoint, String ipAddress);

    List<UsageStatistics> findByUserId(String userId);
    
    List<UsageStatistics> findByStatDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT us FROM UsageStatistics us WHERE us.statDate BETWEEN :startDate AND :endDate")
    List<UsageStatistics> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT us.module, SUM(us.count) as totalCount FROM UsageStatistics us " +
           "WHERE us.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY us.module ORDER BY totalCount DESC")
    List<Object[]> getModuleStatistics(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    @Query("SELECT us.module, us.action, SUM(us.count) as totalCount FROM UsageStatistics us " +
           "WHERE us.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY us.module, us.action ORDER BY us.module, totalCount DESC")
    List<Object[]> getModuleActionStatistics(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT us.module, SUM(us.count) as totalCount FROM UsageStatistics us " +
           "WHERE us.userId = :userId AND us.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY us.module ORDER BY totalCount DESC")
    List<Object[]> getUserModuleStatistics(@Param("userId") String userId,
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
}
