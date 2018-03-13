package openagent.api;

import java.awt.image.BufferedImage;

public class FilenameImage {
	
	private String filename;
	
	private BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	
	public FilenameImage(BufferedImage img, String filename) {
		this.img = img;
		this.filename = filename;
	}
	
	public void setFilename(String filename) { this.filename = filename; }
	
	public String getFilename() { return filename; }
	
	public void setImage(BufferedImage img) { this.img = img; }
	
	public BufferedImage getImage() { return img; }

}
