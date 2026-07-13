package org.example.fund_reply_insertSql.service;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.fund_reply_insertSql.entity.AutoColl;
import org.example.fund_reply_insertSql.mapper.AutoCollMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AutoCollImportService {

	private static final String REQUEST_MARKER = "通道请求报文：";
	private static final Pattern SERVICE_CODE_PATTERN = Pattern.compile("<ServiceCode>(.*?)</ServiceCode>", Pattern.DOTALL);
	private static final Pattern SERVICE_SCENE_PATTERN = Pattern.compile("<ServiceScene>(.*?)</ServiceScene>", Pattern.DOTALL);
	private static final Pattern TRANS_CODE_PATTERN = Pattern.compile("<TransCode>(.*?)</TransCode>", Pattern.DOTALL);
	private static final Pattern TRAN_CODE_PATTERN = Pattern.compile("<TranCode>(.*?)</TranCode>", Pattern.DOTALL);

	private final AutoCollMapper autoCollMapper;

	public AutoCollImportService(AutoCollMapper autoCollMapper) {
		this.autoCollMapper = autoCollMapper;
	}

	public AutoColl importFromLogFile(MultipartFile file) {
		String content = readText(file);
		String collreq = extractRequestPayload(content);
		String serviceCode = extractTagValue(collreq, SERVICE_CODE_PATTERN);
		String serviceScene = extractTagValue(collreq, SERVICE_SCENE_PATTERN);
		String method = extractTagValue(collreq, TRANS_CODE_PATTERN);

		if (method == null) {
			method = extractTagValue(collreq, TRAN_CODE_PATTERN);
		}

		AutoColl autoColl = new AutoColl();
		autoColl.setCollreq(collreq);
		autoColl.setTxncode(nullToEmpty(serviceCode) + nullToEmpty(serviceScene));
		autoColl.setMethod(method);

		autoCollMapper.insert(autoColl);
		return autoColl;
	}

	private String readText(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("上传文件不能为空");
		}

		try {
			return new String(file.getBytes(), StandardCharsets.UTF_8);
		} catch (Exception ex) {
			throw new IllegalArgumentException("读取文件失败", ex);
		}
	}

	private String extractRequestPayload(String text) {
		int markerIndex = text.indexOf(REQUEST_MARKER);
		if (markerIndex < 0) {
			throw new IllegalArgumentException("日志中未找到“通道请求报文：”标记");
		}

		String payload = text.substring(markerIndex + REQUEST_MARKER.length()).trim();
		if (payload.isEmpty()) {
			throw new IllegalArgumentException("通道请求报文内容为空");
		}
		return payload;
	}

	private String extractTagValue(String xml, Pattern pattern) {
		Matcher matcher = pattern.matcher(xml);
		if (!matcher.find()) {
			return null;
		}
		return matcher.group(1).trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}