/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.uim.plugin.thumbler.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.im4java.core.ConvertCmd;
import org.im4java.core.ETOperation;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.ProcessExecutor;
import org.im4java.process.ProcessStarter;
import javax.imageio.ImageIO;

/**
*
* @author Georgios Markakis <gwarkx@hotmail.com>
* @since 12 Apr 2012
*/

public final class ImageMagickUtils {

	private final static ProcessExecutor exec;
	
	static{
		String myPath="C:\\Program Files\\ImageMagick-6.7.7-Q16\\ImageMagick.exe;C:\\Software\\exiftool\\exiftool(-k).exe";
		ProcessStarter.setGlobalSearchPath(myPath);
		
		 exec = new ProcessExecutor();
	}

	/**
	 * Private Constructor, no instantiation allowed
	 */
	private ImageMagickUtils(){
		
	}
	
	/**
	 * @param img
	 */
	public static BufferedImage convert(File img){
		BufferedImage newimg;
		
		try {
			newimg = ImageIO.read(img);
			return newimg;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

		/*
		IMOperation op = new IMOperation();
		op.addImage(img.getPath());
		op.addImage(img.getPath() + ".jpg");

		ConvertCmd convert = new ConvertCmd();
		try {
			convert.run(op);
			
			BufferedImage newimg = ImageIO.read(new File(img.getPath() + ".jpg"));
			BufferedImage newimg = ImageIO.read(img);

			return newimg;

			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IM4JavaException e) {
			e.printStackTrace();
		}
		
		return null;
		*/
	}
	
	
	/**
	 * @param img
	 */
	public static void addXMPInfo(BufferedImage img, Map<EDMXMPValues, String> map){
		ETOperation op = new ETOperation();

		 Iterator<EDMXMPValues> it = map.keySet().iterator();
		 
		 while(it.hasNext()){
			 EDMXMPValues xmpkey = it.next();
			 String xmpvalue = map.get(xmpkey);
			 op.setTag(xmpkey.getFieldId(), xmpvalue);
		 }

        ExiftoolCmd cmd = new ExiftoolCmd();
        
        
		try {
			cmd.run(op, img);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IM4JavaException e) {
			e.printStackTrace();
		}
	}

}
