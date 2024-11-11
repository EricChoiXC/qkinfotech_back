package com.qkinfotech.core.file;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.aspose.cells.HtmlSaveOptions;
import com.aspose.cells.Workbook;
import com.aspose.slides.*;
import com.aspose.words.Document;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.mvc.SimpleResult;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.storage.StorageManager;
import com.qkinfotech.core.storage.SysStorageData;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/file")
@Slf4j
public class FileTransferController implements DisposableBean, InitializingBean {
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;

	@Autowired
	protected HttpServletRequest request;

	@Autowired
	protected HttpServletResponse response;

	@Autowired
	protected SimpleResult result;

	@Autowired
	protected StorageManager storageManager;
	
	@Autowired
	protected FileManager fileManager;
	
	@Autowired
	protected SimpleService<SysFileSlice> sysFileSliceService;

	@Autowired
	protected SimpleService<SysFile> sysFileService;

	@Autowired
	protected SimpleService<SysStorageData> sysStorageDataService;
	
	protected StreamTransferDispatcher std = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		std = new StreamTransferDispatcher();
	}

	@Override
	public void destroy() throws Exception {
		std.shutdown();
	}

	private void close(Closeable o) {
		if(o != null) {
			try {
				o.close();
			} catch (IOException e) {
				logger.trace(e.getMessage(), e);
			}
		}
	}


	@Getter
	@Setter
	private static class Range {

		private long start;

		private long end;

		public Range(long start, long end) {
			this.start = start;
			this.end = end;
		}
		
		public long getLength() {
			return end - start + 1; 
		}

	}

	/*
	 * Range:bytes=0-500   
	 * 表示下载从0到500字节的文件，即头500个字节  ,[0-500]前闭后闭。0<=range<=500 
	 *     
	 * Range:bytes=501-1000
	 * 表示下载从500到1000这部分的文件，单位字节       
	 * 
	 * Range:bytes=-500
	 * 表示下载最后的500个字节      
	 *      
	 * Range:bytes=500-
	 * 表示下载从500开始到文件结束这部分的内容   
	 *  
	 * Range:bytes=500-600,700-1000
	 * 表示下载这两个区间的内容
	 * 多 Range 返回的 Content-Type 和 Content-Length 不一样 
	 * Content-Type: multipart/byteranges; boundary=CATALINA_MIME_BOUNDARY
	 * Content-Length: <file-size>
	 */
	private Range range(long filesize) {

		String range = request.getHeader("Range");
		if (!StringUtils.hasText(range)) {
			return null;
		}
		range = range.trim().replace(" ", "");
		if(!range.startsWith("bytes=")) {
			return null;
		}
		range = range.substring(6);
		
		Range result = new Range(filesize - 1, 0);

		String[] ranges = StringUtils.tokenizeToStringArray(range, ",");

		for (String r : ranges) {
			int p = r.indexOf('-');
			if (p < 0) {
				throw new RuntimeException("range definition error:" + range);
			}
			String start = r.substring(0, p).trim();
			String end = r.substring(p + 1).trim();
			long iStart = 0;
			long iEnd = filesize;
			if (StringUtils.hasText(start)) {
				iStart = Long.parseLong(start);
			}
			if (StringUtils.hasText(end)) {
				iEnd = Long.parseLong(end);
				if (!StringUtils.hasText(start)) {
					iStart = filesize - iEnd;
					iEnd = filesize - 1;
				}
			}

			if (iEnd >= filesize) {
				iEnd = filesize - 1;
			}

			if (iStart < result.getStart()) {
				result.setStart(iStart);
			}
			if (iEnd > result.getEnd()) {
				result.setEnd(iEnd);
			}
		}
		if (result.getStart() > result.getEnd() || result.getStart() < 0 || result.getEnd() >= filesize) {
			return null;
		}
		return result;
	}

	@GetMapping("/openPptToPdf")
	public ResponseEntity<ByteArrayResource> openPptToPdf() throws Exception {
		String fId = request.getParameter("fId");
		if (!StringUtils.hasText(fId)) {
			throw new IllegalArgumentException("fId is empty");
		}

		SysFile sysFile = fileManager.getSysFile(fId);
		if(sysFile == null) {
			throw new IllegalArgumentException("fId is not found");
		}

		Range range = null;
		long filesize = sysFile.getfSize();
		String filename = sysFile.getfFileName();
		try {
			range = range(filesize);
		} catch (Exception e) {
			range = null;
		}
		response.setContentType(sysFile.getfMimeType());
		if (range != null && range.getLength() < sysFile.getfSize()) {
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + filesize);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			range = new Range(0, filesize - 1);
		}
		long length = range.getLength();
		//response.setHeader("Content-Length", "" + length);

		//OutputStream outputStream = response.getOutputStream();
		ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
		//InputStream inputStream = fileManager.getInputStream(fId, range.getStart(), range.getEnd()+1);
		InputStream inputStream = fileManager.getInputStream(fId);

		try {
			Presentation presentation = new Presentation(inputStream);
			HtmlOptions htmlOptions = new HtmlOptions();
			htmlOptions.setHtmlFormatter(HtmlFormatter.createDocumentFormatter("", false));
			//presentation.save(outputStream, SaveFormat.Html, htmlOptions);
			//presentation.save(outputStream1, SaveFormat.Html, htmlOptions);

			PdfOptions pdfOptions = new PdfOptions();
			presentation.save(outputStream1, SaveFormat.Pdf, pdfOptions);

			//outputStream.flush();
			//outputStream.close();
			outputStream1.close();
			inputStream.close();

			byte[] fileContent = outputStream1.toByteArray();
			ByteArrayResource resource = new ByteArrayResource(fileContent);

			// 设置响应头
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
			headers.setContentLength(fileContent.length);
			headers.set("Content-Disposition", "inline; filename=output.html");
			headers.set("X-Frame-Options", "SAMEORIGIN");
			headers.set("Content-Security-Policy", "frame-ancestors 'self'");

			// 返回ResponseEntity，让浏览器展示HTML内容
			return ResponseEntity.ok()
					.headers(headers)
					.body(resource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	@GetMapping("/openXlsToHtml")
	public ResponseEntity<ByteArrayResource> openXlsToHtml() throws Exception {
//		com.aspose.cells.License license = new com.aspose.cells.License();
//		license.setLicense("/home/qks/license/aspose.lic");

		String fId = request.getParameter("fId");
		if (!StringUtils.hasText(fId)) {
			throw new IllegalArgumentException("fId is empty");
		}

		SysFile sysFile = fileManager.getSysFile(fId);
		if(sysFile == null) {
			throw new IllegalArgumentException("fId is not found");
		}

		Range range = null;
		long filesize = sysFile.getfSize();
		String filename = sysFile.getfFileName();
		try {
			range = range(filesize);
		} catch (Exception e) {
			range = null;
		}
		response.setContentType(sysFile.getfMimeType());
		if (range != null && range.getLength() < sysFile.getfSize()) {
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + filesize);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			range = new Range(0, filesize - 1);
		}
		long length = range.getLength();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = fileManager.getInputStream(fId, range.getStart(), range.getEnd()+1);

		try {
			Workbook wb = new Workbook(inputStream);
			HtmlSaveOptions saveOptions = new HtmlSaveOptions();
			saveOptions.setParseHtmlTagInCell(true);
			//wb.save(outputStream, saveOptions);

			com.aspose.cells.PdfSaveOptions pdfOptions = new com.aspose.cells.PdfSaveOptions();
			wb.save(outputStream, pdfOptions);

			outputStream.close();
			inputStream.close();

			byte[] fileContent = outputStream.toByteArray();
			ByteArrayResource resource = new ByteArrayResource(fileContent);

			// 设置响应头
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
			headers.setContentLength(fileContent.length);
			headers.set("Content-Disposition", "inline; filename=output.html");
			headers.set("X-Frame-Options", "SAMEORIGIN");
			headers.set("Content-Security-Policy", "frame-ancestors 'self'");

			// 返回ResponseEntity，让浏览器展示HTML内容
			return ResponseEntity.ok()
					.headers(headers)
					.body(resource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	@GetMapping("/openDocToHtml")
	public ResponseEntity<ByteArrayResource> openDocToHtml() throws Exception {
//		com.aspose.words.License license = new com.aspose.words.License();
//		license.setLicense("/home/qks/license/aspose.lic");

		String fId = request.getParameter("fId");
		if (!StringUtils.hasText(fId)) {
			throw new IllegalArgumentException("fId is empty");
		}

		SysFile sysFile = fileManager.getSysFile(fId);
		if(sysFile == null) {
			throw new IllegalArgumentException("fId is not found");
		}

		Range range = null;
		long filesize = sysFile.getfSize();
		String filename = sysFile.getfFileName();
		try {
			range = range(filesize);
		} catch (Exception e) {
			range = null;
		}
		response.setContentType(sysFile.getfMimeType());
		if (range != null && range.getLength() < sysFile.getfSize()) {
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + filesize);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			range = new Range(0, filesize - 1);
		}
		long length = range.getLength();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		InputStream inputStream = fileManager.getInputStream(fId);

		try {
			Document document = new Document(inputStream);
			com.aspose.words.HtmlSaveOptions saveOptions = new com.aspose.words.HtmlSaveOptions();
			saveOptions.setSaveFormat(com.aspose.words.SaveFormat.HTML);
			saveOptions.setExportXhtmlTransitional(true);
			saveOptions.setExportImagesAsBase64(true);
			saveOptions.setExportPageSetup(true);
			saveOptions.setTableWidthOutputMode(640);
			//document.save(outputStream, saveOptions);

			com.aspose.words.PdfSaveOptions pdfOptions = new com.aspose.words.PdfSaveOptions();
			pdfOptions.setSaveFormat(com.aspose.words.SaveFormat.PDF);
			pdfOptions.setExportDocumentStructure(true);
			document.save(outputStream, pdfOptions);

			outputStream.close();
			inputStream.close();

			byte[] fileContent = outputStream.toByteArray();
			ByteArrayResource resource = new ByteArrayResource(fileContent);

			// 设置响应头
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.TEXT_HTML);
			headers.setContentLength(fileContent.length);
			headers.set("Content-Disposition", "inline; filename=output.html");
			headers.set("X-Frame-Options", "SAMEORIGIN");
			headers.set("Content-Security-Policy", "frame-ancestors 'self'");

			// 返回ResponseEntity，让浏览器展示HTML内容
			return ResponseEntity.ok()
					.headers(headers)
					.body(resource);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}

	private DeferredResult<ResponseEntity<InputStream>> output(String fId, boolean isOpen) throws Exception {
		if (!StringUtils.hasText(fId)) {
			throw new IllegalArgumentException("fId is empty");
		}

		SysFile sysFile = fileManager.getSysFile(fId);
		if(sysFile == null) {
			throw new IllegalArgumentException("fId is not found");
		}
		
		Range range = null;

		long filesize = sysFile.getfSize();

		String filename = sysFile.getfFileName();

		try {
			range = range(filesize);
		} catch (Exception e) {
			//response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			//return;
			range = null;
		}

		// String etag = request.getHeader("ETag");
		if(isOpen) {
			response.setContentType(sysFile.getfMimeType());
		} else {
			response.setContentType("application/octet-stream");
			response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
		}
		
		if (range != null && range.getLength() < sysFile.getfSize()) {
			response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
			response.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + filesize);
		} else {
			response.setStatus(HttpServletResponse.SC_OK);
			range = new Range(0, filesize - 1);
		}
		long length = range.getLength();
		response.setHeader("Content-Length", "" + length);
		
		OutputStream outputStream = response.getOutputStream();
		InputStream inputStream = fileManager.getInputStream(fId, range.getStart(), range.getEnd()+1);

		if(length < 1024*1024l) {
			FileCopyUtils.copy(inputStream, outputStream);
			return null;
		}
		
		DeferredResult<ResponseEntity<InputStream>> deferredResult = new DeferredResult<ResponseEntity<InputStream>>((long) 1000l * 60 * 10);

		std.addTask(inputStream, outputStream, 0l, range.getLength()).whenComplete((r, e)->{
			close(inputStream);
			try {
				outputStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			deferredResult.setResult(null);
			if(e!= null) {
				logger.error(e.getMessage(), e);
			}
		});
		
		return deferredResult;

	}
	
	/*
	 * 测试：
	 * curl -D "resp-header.txt" -H 'Range: bytes=0-2000' http://localhost:8080/file/download?fId=xxxxxx > /tmp/test.jpg
	 */
	@GetMapping("/download")
	public DeferredResult<ResponseEntity<InputStream>> download() throws Exception {

		String fId = request.getParameter("fId");
		return output(fId, false);
	}

	
	@GetMapping("/open")
	public DeferredResult<ResponseEntity<InputStream>> open() throws Exception {

		String fId = request.getParameter("fId");
		return output(fId, true);
	}


	@GetMapping("/downloadAll")
	public DeferredResult<ResponseEntity<InputStream>> downloadAll() throws Exception {

		String fIdstr = request.getParameter("fIds");
		String[] fIds = fIdstr.split(",");
		
		String filename = request.getParameter("filename");
		if(!StringUtils.hasText(filename)) {
			filename = "all.zip";
		}
		
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
		
		OutputStream outputStream = response.getOutputStream();
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		
		DeferredResult<ResponseEntity<InputStream>> deferredResult = new DeferredResult<ResponseEntity<InputStream>>((long) 1000l * 60 * 10);

		CompletableFuture.supplyAsync(() -> {
			try {
				
				for(String fId : fIds) {
					SysFileInputStream inputStream = fileManager.getInputStream(fId);
					ZipEntry entry = new ZipEntry(inputStream.getSysFile().getfFileName());
			        zipOutputStream.putNextEntry(entry);
			        StreamUtils.copy(inputStream, zipOutputStream);
			        inputStream.close();
			        zipOutputStream.closeEntry();
			        zipOutputStream.flush();
				}
				
				zipOutputStream.finish();
			}catch(Exception e) {
				throw new RuntimeException(e);
			}
			return null;
	    }).whenComplete((r, e) -> {
	    	try {
				outputStream.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			deferredResult.setResult(null);
			if(e!= null) {
				logger.error(e.getMessage(), e);
			}
	    });

		return deferredResult;
	}
	
	
	@RequestMapping("/upload")
	@ResponseBody
	public void upload(MultipartFile file) throws Exception {

		String filename = request.getParameter("file.name");
		if(filename == null) {
			filename = file.getOriginalFilename();
		}
		
		File tempfile = File.createTempFile("~upload", "x");
		file.transferTo(tempfile);
		SysFile sysFile = fileManager.create(filename, tempfile);
		tempfile.delete();
		
		JSONObject obj = new JSONObject();
		obj.put("fId", sysFile.getfId());
		result.from(obj);
	}
	
	@RequestMapping("/checkFileDigest")
	@ResponseBody
	public void checkFileDigest() throws Exception {
		JSONObject obj = new JSONObject();

		String filename = request.getParameter("file.name");
		long fileSize = Long.parseLong(request.getParameter("file.size"));
		String fileDigest = request.getParameter("file.digest");

		SysFile file = fileManager.create(filename, fileSize, fileDigest);
		obj.put("fId", file.getfId());
		obj.put("uploaded", file.getfStatus() == 1);
		result.from(obj);
	}	
	
	@RequestMapping("/checkChunkDigest")
	@ResponseBody
	public void checkChunkDigest() throws Exception {
		JSONObject obj = new JSONObject();
		
		String fileId = request.getParameter("file.id");
		long partStart = Long.parseLong(request.getParameter("part.start"));
		int  partSize = Integer.parseInt(request.getParameter("part.size"));
		String partDigest = request.getParameter("part.digest");

		Specification<SysStorageData> spec = (root, query, cb) -> {
			Predicate predicate = cb.and(
				cb.equal(root.get("fMD5"), partDigest), 
				cb.equal(root.get("fSize"), partSize)
			);
			return query.where(predicate).getRestriction();
		}; 
		List<SysStorageData> files = sysStorageDataService.findAll(spec);
		if(files != null && files.size() == 1) {
			SysStorageData data = files.get(0);
			SysFile sysFile = sysFileService.getById(fileId);
			SysFileSlice sysFileSlice = new SysFileSlice();
			sysFileSlice.setfStart(partStart);
			sysFileSlice.setfSize(partSize);
			sysFileSlice.setfSysFile(sysFile);
			sysFileSlice.setfSysStorageData(data);
			sysFileSliceService.save(sysFileSlice);
			obj.put("id", sysFileSlice.getfId());
			obj.put("exists", true);
			result.from(obj);
		} else {
			obj.put("exists", false);
			result.from(obj);
		}
	}

	@RequestMapping("/uploadChunk")
	@ResponseBody
	public void uploadChunk(MultipartFile file) throws Exception {
		JSONObject obj = new JSONObject();

		String fileName = request.getParameter("file.name");
		String fileId = request.getParameter("file.id");
		long fileSize = Long.parseLong(request.getParameter("file.size"));
		String fileDigest = request.getParameter("file.digest");
		long partIndex = Long.parseLong(request.getParameter("part.index"));
		long partStart = Long.parseLong(request.getParameter("part.start"));
		long partSize = Long.parseLong(request.getParameter("part.size"));
		long partTotal = Long.parseLong(request.getParameter("part.total"));
		String partDigest = request.getParameter("part.digest");
		
		// 根据 file.id 获取 SysFile
		SysFile sysFile = fileManager.getSysFile(fileId);
		if(sysFile == null) {
			throw new IllegalArgumentException("fId is not found");
		}
		
		// 根据 sysFile 获取 SysFileSlice,  fStart <= part.start 倒序， 第一个
		Specification<SysFileSlice> spec = (root, query, cb) -> {
			Predicate predicate = cb.and(
				cb.equal(root.get("fSysFile.fId"), fileId), 
				cb.lessThan(root.get("fStart"), partStart + partSize)
			);
			return query.where(predicate).getRestriction();
		}; 
		SysFileSlice slice = sysFileSliceService.findOne(spec, Sort.by(Order.desc("fStart")));
		// 判断是否重叠
		if(slice != null) {
			if(slice.getfStart() == partStart || slice.getfSize() == partSize) {
				obj.put("finished", sysFile.getfStatus() == 1);obj.put("fId", sysFile.getfId());
				result.from(obj);
				return;
			}
			if(slice.getfStart() >= partStart || slice.getfStart() + slice.getfSize() > partStart) {
				throw new IllegalArgumentException("illegal file part");
			}
		}
		
		sysFileService.lock(sysFile);
		// 写入chunk，写入 fileSlice,判断 是否 finish
		try {
			fileManager.transfer(sysFile, file.getBytes());
			obj.put("finished", sysFile.getfStatus() == 1);
		}catch(SysFileStatusException e) {
			obj.put("finished", true);
		}
		obj.put("fId", sysFile.getfId());
		result.from(obj);
		
	}

	/**
	 *  后端上传附件
	 * @param file
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public JSONObject uploadFile(MultipartFile file,String filename) throws Exception {
		File tempfile = File.createTempFile("~upload", "x");
		file.transferTo(tempfile);
		SysFile sysFile = fileManager.create(filename, tempfile);
		tempfile.delete();
		JSONObject obj = new JSONObject();
		obj.put("fId", sysFile.getfId());
		return obj;
	}
	
	public static void main(String[] args) throws Exception {
		String[] fIds = {"D:\\send\\file\\1.doc", "D:\\send\\file\\2.doc"};
		
		OutputStream outputStream = new FileOutputStream("d:\\send\\file\\all.zip");
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		
		for(String fId : fIds) {
			FileInputStream inputStream = new FileInputStream(fId);
			ZipEntry entry = new ZipEntry(fId);
	        zipOutputStream.putNextEntry(entry);
	        StreamUtils.copy(inputStream, zipOutputStream);
	        inputStream.close();
	        zipOutputStream.closeEntry();
	        zipOutputStream.flush();
		}
		
		zipOutputStream.finish();
		outputStream.flush();
		outputStream.close();
	}

}
