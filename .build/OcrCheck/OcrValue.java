import com.catalyst.Context;
import com.catalyst.basic.BasicIO;
import com.catalyst.basic.ZCFunction;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import java.io.*;


import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.awt.image.BufferedImage;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.zc.component.files.ZCFile;
import com.zc.component.files.ZCFileDetail;
import com.zc.component.files.ZCFolder;

import com.zc.component.ml.ZCContent;
import com.zc.component.ml.ZCLine;
import com.zc.component.ml.ZCML;
import com.zc.component.ml.ZCOCRModelType;
import com.zc.component.ml.ZCOCROptions;
import com.zc.component.ml.ZCPanData;
import com.zc.component.ml.ZCParagraph;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;


public class OcrValue implements ZCFunction {
	private static final Logger LOGGER = Logger.getLogger(OcrValue.class.getName());
	static Boolean pan_format(String pan)
	{
		Pattern panPattern = Pattern.compile("^[A-Z]{5}\\d{4}[A-Z]$");
        Matcher panMatcher = panPattern.matcher(pan);
		if(panMatcher.matches())
		{
			return true;
		}
		return false;
	}
	@Override
    public void runner(Context context, BasicIO basicIO) throws Exception {
		try {
			
			ZCProject.initProject();
			

			//Create an instance for the File Store
			ZCFile fileStore = ZCFile.getInstance();
			//Get a folder instance using the Folder ID
			
			File aadhaarFront = new File("/Users/farook/downloads/myAadhaar1.jpg"); //Specify the file path of the front side image of the Aadhaar card
			File aadhaarBack = new File("/Users/farook/downloads/myAadhaar2.jpg"); //Specify the file path of the back side image of the Aadhaar card
			ZCFolder folder = fileStore.getFolderInstance(2299000000007939L);
			//Upload the file using the folder instance
			ZCFileDetail filedetails= folder.uploadFile(aadhaarFront);
			ZCFileDetail filedetail = folder.uploadFile(aadhaarBack);
			Long AadhaarfrontId = filedetails.getFileId();
			Long AadhaarbackId= filedetail.getFileId();
			File panCard = new File("/Users/farook/downloads/pan.jpg"); //Specify the file path
			ZCOCROptions options = ZCOCROptions.getInstance().setModelType(ZCOCRModelType.PAN); //Set the model type
			ZCContent ocrcontent = ZCML.getInstance().getContent(panCard, options); //Call getContent() with the file object to get the detected text in ZCContent object
			ZCPanData  panData = ocrcontent.getPanData(); //T his method obtains the PAN data
			ZCFileDetail pancard= folder.uploadFile(panCard);
			//To etch individual elements like the first name, last name, PAN details, and DOB from the processed image
			String  firstName = panData.getFirstName();
			String  lastName = panData.getLastName();
			String  pan = panData.getPan();
			Date  dob =  panData.getDob();
			//Get Folder details using folder ID
			Boolean Legit_pan=pan_format(pan);
//Download the File as an Input Stream using the file id
			InputStream f = (InputStream) folder.downloadFile(AadhaarfrontId);
			InputStream b = (InputStream) folder.downloadFile(AadhaarbackId);
			BufferedImage imagefront = ImageIO.read(f);
			BufferedImage imageback = ImageIO.read(b);
            ImageIO.write(imagefront, "jpg", new File("output1.jpg"));
			ImageIO.write(imageback, "jpg", new File("output2.jpg"));
			File fid=new File("output1.jpg");
			File bid =new File("output2.jpg");
			String languageCode = "eng,tam"; //Set the languages
			ZCContent ocrContent = ZCML.getInstance().getContentForAadhaar(fid , bid, languageCode); //Call getContent() with the file object to get the detected text in ZCContent object
			//To get individual paragraphs
			List <ZCParagraph> paragraphs = ocrContent.getParagraphs();
			for(ZCParagraph paragraph : paragraphs){
			//To get individual lines in the paragraph
				List<ZCLine> paraLines = paragraph.lines;    
				for(ZCLine line : paraLines){      
			//To get individual words in the line  
					String[] words = line.words;        
					String text = line.text;  //Raw line text
				}    
				String text = paragraph.text; //Returns the raw paragraph text
			}
			String text = ocrContent.text; //Returns the raw image text
			String pattern = "\\d{12}";

    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(text);

    int count = 0;
    while (m.find()) {
      count++;
    }
		    basicIO.setStatus(200);
			if(count==1 && Legit_pan==true)
			{
				basicIO.write("Valid");
			}
		    else
			{
				basicIO.write("Not Valid");
			}
	}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in OcrValue",e);
			basicIO.setStatus(500);

        	basicIO.write("Error in value");
		}
		

	}

}