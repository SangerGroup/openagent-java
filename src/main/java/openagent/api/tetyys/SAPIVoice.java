package openagent.api.tetyys;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.embed.swing.JFXPanel;

public class SAPIVoice {
	
	private String name;
	private int pitch = 0;
	private int speed = 0;
	private String previousMsg = ""; // Save download speed if message is the same
	private Media reference;
	private MediaPlayer player;
	
	public SAPIVoice(String name, int pitch, int speed) {
		this.name = name;
		this.pitch = pitch;
		this.speed = speed;
	}
	
	public static SAPIVoice bonziInstance() {
		return new SAPIVoice("Adult Male #2, American English (TruVoice)", 140, 157);
	}
	
	public static void main(String[] args) {
		SAPIVoice peedyVoice = bonziInstance();
		peedyVoice.speak("Testing testing");
	}
	
	private double timeout = 0;
	
	public double speak(String msg) {
		String url = "https://tetyys.com/SAPI4/SAPI4?text=" + EncodingUtil.encodeURIComponent(msg) +
					"&voice=" + EncodingUtil.encodeURIComponent(name) + "&pitch=" + pitch + "&speed=" + speed;
		System.out.println(url);
		new JFXPanel();
		String file = Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "sapi.wav";
		if(!previousMsg.equals(msg)) {
			// Download file
			try { 
				ReadableByteChannel rbc;
				URL website = new URL(url);
				rbc = Channels.newChannel(website.openStream());
				FileOutputStream fos = new FileOutputStream(file);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			} catch (IOException e) {
				throw new AgentTTSException("Couldn't connect to TETYYS server.", e);
			}
		}
		reference = new Media(new File(file).toURI().toString());
		player = new MediaPlayer(reference);
	
		player.setOnReady(() -> { 
			timeout = reference.getDuration().toMillis();
		});
		player.play();
		previousMsg = msg;
		return timeout;
	}
	
	public void stopSpeaking() {
		player.stop();
	}
	
	/**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @return The filename
     * @throws IOException
     */
    public static String downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
        String fileName = "";
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
 
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
 
            outputStream.close();
            inputStream.close();
 
            System.out.println("File downloaded");
        } else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
        return fileName;
    }

	/**
	 * Utility class for JavaScript compatible UTF-8 encoding and decoding.
	 * 
	 * @see http://stackoverflow.com/questions/607176/java-equivalent-to-javascripts-encodeuricomponent-that-produces-identical-output
	 * @author John Topley 
	 */
	static class EncodingUtil
	{
	  /**
	   * Decodes the passed UTF-8 String using an algorithm that's compatible with
	   * JavaScript's <code>decodeURIComponent</code> function. Returns
	   * <code>null</code> if the String is <code>null</code>.
	   *
	   * @param s The UTF-8 encoded String to be decoded
	   * @return the decoded String
	   */
	  public static String decodeURIComponent(String s)
	  {
	    if (s == null)
	    {
	      return null;
	    }

	    String result = null;

	    try
	    {
	      result = URLDecoder.decode(s, "UTF-8");
	    }

	    // This exception should never occur.
	    catch (UnsupportedEncodingException e)
	    {
	      result = s;  
	    }

	    return result;
	  }

	  /**
	   * Encodes the passed String as UTF-8 using an algorithm that's compatible
	   * with JavaScript's <code>encodeURIComponent</code> function. Returns
	   * <code>null</code> if the String is <code>null</code>.
	   * 
	   * @param s The String to be encoded
	   * @return the encoded String
	   */
	  public static String encodeURIComponent(String s)
	  {
	    String result = null;

	    try
	    {
	      result = URLEncoder.encode(s, "UTF-8")
	                         .replaceAll("\\+", "%20")
	                         .replaceAll("\\%21", "!")
	                         .replaceAll("\\%27", "'")
	                         .replaceAll("\\%28", "(")
	                         .replaceAll("\\%29", ")")
	                         .replaceAll("\\%7E", "~");
	    }

	    // This exception should never occur.
	    catch (UnsupportedEncodingException e)
	    {
	      result = s;
	    }

	    return result;
	  }  

	  /**
	   * Private constructor to prevent this class from being instantiated.
	   */
	  private EncodingUtil()
	  {
	    super();
	  }
	}

}
