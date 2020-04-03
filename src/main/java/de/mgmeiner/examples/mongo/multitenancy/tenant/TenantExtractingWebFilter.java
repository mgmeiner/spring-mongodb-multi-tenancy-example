package de.mgmeiner.examples.mongo.multitenancy.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Extracts the tenant id from the X-Tenant http header and adds it to reactor's subscriber context.
 */
class TenantExtractingWebFilter implements WebFilter {

    private List<String> validTenantIds;

    public TenantExtractingWebFilter(List<String> validTenantIds) {
        this.validTenantIds = validTenantIds;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var tenantIdHeader = exchange.getRequest().getHeaders().getOrEmpty("X-Tenant");
        var response = exchange.getResponse();

        if (tenantIdHeader.isEmpty() || !validTenantIds.contains(tenantIdHeader.get(0))) {
            // tenant missing or not valid
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.setComplete();
        }

        var tenantId = tenantIdHeader.get(0);
        return chain.filter(exchange).subscriberContext(context -> context.put("tenant", tenantId));
    }
}
