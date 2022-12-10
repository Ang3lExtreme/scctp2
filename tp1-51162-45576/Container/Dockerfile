FROM tomcat:10.1.2-jdk17
WORKDIR /usr/local/tomcat
ADD scc2223-tp1-1.0.war webapps
EXPOSE 8080
ENV COSMOSDB_URL="https://scc2345576.documents.azure.com:443/"
ENV COSMOSDB_KEY="CNZeJSeeFbVncj5xseYeFOznkT2xxNT7q16hBsFSlCoiWl2fT2vBocu4PFN2wGBaCEjU24JSj4kZACDbOqWXUg=="
ENV COSMOSDB_DATABASE="scc23db45576"
ENV REDIS_URL="azure-redis"