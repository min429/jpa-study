package com.jpa.study.lock;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface Board1Repository extends JpaRepository<Board1, Long> {

	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("SELECT b FROM Board1 b WHERE b.id = :id")
	Optional<Board1> findByIdWithReadLock(Long id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT b FROM Board1 b WHERE b.id = :id")
	Optional<Board1> findByIdWithWriteLock(Long id);

	@Modifying
	@Transactional
	@Query("UPDATE Board1 b SET b.likes = b.likes + 1 WHERE b.id = :id")
	int incrementLikes(Long id);
}
