package com.jpastudy.lock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Board2Service {

    private final Board2Repository boardRepository;

    @Transactional
    public void likeWithOptimisticLock(Long boardId) {
        Board2 board = boardRepository.findById(boardId).orElseThrow();
        board.setLikes(board.getLikes() + 1); // 변경
        boardRepository.save(board); // 변경 감지로 version 증가
    }
}
