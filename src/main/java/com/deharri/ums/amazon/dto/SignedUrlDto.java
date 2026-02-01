package com.deharri.ums.amazon.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

/**
 * Data Transfer Object for pre-signed S3 URLs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "SignedUrl",
        description = "Response containing a pre-signed URL for S3 resource access"
)
public class SignedUrlDto {

    @Schema(
            description = "Pre-signed URL for direct access to the S3 resource. Valid for a limited time.",
            example = "https://bucket-name.s3.region.amazonaws.com/path/to/file?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
    )
    private URL url;
}
