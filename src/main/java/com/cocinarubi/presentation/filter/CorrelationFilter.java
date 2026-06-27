package com.cocinarubi.presentation.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class CorrelationFilter extends OncePerRequestFilter {

    private static final String HEADER  = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";
    private static final Pattern ID_VALIDO = Pattern.compile("^[a-zA-Z0-9\\-_]{1,64}$");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String serverCorrelationId = UUID.randomUUID().toString();

        String clientId = request.getHeader(HEADER);
        String mdcCorrelationId = (clientId != null && ID_VALIDO.matcher(clientId).matches())
                ? clientId
                : serverCorrelationId;

        MDC.put(MDC_KEY, mdcCorrelationId);
        response.setHeader(HEADER, serverCorrelationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
