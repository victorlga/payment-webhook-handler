FROM clojure:openjdk-17-tools-deps

# Install curl (and leiningen)
RUN apt-get update && apt-get install -y curl \
  && curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein \
  && chmod +x /usr/local/bin/lein \
  && lein -v

# Set workdir
WORKDIR /app

# Copy project files
COPY . /app

# Preload dependencies
RUN lein deps

# Start server
CMD ["lein", "run"]
