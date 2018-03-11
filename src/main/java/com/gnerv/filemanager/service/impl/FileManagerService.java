package com.gnerv.filemanager.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.gnerv.filemanager.model.FileManagerModel;
import com.gnerv.filemanager.service.IFileManager;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

@Service
public class FileManagerService implements IFileManager {

	private String filePathPrefix = "D:\\Users\\legend\\demo\\";
	
	@Override
	public FileManagerModel createAndSaveFile(MultipartFile file) {
		FileManagerModel fileManagerModel = new FileManagerModel();
		try {
			String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			String md5DigestAsHex = DigestUtils.md5DigestAsHex(file.getBytes());
			String path = filePathPrefix + uuid;
			File filePath = new File(path);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			File fileObj = new File(filePath, uuid);
			Files.write(file.getBytes(), fileObj);
			
			fileManagerModel.setUuid(uuid);
			fileManagerModel.setName(file.getOriginalFilename());
			fileManagerModel.setPath(path);
			fileManagerModel.setMd5(md5DigestAsHex);
			
			Gson gson = new Gson();
			String json = gson.toJson(fileManagerModel);
			fileObj = new File(filePath, "fileManagerModel");
			Files.write(json.getBytes(), fileObj);
			
			return fileManagerModel;
		} catch (Exception e) {
		}
		return fileManagerModel;
	}

	@Override
	public File getFileByUuid(String uuid) {
		String path = filePathPrefix + uuid;
		try {
			File fileObj = new File(path, "fileManagerModel");
			if(fileObj.exists()) {
				List<String> readLines = Files.readLines(fileObj, Charsets.UTF_8);
				Gson gson = new Gson();
				FileManagerModel fromJson = gson.fromJson(readLines.get(0), FileManagerModel.class);
				String path2 = fromJson.getPath();
				File file = new File(path2, uuid);
				return file;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	@Override
	public boolean checkFile(String uuid) {
		String path = filePathPrefix + uuid;
		File fileManagerModel = new File(path, "fileManagerModel");
		File fileManagerUuid = new File(path, uuid);
		if(fileManagerUuid.exists() && fileManagerUuid.isFile() && fileManagerModel.exists() && fileManagerModel.isFile()) {
			return true;
		}else {
			return false;
		}
	}

	@Override
	public String getFilenameByUuid(String uuid) {
		String path = filePathPrefix + uuid;
		try {
			File fileObj = new File(path, "fileManagerModel");
			if(fileObj.exists()) {
				List<String> readLines = Files.readLines(fileObj, Charsets.UTF_8);
				Gson gson = new Gson();
				FileManagerModel fromJson = gson.fromJson(readLines.get(0), FileManagerModel.class);
				return fromJson.getName();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		return "";
	}

}
