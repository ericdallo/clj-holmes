FROM ubuntu:16.04

RUN apt-get update && apt-get upgrade -y
COPY clj-holmes-ubuntu-latest /bin/clj-holmes

RUN chmod +x /bin/clj-holmes
