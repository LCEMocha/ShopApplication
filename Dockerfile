# 베이스 이미지를 Java 17을 포함한 이미지로 설정
FROM maven:3.8.4-openjdk-17 as build

# 작업 디렉토리 설정
WORKDIR /app

# 현재 디렉토리의 모든 파일을 컨테이너의 /app 디렉토리로 복사
COPY . .

# Maven을 사용하여 패키지 빌드
RUN mvn package -DskipTests

# 실행 이미지
FROM openjdk:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일을 복사
COPY --from=build /app/target/shop-0.0.1-SNAPSHOT.jar /app/shop-application.jar

# 애플리케이션 실행
CMD ["java", "-jar", "shop-application.jar"]