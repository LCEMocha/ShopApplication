package com.shop.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

@Service
@Log
public class FileService {

    public String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception{
        // UUID는 서로 다른 개체들을 구별하기 위해서 이름을 부여한다. 파일의 이름으로 사용하면 중복문제 예방
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        //UUID로 받은 값과 원래 파일 이름의 확장자를 조합해서 저장될 파일 이름을 만든다.
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        // FileOutputStream은 바이트 단위의 출력을 내보내는 클래스이다.
        // 생성자로 파일의 저장위치와 파일명을 넘겨 파일 출력 스트림(데이터의 흐름)을 만든다.
        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);  // fileData를 파일 출력 스트림에 입력
        fos.close();
        return savedFileName;  // 업로드된 파일이름 반환
    }

    public void deleteFile(String filePath) throws Exception{
        File deleteFile = new File(filePath);  // 파일 저장 경로를 이용하여 파일 객체 생성
        if(deleteFile.exists()) {  // 해당 파일이 존재하면 파일을 삭제한다.
            deleteFile.delete();
            log.info("파일을 삭제하였습니다.");
        } else {
            log.info("파일이 존재하지 않습니다.");
        }
    }

}