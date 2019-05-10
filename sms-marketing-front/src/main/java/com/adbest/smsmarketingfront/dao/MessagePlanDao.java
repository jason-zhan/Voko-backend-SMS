package com.adbest.smsmarketingfront.dao;

import com.adbest.smsmarketingentity.MessagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessagePlanDao extends JpaRepository<MessagePlan, Long>, JpaSpecificationExecutor<MessagePlan> {

    @Query("select MessagePlan from MessagePlan where id = ?1 and disable = false ")
    MessagePlan getOneUsable(Long id);
}
