package org.example.fund_reply_insertSql.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.example.fund_reply_insertSql.service.AutoCollImportService;
import org.example.fund_reply_insertSql.service.AutoCollImportService.ImportSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/autocoll")
public class AutoCollImportController {

	private final AutoCollImportService autoCollImportService;

	public AutoCollImportController(AutoCollImportService autoCollImportService) {
		this.autoCollImportService = autoCollImportService;
	}

	@PostMapping("/import-log")
	public ResponseEntity<Map<String, Object>> importLog(@RequestParam("file") MultipartFile file) {
		try {
			ImportSummary summary = autoCollImportService.importFromLogFile(file);
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("success", true);
			result.put("total", summary.total());
			result.put("successCount", summary.success());
			result.put("failedCount", summary.failed());
			result.put("pkids", summary.pkids());
			result.put("errors", summary.errors());
			return ResponseEntity.ok(result);
		} catch (IllegalArgumentException ex) {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("success", false);
			result.put("message", ex.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
		}
	}
}