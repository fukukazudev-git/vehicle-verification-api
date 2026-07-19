package com.example.vehicleverification.presentation.advice;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.example.vehicleverification.domain.exception.DuplicateResourceException;
import com.example.vehicleverification.domain.exception.ResourceNotFoundException;
import com.example.vehicleverification.presentation.dto.error.ErrorResponse;
import com.example.vehicleverification.presentation.dto.error.FieldValidationError;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;

// ResponseEntityExceptionHandlerを継承することで、Spring MVCが投げる標準例外
// (型変換失敗・JSONパース失敗・未対応メソッドなど)が基底クラスの個別ハンドラに
// マッチするようになる。継承しない場合、下のhandleExceptionが先に全部拾ってしまい
// 本来4xxで返すべきクライアント起因のエラーが500になる
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    // 一意制約違反(重複登録)の例外処理ハンドラ
    // どの項目が重複したかをfieldErrorsで返す
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of(new FieldValidationError(ex.getField(), ex.getMessage())));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // サービス層の事前チェックとcommitの間に別トランザクションが割り込んだ場合の保険
    // 事前チェックだけでは同時実行時にすり抜けるため両方必要
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "既に登録されている値と重複しています。",
                request.getRequestURI(),
                null);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
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
    // 基底クラスが宣言している標準例外は、より具体的な型としてそちらが優先されるため
    // ここには本当に想定外のものだけが来る
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

    // バリデーションの例外処理ハンドラ
    // 基底クラスもMethodArgumentNotValidExceptionを宣言しているため、独自の
    // @ExceptionHandlerではなくprotectedフックのoverrideとして実装する
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
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
                resolvePath(request),
                fieldErrors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 基底クラスの各ハンドラはすべてここを通る。既定のボディはProblemDetail(RFC 7807)で
    // このプロジェクトのErrorResponseと形が違うため、ここで統一する
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String error = (status != null)
                ? status.getReasonPhrase()
                : String.valueOf(statusCode.value());

        // クライアント起因の4xxはログに残さない。5xxのみ詳細を記録する
        if (statusCode.is5xxServerError()) {
            log.error("予期しないエラーが発生しました: {}", resolvePath(request), ex);
        }

        // ex.getMessage()はパーサ内部の型名などが漏れるためクライアントには返さない
        String message = statusCode.is4xxClientError()
                ? "リクエストの形式が不正です。"
                : "サーバーエラーが発生しました。";

        ErrorResponse errorBody = new ErrorResponse(
                statusCode.value(),
                error,
                message,
                resolvePath(request),
                null);

        return new ResponseEntity<>(errorBody, headers, statusCode);
    }

    // 基底クラスのハンドラはWebRequestを受け取るため、自前のハンドラが使う
    // HttpServletRequest#getRequestURIと同じ形式のパスを取り出す
    private String resolvePath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }

}
