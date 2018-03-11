package com.gnerv.filemanager.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gnerv.filemanager.common.FileManagerR;
import com.gnerv.filemanager.model.FileManagerModel;
import com.gnerv.filemanager.service.impl.FileManagerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value = "文件上传接口", consumes = "文件上传接口")
public class FileManagerController {
	
	@Autowired
	FileManagerService fileManagerService;

	@ApiOperation(value="单个文件上传接口", notes="用于上传单个大小小于1M的文件")
	@PostMapping(value="/upLoadFileForSingle")
	private FileManagerR upLoadFileForSingle(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return FileManagerR.error("no file! please choose a file!");
		}
		if (file.getSize() / 1024 / 1024 > 1) {
			return FileManagerR.error("file size is too big! must be less than 1M!");
		}
		FileManagerModel fileManager = fileManagerService.createAndSaveFile(file);
		return FileManagerR.ok("true").put("fileManager", fileManager);
	}

	@ApiOperation(value="多个上传文件接口", notes="用于上传单个大小小于1M，总大小小于10M的文件")
	@PostMapping(value="/upLoadFileForMultiple")
	private FileManagerR upLoadFileForMultiple(@RequestParam("files") MultipartFile[] files) {
		long size = 0;
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				return FileManagerR.error("no file! please choose a file!");
			}
			if (file.getSize() / 1024 / 1024 > 1) {
				return FileManagerR.error("file size is too big! must be less than 1M!");
			}
			size += file.getSize();
		}
		if (size / 1024 / 1024 > 10) {
			return FileManagerR.error("files size is too big! must be less than 10M!");
		}
		List<FileManagerModel> fileManagers = new ArrayList<FileManagerModel>();

		for (MultipartFile file : files) {
			FileManagerModel fileManager = fileManagerService.createAndSaveFile(file);
			fileManagers.add(fileManager);
		}
		return FileManagerR.ok("true").put("fileManagers", fileManagers);
	}
	
	@ApiOperation(value="下载文件接口", notes="通过文件UUID值（唯一）下载文件")
	@GetMapping(value="/downLoadFileForSingle")
	private FileManagerR downLoadFileForSingle(@RequestParam("uuid") String uuid, HttpServletResponse response) {
		try {
			boolean checkFile = fileManagerService.checkFile(uuid);//检查文件是否存在
			if(!checkFile) {
				return FileManagerR.error("no file!");
			}
			File fileByUuid = fileManagerService.getFileByUuid(uuid);//获取文件
			response.setContentType("application/octet-stream");//设置文件为直接下载 不可在线打开
			String filenameByUuid = fileManagerService.getFilenameByUuid(uuid);//获取文件原始名称
			String fileName = URLEncoder.encode(filenameByUuid, "UTF-8");//设置文件名和编码
			response.setHeader( "Content-Disposition", "attachment;filename=" +fileName);
			FileCopyUtils.copy(new FileInputStream(fileByUuid), response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return FileManagerR.ok("false");
		}
		return FileManagerR.ok("false");
	}
	
}
