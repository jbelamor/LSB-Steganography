# LSB-Steganography

Source code in Python and Java to provide a cross-language library to hide texts in pictures based on the steganography technique LSB (less significant bit).

Despite the algorithm works fine, there's still need to work with the code to make it more user-friendly.

The idea is to be able to hide using either Python or Java and reveal the hidden payload using either Python or Java.

## Usage

### Python

#### Hide
```
>>> from stego import Stego
>>> st=Stego('example.jpg')

> In case of hiding the content of a file, use function:
>>> st.hide(st.get_payload_bins_from_file('test_payload.txt'))

> In case of coming from a string, use this one:
>>> st.hide(st.get_payload_bins_from_text('text_to_hide'))
```

#### Reveal
```
>>> from stego import Stego
>>> st=Stego('example.jpg')
>>> st.reveal()
```
------

### Java

#### Hide
Modifying the method `main` you can write the text you want to hide or import a file (this last function is not implemented yet)

```
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
```

#### Reveal
```
>>> from stego import Stego
>>> st=Stego('example.jpg')
>>> st.reveal()
```
