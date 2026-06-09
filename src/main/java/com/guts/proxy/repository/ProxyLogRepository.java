package com.guts.proxy.repository;

import com.guts.proxy.model.ProxyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyLogRepository extends JpaRepository<ProxyLog,Integer> {
}
