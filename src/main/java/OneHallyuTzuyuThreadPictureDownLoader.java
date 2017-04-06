import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.magnidei.pic.util.WebPictureParser;

/**
 * 
 * @author Leonhart Lu
 *
 */
public class OneHallyuTzuyuThreadPictureDownLoader implements Runnable {

	private static Logger logger = LogManager.getLogger(OneHallyuTzuyuThreadPictureDownLoader.class);
	private String tzuyuThreadHeadPageURL;
	private String savePath;
	
	public static void main(String[] args) {
		new OneHallyuTzuyuThreadPictureDownLoader("https://onehallyu.com/topic/166992-%E3%80%90official-thread-of-chou-tzuyu%E3%80%91-%E1%83%A6knocknock-%E1%83%A6standbyyu-%E1%83%A6anotherdayanotherslay/", "C:/Users/User/Desktop/Tzuyu/").run();
	}
	
	public OneHallyuTzuyuThreadPictureDownLoader(String tzuyuThreadHeadPageURL, String savePath) {
		this.tzuyuThreadHeadPageURL = tzuyuThreadHeadPageURL;
		this.savePath = savePath;
	}

	@Override
	public void run() {
		try {
			downLoadTzuyuPics(getPageNumAsStr());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			logger.info("------ Enjoy Our Savage Maknae ^_^ ------");
		}
	}

	private void downLoadTzuyuPics(String pageNumStr) throws InterruptedException {
		int pageNum = Integer.parseInt(pageNumStr);
		ExecutorService executor = Executors.newSingleThreadExecutor();
		for (int i = 0; i < pageNum; i++) {
			String picURL = "";
			String savePathByPageNum = savePath + String.valueOf(i+1) + "/";
			if (i == 0) {
				picURL = tzuyuThreadHeadPageURL;
			} else {
				picURL = tzuyuThreadHeadPageURL + "page-" + String.valueOf(i+1);
			}
			executor.execute(new WebPictureParser(picURL, savePathByPageNum));
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	private String getPageNumAsStr() throws IOException {
		Document headPage = Jsoup.connect(tzuyuThreadHeadPageURL).get();
		String pageNumInfo = headPage.select("li.pagejump > a").first().html();
		String pageNumStr = pageNumInfo.substring(pageNumInfo.lastIndexOf(" ")+1);
		return pageNumStr;
	}
	

}
