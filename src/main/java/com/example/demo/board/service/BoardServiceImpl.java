package com.example.demo.board.service;

import com.example.demo.account.entity.Account;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account_profile.entity.AccountProfile;
import com.example.demo.account_profile.repository.AccountProfileRepository;
import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import com.example.demo.board.service.request.CreateBoardRequest;
import com.example.demo.board.service.request.ListBoardRequest;
import com.example.demo.board.service.request.ModifyBoardRequest;
import com.example.demo.board.service.response.ListBoardResponse;
import com.example.demo.board.service.response.ModifyBoardResponse;
import com.example.demo.board.service.response.ReadBoardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    final private BoardRepository boardRepository;
    final private AccountRepository accountRepository;
    final private AccountProfileRepository accountProfileRepository;

    @Override
    public ListBoardResponse list(ListBoardRequest request) {
        // PageRequest 생성 (0부터 시작하는 페이지 인덱스를 위해 page - 1을 사용)
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getPerPage());

        // 페이징된 게시글 목록을 가져오기
        Page<Board> boardPage = boardRepository.findAllWithWriter(pageRequest);

        // ListBoardResponse 객체로 변환하여 반환
        return new ListBoardResponse(boardPage.getContent(), boardPage.getTotalElements(), boardPage.getTotalPages());
    }

    @Override
    public Board register(CreateBoardRequest createBoardRequest) {
        log.info("accountId: {}", createBoardRequest.getAccountId());

        Account account = accountRepository.findById(createBoardRequest.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account 존재하지 않음"));

        AccountProfile accountProfile = accountProfileRepository.findByAccount(account)
                .orElseThrow(() -> new RuntimeException("AccountProfile not found"));

        return boardRepository.save(createBoardRequest.toBoard(accountProfile));
    }

    @Override
    public ReadBoardResponse read(Long boardId) {
        Optional<Board> maybeBoard = boardRepository.findByIdWithWriter(boardId);

        if (maybeBoard.isEmpty()) {
            log.info("정보가 없습니다!");
            return null;
        }

        Board board = maybeBoard.get();
        return ReadBoardResponse.from(board);
    }

    @Override
    public boolean delete(Long boardId) {
        // 게시글 존재 여부 확인
        Optional<Board> maybeBoard = boardRepository.findById(boardId);
        if (maybeBoard.isEmpty()) {
            return false; // 존재하지 않으면 false 반환
        }

        // 삭제 수행
        boardRepository.deleteById(boardId);

        // 삭제 후 검증
        return !boardRepository.existsById(boardId);
    }

    @Override
    public ModifyBoardResponse modify(Long boardId, Long accountId, ModifyBoardRequest modifyBoardRequest) {
        Board board = boardRepository.findByIdWithWriter(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        // 작성자 검증
        log.info("board.getWriter(): {}", board.getWriter());
        log.info("board.getWriter().getAccount(): {}", board.getWriter().getAccount());
        log.info("board.getWriter().getAccount().getId(): {}", board.getWriter().getAccount().getId());

        log.info("accountId: {}", accountId);

        if (board.getWriter().getAccount().getId() != accountId) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 수정 작업 수행
        if (modifyBoardRequest.getTitle() != null) {
            board.setTitle(modifyBoardRequest.getTitle());
        }
        if (modifyBoardRequest.getContent() != null) {
            board.setContent(modifyBoardRequest.getContent());
        }

        boardRepository.save(board);

        return ModifyBoardResponse.from(board);
    }
}
