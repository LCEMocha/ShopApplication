FROM openjdk:17-jdk

# 컨테이너 내에서 사용할 작업 디렉토리 설정
WORKDIR /app

# Maven 빌드로 생성된 JAR 파일을 이미지에 복사
COPY target/shop-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 시작 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "app.jar"]