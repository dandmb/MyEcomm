package com.dmb.payment.handler;

import java.util.Map;

public record ErrorResponse(
    Map<String, String> errors
) {

}
