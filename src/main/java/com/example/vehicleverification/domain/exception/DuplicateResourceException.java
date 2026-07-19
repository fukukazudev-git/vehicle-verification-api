package com.example.vehicleverification.domain.exception;

import lombok.Getter;

// 一意制約に違反する登録・更新を表す例外
// どの項目が重複したかを呼び出し元に伝えるためfieldを保持する
@Getter
public class DuplicateResourceException extends RuntimeException {

    private final String field;

    public DuplicateResourceException(String field, String message) {
        super(message);
        this.field = field;
    }

}
