package org.magnidei.pic.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
/**
 * 
 * @author Leonhart Lu
 *
 */
public class WebPictureParser implements Runnable {

	private static Logger logger = LogManager.getLogger(WebPictureParser.class);
	private String targetURL;
	private String savePosition;
	
	public static void main(String[] args) {
		new WebPictureParser(args[0], args[1]).run();
	}
	
	public WebPictureParser(String targetURL, String savePosition) {
		this.targetURL = targetURL;
		this.savePosition = savePosition;
	}
	
	private Set<String> getImgUris() throws IOException {
		Document doc = Jsoup.connect(targetURL).get();
		Elements imgElements = doc.select("img");
		Set<String> imgUris = new HashSet<>();
		for (Element element : imgElements) {
			String imgUri = element.attr("src");
			String lowerCaseImgUri = imgUri.toLowerCase();
			if (lowerCaseImgUri.contains("http") && (lowerCaseImgUri.contains(".jpg") || lowerCaseImgUri.contains(".gif") || lowerCaseImgUri.contains(".png"))) {
				imgUris.add(imgUri);
			}
		}
		return imgUris;
	}

	@Override
	public void run() {
		ExecutorService executors = Executors.newCachedThreadPool();
		try {
			logger.info("GET: " + targetURL);
			Set<String> imgUris = getImgUris();
			logger.info("# of image URIs: " + imgUris.size());
			for (String imgUri : imgUris) {
				executors.execute(new AsyncWebImgGetter(imgUri, savePosition));
			}
			executors.shutdown();
			executors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			logger.info("------ Program Terminated Successfully ------");
		}
	}
	
	private class AsyncWebImgGetter implements Runnable {
		
		private String imgUri;
		private String savePos;
		
		public AsyncWebImgGetter(String imgUri, String savePos) {
			this.imgUri = imgUri;
			this.savePos = savePos;
		}

		@Override
		public void run(){
			logger.info("GET: " + imgUri);
			try (CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();) {
				httpclient.start();
				Future<HttpResponse> future = httpclient.execute(new HttpGet(imgUri), null);
				HttpResponse response = future.get();
				HttpEntity entity = response.getEntity();
				FileUtils.copyInputStreamToFile(entity.getContent(), new File(getImgFileName()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		private String getImgFileName() {
			String filename = "";
			String lowerCaseImgUri = imgUri.toLowerCase();
			if (lowerCaseImgUri.contains(".jpg")) {
				filename = imgUri.substring(imgUri.lastIndexOf("/"), imgUri.lastIndexOf(".jpg") + 4);
			} else if (lowerCaseImgUri.contains(".gif")) {
				filename = imgUri.substring(imgUri.lastIndexOf("/"), imgUri.lastIndexOf(".gif") + 4);
			} else {
				filename = imgUri.substring(imgUri.lastIndexOf("/"), imgUri.lastIndexOf(".png") + 4);
			}
			filename = filename.replaceAll("[\\/:*?\"<>|]", "_");
			return savePos + filename;
		}
	}
}
