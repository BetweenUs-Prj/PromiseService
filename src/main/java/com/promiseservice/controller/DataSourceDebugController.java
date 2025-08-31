package com.promiseservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

/**
 * 데이터소스 디버그 컨트롤러
 * 이유: 실행 중인 앱이 실제로 어떤 데이터베이스에 연결되어 있는지 확인하기 위해
 * 환경 착오 방지 및 DB 연결 상태 진단에 사용
 */
@Slf4j
@RestController
@RequestMapping("/api/debug/ds")
@RequiredArgsConstructor
public class DataSourceDebugController {

    private final DataSource dataSource;

    /**
     * 데이터베이스 연결 정보 조회
     * 이유: 실행 중인 앱이 실제로 어떤 DB에 연결되어 있는지 확인하여 환경 착오 방지
     * 
     * @return 데이터베이스 연결 정보
     */
    @GetMapping
    public Map<String, Object> getDataSourceInfo() {
        log.info("=== 🔍 데이터소스 정보 조회 시작 ===");
        
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 기본 연결 정보
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("driverName", metaData.getDriverName());
            result.put("driverVersion", metaData.getDriverVersion());
            result.put("url", connection.getMetaData().getURL());
            result.put("username", connection.getMetaData().getUserName());
            result.put("catalog", connection.getCatalog());
            result.put("schema", connection.getSchema());
            
            // HikariCP 정보 (사용 중인 경우)
            if (dataSource.getClass().getName().contains("HikariDataSource")) {
                try {
                    var hikariDs = (com.zaxxer.hikari.HikariDataSource) dataSource;
                    result.put("hikariJdbcUrl", hikariDs.getJdbcUrl());
                    result.put("hikariUsername", hikariDs.getUsername());
                    result.put("hikariPoolName", hikariDs.getPoolName());
                    result.put("hikariMaximumPoolSize", hikariDs.getMaximumPoolSize());
                } catch (Exception e) {
                    result.put("hikariError", e.getMessage());
                }
            }
            
            log.info("데이터소스 정보 조회 성공: {}", result);
            
        } catch (Exception e) {
            log.error("데이터소스 정보 조회 실패: {}", e.getMessage());
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
