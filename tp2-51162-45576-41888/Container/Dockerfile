FROM tomcat:10.1.2-jdk17
WORKDIR /usr/local/tomcat
ADD scc2223-tp2-1.0.war webapps
EXPOSE 8080
FROM openjdk:17-alpine
COPY ./scc2223-tp2-1.0-jar-with-dependencies.jar /home/
WORKDIR /home/
CMD ["java", "-cp", "scc2223-tp2-1.0-jar-with-dependencies.jar", "scc.Main.Main"]
ENV COSMOSDB_URL="https://scc2345576.documents.azure.com:443/"
ENV COSMOSDB_KEY="CNZeJSeeFbVncj5xseYeFOznkT2xxNT7q16hBsFSlCoiWl2fT2vBocu4PFN2wGBaCEjU24JSj4kZACDbOqWXUg=="
ENV COSMOSDB_DATABASE="scc23db45576"
