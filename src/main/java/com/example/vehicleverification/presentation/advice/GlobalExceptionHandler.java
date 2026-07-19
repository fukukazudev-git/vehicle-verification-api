package com.example.vehicleverification.presentation.advice;

import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.List;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.presentation.dto.error.ErrorResponse;
import com.example.vehicleverification.presentation.dto.error.FieldValidationError;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ResourceNotFoundExceptionの例外処理ハンドラ
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), // status 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // error "Not Found"
                ex.getMessage(), // message "Resource not found"
                request.getRequestURI(), // path "/users/{id}"
                null // fieldErrors
        );
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // バリデーションの例外処理ハンドラ
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldValidationError(
                        err.getField(),
                        err.getDefaultMessage()))
                .toList();

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "入力値が不正です。",
                request.getRequestURI(),
                fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 楽観的ロックの例外処理ハンドラ
    // @Versionによる実際の競合はSpringがObjectOptimisticLockingFailureExceptionにラップするため、
    // サービス層が明示的に投げるOptimisticLockExceptionと合わせて両方を受ける
    @ExceptionHandler({ OptimisticLockException.class, ObjectOptimisticLockingFailureException.class })
    public ResponseEntity<ErrorResponse> handleOptimisticLock(
            Exception ex,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "ほかのユーザーが先に更新しました。",
                request.getRequestURI(),
                null);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // その他の予期しない例外は500 Internal Server Errorとして処理する
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {
        // 例外の詳細はログにのみ出力し、クライアントには固定文言を返す
        log.error("予期しないエラーが発生しました: {}", request.getRequestURI(), ex);

        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "サーバーエラーが発生しました。",
                request.getRequestURI(),
                null);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
