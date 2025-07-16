# Nginx reverse proxy
FROM nginx:alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Remove default nginx configuration
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Create directory for uploads
RUN mkdir -p /usr/share/nginx/html/uploads

# Create non-root user
RUN addgroup -g 1001 -S ecodeli && \
    adduser -S ecodeli -u 1001 -G ecodeli

# Change ownership of nginx directories
RUN chown -R ecodeli:ecodeli /usr/share/nginx/html && \
    chown -R ecodeli:ecodeli /var/cache/nginx && \
    chown -R ecodeli:ecodeli /var/log/nginx && \
    chown -R ecodeli:ecodeli /etc/nginx/conf.d

# Create nginx pid directory
RUN mkdir -p /var/run/nginx && \
    chown -R ecodeli:ecodeli /var/run/nginx

# Expose ports
EXPOSE 80 443

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]