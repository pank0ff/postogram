package com.pank0ff.postogram.repos;

import com.pank0ff.postogram.domain.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RateRepo extends JpaRepository<Rate, Long> {

    List<Rate> findAll();

    List<Rate> findByUserId(Long id);

    Rate findById(Integer id);


    List<Rate> findByMessageId(Integer id);

}
