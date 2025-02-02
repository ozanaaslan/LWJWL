package com.github.ozanaaslan.lwjwl.web.session;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SameSite {
    NO_RESTRICTION("No Restriction"),
    LAX("Lax"),
    STRICT("Strict");
    @Getter
    @NonNull
    String property;
}
