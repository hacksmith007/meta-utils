# Use a small base image
FROM alpine:latest

# Add a simple shell script that will run in the container
COPY entrypoint.sh /entrypoint.sh

# Make sure it's executable
RUN chmod +x /entrypoint.sh

# Set the default command
CMD ["/entrypoint.sh"]