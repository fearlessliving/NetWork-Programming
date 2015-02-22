import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/* My program does not take the key image file as the parameter.
 * My program is going to generate the key(every 4 pixel have 2 black pixels) first, then use this key to encrypt the image
 * after that I use the same key to decrypt the encrypted image to make it readable by human
 */
public class Encrypt {
	
	
	public static void main(String[] args)
	{
	
	    try{
	    	int Height = 480;
			int Width = 640;
	    	//Read the serect image
	    	File originalImageFile =new File("DirectDemocracy.bmp");
	    	BufferedImage originalImage=Tools.read(originalImageFile,Width,Height);
			//generate the key 
			BufferedImage key=Tools.generateKeyImage(Width, Height);
	    	//Encrypt the image using key image
	    	BufferedImage encryptedImage=Tools.encryptImage(originalImage,key);
	    	//Write to the file
	    	File output =new File("encryptedImage.png");
	    	ImageIO.write(encryptedImage, "png", output);
	    	//display the retrived image
	    	BufferedImage combineImage=Tools.combineImage(encryptedImage, key);
	    	//Write to the file
	    	File output2 =new File("retrivedImage.png");
	    	ImageIO.write(combineImage, "png", output2);
	    	
			
		}
	    catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
class Tools {
	
	//read image to be encrypted
	public static BufferedImage read(File originalImageFile, int width, int height) {	
		BufferedImage imgSrcRes =  new BufferedImage(width/2, height/2, BufferedImage.TYPE_INT_ARGB);if (originalImageFile == null) 
			return null;
		BufferedImage imgSrc = null;
		try {
			imgSrc = ImageIO.read(originalImageFile);
			
			// resize the source to the size of the key
			
			Graphics2D g = imgSrcRes.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.drawImage(imgSrc, 0, 0, width/2, height/2, 0, 0, imgSrc.getWidth(), imgSrc.getHeight(), null);
			g.dispose();
			
			
			
		} 
		catch (Exception e) {
			return null;
		}
		return imgSrcRes;
	}
	
	
	//Generate the key image
	public static BufferedImage generateKeyImage(int width, int height) {

		// generate empty key image
		BufferedImage key = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D keyGraphics = key.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		keyGraphics.setColor(new Color(255, 255, 255, 0));
		keyGraphics.fillRect(0, 0, width, height);
		
		// fill it with the random key structure
		keyGraphics.setColor(new Color(0, 0, 0, 255));
		
		Random random = new Random();
		
		// each 2x2-pixel-pack has 2 randomly set pixels
		for (int y = 0; y < height; y += 2) {
			for (int x = 0; x < width; x += 2) {
				// determine the two pixels
				int px1 = random.nextInt(4);
			    int px2 = random.nextInt(4);
				while (px1 == px2) px2 = random.nextInt(4);
				
				// determine the coordinates of them
				int px1x = (px1 < 2) ? px1 : px1 - 2;
				int px1y = (px1 < 2) ? 0 : 1;
				int px2x = (px2 < 2) ? px2 : px2 - 2;
				int px2y = (px2 < 2) ? 0 : 1;
				
				// write them
				keyGraphics.fillRect(x + px1x, y + px1y, 1, 1);
				keyGraphics.fillRect(x + px2x, y + px2y, 1, 1);
			}
		}
		keyGraphics.dispose();
		
		return key;
	}

	//make it serect 
	public static BufferedImage encryptImage(BufferedImage imgSrc,BufferedImage imgKey) {
		if (imgKey == null || imgSrc == null) 
			return null;
		
		// resize the source to the size of the key
		BufferedImage imgSrcRes =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imgSrcRes.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g.drawImage(imgSrc, 0, 0, imgKey.getWidth(), imgKey.getHeight(), 0, 0, imgSrc.getWidth(), imgSrc.getHeight(), null);
		g.dispose();
		
		BufferedImage imgEncr =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D encrGraphics = imgEncr.createGraphics();
		
		// fill it with a fully transparent "white" (should allready be this way with TYPE_INT_ARGB)
		encrGraphics.setColor(new Color(255, 255, 255, 0));
		encrGraphics.fillRect(0, 0, imgEncr.getWidth(), imgEncr.getHeight());
		
		// encrypt
		encrGraphics.setColor(new Color(0, 0, 0, 255));
		
		// each 2x2-pixel-pack has 2 pixels to set
		for (int y = 0; y < imgEncr.getHeight(); y += 2) {
			for (int x = 0; x < imgEncr.getWidth(); x += 2) {
				// because 1 black pixel of the original image is now a square of 4 black pixels,
				// only the first pixel has to be checked
				if (imgSrcRes.getRGB(x, y) == Color.BLACK.getRGB()) {
					// write the two pixels to complete the block together with the key
					if (imgKey.getRGB(x, y)>>>24==0) encrGraphics.fillRect(x, y, 1, 1);
					if (imgKey.getRGB(x+1, y)>>>24==0) encrGraphics.fillRect(x + 1, y, 1, 1);
					if (imgKey.getRGB(x, y+1)>>>24==0) encrGraphics.fillRect(x, y + 1, 1, 1);
					if (imgKey.getRGB(x+1, y+1)>>>24==0) encrGraphics.fillRect(x + 1, y + 1, 1, 1);
	
				} 
				else {
					// write the two pixels at the same position in the key
					if (imgKey.getRGB(x, y) == Color.BLACK.getRGB()) encrGraphics.fillRect(x, y, 1, 1);
					if (imgKey.getRGB(x + 1, y) == Color.BLACK.getRGB()) encrGraphics.fillRect(x + 1, y, 1, 1);
					if (imgKey.getRGB(x, y + 1) == Color.BLACK.getRGB()) encrGraphics.fillRect(x, y + 1, 1, 1);
					if (imgKey.getRGB(x + 1, y + 1) == Color.BLACK.getRGB()) encrGraphics.fillRect(x + 1, y + 1, 1, 1);
				}
			}
		}
		encrGraphics.dispose();
		
		return imgEncr;
	}
	
	
	public static BufferedImage combineImage(BufferedImage imgEnc,BufferedImage imgKey) {
		
		// copy key to image
		BufferedImage imgOverlay =  new BufferedImage(imgKey.getWidth(), imgKey.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = imgOverlay.createGraphics();
		g.drawImage(imgKey, 0, 0, imgKey.getWidth(), imgKey.getHeight(), 0, 0, imgKey.getWidth(), imgKey.getHeight(), null);
		g.drawImage(imgEnc, 0, 0, imgEnc.getWidth(), imgEnc.getHeight(), 0, 0, imgEnc.getWidth(), imgEnc.getHeight(), null);
		g.dispose();
		
		return imgOverlay;
	}
	
	
	
}
