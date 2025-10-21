package com.jpastudy.lock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Board1Service {

    private final Board1Repository boardRepository;

    @Transactional
    protected void likeWithoutLock(Long boardId) {
        Board1 board = boardRepository.findById(boardId).orElseThrow();
        board.setLikes(board.getLikes() + 1);
        boardRepository.save(board);
    }

    @Transactional
    public void likeWithWriteLock(Long boardId) {
        Board1 board = boardRepository.findByIdWithWriteLock(boardId).orElseThrow();
        board.setLikes(board.getLikes() + 1);
        boardRepository.save(board);
    }

    @Transactional
    public void likeWithReadLock(Long boardId) {
        Board1 board = boardRepository.findByIdWithReadLock(boardId).orElseThrow();
        board.setLikes(board.getLikes() + 1);
        boardRepository.save(board);
    }
}
