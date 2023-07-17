package com.portfolio.models.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MailDto {
    String message;
    String subject;
}
