package com.example.chatservice.domain.dto.out;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReadReceiptResDto {
    private String messageId;
    private String readerUuid;
}
