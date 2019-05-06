package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MessagePlanDao extends JpaRepository<MessagePlan, Integer>, JpaSpecificationExecutor<MessagePlan> {
}
