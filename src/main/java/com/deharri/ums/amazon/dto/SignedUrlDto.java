package com.deharri.ums.amazon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;

@AllArgsConstructor
@Data
public class SignedUrlDto {

    private final URL URL;

}
