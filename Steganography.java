import java.awt.image.BufferedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.Console;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by ozymandias on 22/03/18.
 */

public class Steganography {
    private BufferedImage picture;
    private int[] pixels;
    private int size;
    private int maxBitsPayload = 262144;
    private int pixelsNeededSize = 6;
    private int bpp = 3;
    private int width;
    private int height;

    public Steganography(BufferedImage picture){
        this.picture = picture;
	this.width = picture.getWidth();
	this.height = picture.getHeight();
        pixels = new int[this.width * this.height];
        //picture.getPixels(this.pixels, 0, picture.getWidth(), 0,0, picture.getWidth(), picture.getHeight());
	pixels = picture.getRGB(0, 0, width, height, null, 0, width);
    }

    private int getPayloadSize(){
        int finalSize = 0;
        String binSize = "";
        String r, g, b;
        for (int i=0; i<6; i++){
	    Color pixel = new Color(pixels[i]);
            r = Integer.toBinaryString(pixel.getRed());
            g = Integer.toBinaryString(pixel.getGreen());
            b = Integer.toBinaryString(pixel.getBlue());
            binSize += r.substring(r.length()-1) + g.substring(g.length() - 1) + b.substring(b.length() - 1);
        }
        finalSize = Integer.parseInt(binSize, 2);
	System.out.println("Tamaño payload: "+finalSize);
        //Log.d("Tamaño payload", ""+finalSize);
        return finalSize;
    }

    private String transfromBitString(String bits, int nFinal){
	String finalBin = new String(new char[nFinal-bits.length()]).replace('\0', '0');
	finalBin += bits;
	return finalBin;
    }

    private int hideBit(String bin, String bit){
	String intermBin = bin.substring(0, bin.length()-2) + bit;
	String finalBin = transfromBitString(intermBin, 8);
	System.out.println(finalBin);
	return Integer.parseInt(finalBin, 2);
    }

    private void hide(BufferedImage resulPic, String payload){
	int r, g, b;
	String payloadSizeBin = Integer.toBinaryString(payload.length());
	int bitsNeededSize = pixelsNeededSize * bpp;
	String finalPayloadSize = transfromBitString(payloadSizeBin, bitsNeededSize);
	System.out.println(finalPayloadSize);
	int[] sizePixels = new int[pixelsNeededSize];
	int[] payloadPixels = new int[(int) Math.ceil(payload.length()/bpp)];
	int[] finalPixels = new int[this.width * this.height];
	String flatPayloadBinary = "";
	Color pixel, newPixel;
	//hide size
	for (int i=0; i<pixelsNeededSize; i++){
	    pixel = new Color(pixels[i]);
	    r = hideBit(Integer.toBinaryString(pixel.getRed()), String.valueOf(finalPayloadSize.charAt((i * 3))));
            g = hideBit(Integer.toBinaryString(pixel.getGreen()), String.valueOf(finalPayloadSize.charAt((i * 3) + 1)));
            b = hideBit(Integer.toBinaryString(pixel.getBlue()), String.valueOf(finalPayloadSize.charAt((i * 3) + 2)));
	    newPixel = new Color(r, g, b);
	    finalPixels[i] = newPixel.getRGB();
	}

	//transform payload to binary
	for (int i=0; i<payload.length(); i++){
	    flatPayloadBinary += transfromBitString(Integer.toBinaryString(payload.charAt(i)), 8);
	}
	System.out.println(flatPayloadBinary);
	
	//hide payload
	for (int i=0; i<flatPayloadBinary.length()/bpp; i++){
	    boolean flagG = false;
	    boolean flagB = false;
	    pixel = new Color(pixels[pixelsNeededSize + i]);
	    r = hideBit(Integer.toBinaryString(pixel.getRed()), String.valueOf(flatPayloadBinary.charAt(i*3)));
	    try{
		g = hideBit(Integer.toBinaryString(pixel.getGreen()), String.valueOf(flatPayloadBinary.charAt((i*3)+1)));
	    }
	    catch (Exception e){
		g = pixel.getGreen();
	    }
	    try {
		b = hideBit(Integer.toBinaryString(pixel.getBlue()), String.valueOf(flatPayloadBinary.charAt((i*3)+2)));
	    }
	    catch (Exception e){
		b = pixel.getBlue();
	    } 
	    newPixel = new Color(r, g, b);
	    finalPixels[pixelsNeededSize + i] = newPixel.getRGB();
	}

	// finalPixels = sizePixels + payloadPixels;
	resulPic.setRGB(0, 0, width, height, finalPixels, 0, width);   
    }
    
    private String extract(){
	size = getPayloadSize();
        String decodedText = "";
        int nPixelsPayload = (int) Math.ceil(this.size*8/3);
        String binaryPayload = "";
        String r, g, b;
        for (int i=6; i<nPixelsPayload+6; i++){
	    Color pixel = new Color(pixels[i]);
            r = Integer.toBinaryString(pixel.getRed());
            g = Integer.toBinaryString(pixel.getGreen());
            b = Integer.toBinaryString(pixel.getBlue());
            binaryPayload += r.substring(r.length()-1) + g.substring(g.length() - 1) + b.substring(b.length() - 1);
        }
        ArrayList<String> splittedPayload = new ArrayList<>();
        int prevI = 0;
        for (int i=8; i<binaryPayload.length(); i+=8){
            splittedPayload.add(binaryPayload.substring(prevI, i));
	    decodedText += (char) Integer.parseInt(binaryPayload.substring(prevI, i), 2);
	    prevI += 8;
        }
	// return splittedPayload.toString();
        return decodedText;
    }

    public static void main(String[] args){
	if (args.length==0){
	    System.out.println("Specify picture");
	    System.exit(1);
	}
	Steganography stego;
	try{
	    BufferedImage originalImage = ImageIO.read(new File(args[1]));
	    stego = new Steganography(originalImage);
	    if (args[0].equals("hide")){
		// Console c = System.console();
		// System.out.println("Introduce texto a esconder:");
		// String text = c.readLine();
		BufferedImage finalPic = ImageIO.read(new File(args[1]));
		stego.hide(finalPic, "este es el texto para ocultare");
		ImageIO.write(finalPic, "PNG", new File("resul.png"));
	    }
	    else if (args[0].equals("reveal")){
		String extractedText = stego.extract();
		System.out.println(extractedText);
	    }
	}
	catch (IOException e){
	    System.out.println(e.getMessage());
	}
    }
}


