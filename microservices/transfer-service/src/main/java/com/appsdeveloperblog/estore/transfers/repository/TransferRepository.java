package com.appsdeveloperblog.estore.transfers.repository;

import com.appsdeveloperblog.estore.transfers.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, String> {

}
