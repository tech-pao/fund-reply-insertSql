package org.example.fund_reply_insertSql.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.annotation.PreDestroy;
import org.example.fund_reply_insertSql.entity.AutoColl;
import org.example.fund_reply_insertSql.mapper.AutoCollMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AutoCollImportService {

	private static final Charset LOG_FILE_CHARSET = Charset.forName("GBK");
	private static final String REQUEST_MARKER = "通道请求报文：";
	private static final Pattern SERVICE_CODE_PATTERN = Pattern.compile("<ServiceCode>(.*?)</ServiceCode>", Pattern.DOTALL);
	private static final Pattern SERVICE_SCENE_PATTERN = Pattern.compile("<ServiceScene>(.*?)</ServiceScene>", Pattern.DOTALL);
	private static final Pattern CONSUMER_SEQ_NO_PATTERN = Pattern.compile("<ConsumerSeqNo>(.*?)</ConsumerSeqNo>",
			Pattern.DOTALL);
	private static final Pattern TRANS_CODE_PATTERN = Pattern.compile("<TransCode>(.*?)</TransCode>", Pattern.DOTALL);
	private static final Pattern XML_PAYLOAD_PATTERN = Pattern.compile("(<\\?xml[^>]*\\?>.*?</service>)",
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final DateTimeFormatter COLL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private final AutoCollMapper autoCollMapper;
	private final ExecutorService importExecutor;
	private final int maxRecordsPerFile;

	public AutoCollImportService(AutoCollMapper autoCollMapper,
			@Value("${autocoll.import.threads:8}") int importThreads,
			@Value("${autocoll.import.max-records-per-file:5000}") int maxRecordsPerFile) {
		this.autoCollMapper = autoCollMapper;
		int threadCount = Math.max(1, importThreads);
		this.importExecutor = Executors.newFixedThreadPool(threadCount);
		this.maxRecordsPerFile = Math.max(1, maxRecordsPerFile);
	}

	public ImportSummary importFromLogFile(MultipartFile file) {
		String content = readText(file);
		List<String> payloadList = extractRequestPayloads(content);
		if (payloadList.isEmpty()) {
			throw new IllegalArgumentException("日志中未找到有效通道请求报文");
		}
		if (payloadList.size() > maxRecordsPerFile) {
			throw new IllegalArgumentException("单次最大处理条数为" + maxRecordsPerFile + "，当前文件为" + payloadList.size() + "条，请拆分后重试");
		}

		List<Future<AutoColl>> futures = new ArrayList<>(payloadList.size());
		for (String payload : payloadList) {
			futures.add(importExecutor.submit(() -> insertOne(payload)));
		}

		int success = 0;
		int failed = 0;
		List<String> pkidList = new ArrayList<>();
		Map<Integer, String> errorMap = new LinkedHashMap<>();

		for (int i = 0; i < futures.size(); i++) {
			try {
				AutoColl inserted = futures.get(i).get();
				success++;
				pkidList.add(inserted.getPkid());
			} catch (Exception ex) {
				failed++;
				errorMap.put(i + 1, rootMessage(ex));
			}
		}

		return new ImportSummary(payloadList.size(), success, failed, pkidList, errorMap);
	}

	private AutoColl insertOne(String collreq) {
		String pkid = extractTagValue(collreq, CONSUMER_SEQ_NO_PATTERN);
		String serviceCode = extractTagValue(collreq, SERVICE_CODE_PATTERN);
		String serviceScene = extractTagValue(collreq, SERVICE_SCENE_PATTERN);
		String method = extractTagValue(collreq, TRANS_CODE_PATTERN);

		if (pkid == null || pkid.isBlank()) {
			throw new IllegalArgumentException("报文缺少ConsumerSeqNo，无法作为PKID入库");
		}

		AutoColl autoColl = new AutoColl();
		autoColl.setPkid(pkid);
		autoColl.setCollreq(collreq);
		autoColl.setTraceid(" ");
		autoColl.setTxncode(nullToEmpty(serviceCode) + nullToEmpty(serviceScene));
		autoColl.setMethod(method);
		autoColl.setColltime(LocalDateTime.now().format(COLL_TIME_FORMATTER));
		autoColl.setServicename(" ");
		autoCollMapper.insert(autoColl);
		return autoColl;
	}

	private String readText(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("上传文件不能为空");
		}

		try {
			return new String(file.getBytes(), LOG_FILE_CHARSET);
		} catch (IOException ex) {
			throw new IllegalArgumentException("读取文件失败", ex);
		}
	}

	private List<String> extractRequestPayloads(String text) {
		List<Integer> markerIndexes = new ArrayList<>();
		int from = 0;
		while (true) {
			int idx = text.indexOf(REQUEST_MARKER, from);
			if (idx < 0) {
				break;
			}
			markerIndexes.add(idx);
			from = idx + REQUEST_MARKER.length();
		}

		List<String> payloadList = new ArrayList<>();
		for (int i = 0; i < markerIndexes.size(); i++) {
			int start = markerIndexes.get(i) + REQUEST_MARKER.length();
			int end = (i + 1 < markerIndexes.size()) ? markerIndexes.get(i + 1) : text.length();
			String section = text.substring(start, end);
			String payload = extractXmlPayload(section);
			if (!payload.isEmpty()) {
				payloadList.add(payload);
			}
		}

		return payloadList;
	}

	private String extractXmlPayload(String section) {
		Matcher xmlMatcher = XML_PAYLOAD_PATTERN.matcher(section);
		if (xmlMatcher.find()) {
			return xmlMatcher.group(1).trim();
		}

		return section.trim();
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

	private String rootMessage(Exception ex) {
		Throwable cause = ex;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return message == null ? "未知错误" : message;
	}

	@PreDestroy
	public void shutdownExecutor() {
		importExecutor.shutdown();
	}

	public record ImportSummary(int total, int success, int failed, List<String> pkids,
			Map<Integer, String> errors) {
	}
}