package com.example.vehicleverification.domain.exception;

// 他のリソースから参照されているため削除できないことを表す例外
// DELETEはリクエストボディを持たず、指し示す入力項目がないためfieldは保持しない
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
